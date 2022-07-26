package top.yifan;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.NoOffsetForPartitionException;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.util.CollectionUtils;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * KafkaAdminTemplate
 *
 * @author Star Zheng
 */
public class KafkaAdminTemplate implements Closeable {

    private static final long DEFAULT_TIMEOUT_MS = 30000;
    private static final String QUERY_END_OFFSET_GROUP_ID = "query_end_offset_group_id";

    private final String bootstrapServers;
    private final AdminClient adminClient;
    private final Queue<String> consumerClientIdQueue = new LinkedBlockingDeque<>();
    private final AtomicInteger consumerClientIdNumberAdder = new AtomicInteger();

    public KafkaAdminTemplate(String bootstrapServers) {
        this(bootstrapServers, DEFAULT_TIMEOUT_MS);
    }

    public KafkaAdminTemplate(String bootstrapServers, long requestTimeoutMs) {
        this.bootstrapServers = bootstrapServers;
        this.adminClient = buildAdminClient(bootstrapServers, requestTimeoutMs);
    }

    @Override
    public void close() throws IOException {
        if (adminClient != null) {
            adminClient.close();
        }
    }

    /**
     * 精确创建 topic 通过 Map<String, String> configs 来指定 eg:
     * <p>
     * 旧日志段的保留测率，删除或压缩，此时选择删除
     * topicConfig.put(TopicConfig.CLEANUP_POLICY_CONFIG,TopicConfig.CLEANUP_POLICY_DELETE);
     * 过期数据的压缩方式，如果上面选项为压缩的话才有效
     * topicConfig.put(TopicConfig.COMPRESSION_TYPE_CONFIG,"snappy");
     * <p>
     * The amount of time to retain delete tombstone markers for log compacted topics.
     * This setting also gives a bound on the time in which a consumer must complete a
     * read if they begin from offset 0 to ensure that they get a valid snapshot of
     * the final stage (otherwise delete tombstones may be collected before they
     * complete their scan). 默认1天
     * topicConfig.put(TopicConfig.DELETE_RETENTION_MS_CONFIG,"86400000");
     * <p>
     * 文件在文件系统上被删除前的保留时间，默认为60秒
     * topicConfig.put(TopicConfig.FILE_DELETE_DELAY_MS_CONFIG,"60000");
     * 将数据强制刷入日志的条数间隔
     * topicConfig.put(TopicConfig.FLUSH_MESSAGES_INTERVAL_CONFIG,"9223372036854775807");
     * 将数据强制刷入日志的时间间隔
     * topicConfig.put(TopicConfig.FLUSH_MS_CONFIG,"9223372036854775807"); offset设置
     * topicConfig.put(TopicConfig.INDEX_INTERVAL_BYTES_CONFIG,"4096"); 每个批量消息最大字节数
     * topicConfig.put(TopicConfig.MAX_MESSAGE_BYTES_CONFIG,"1000012");
     * 记录标记时间与kafka本机时间允许的最大间隔，超过此值的将被拒绝
     * topicConfig.put(TopicConfig.MESSAGE_TIMESTAMP_DIFFERENCE_MAX_MS_CONFIG,"9223372036854775807");
     * 标记时间类型，是创建时间还是日志时间 CreateTime/LogAppendTime
     * topicConfig.put(TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG,"CreateTime");
     * 如果日志压缩设置为可用的话，设置日志压缩器清理日志的频率。默认情况下，压缩比率超过50%时会避免清理日志。
     * 此比率限制重复日志浪费的最大空间，设置为50%，意味着最多50%的日志是重复的。更高的比率设置意味着更少、更高效 的清理，但会浪费更多的磁盘空间。
     * topicConfig.put(TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG,"0.5");
     * 消息在日志中保持未压缩状态的最短时间，只对已压缩的日志有效
     * topicConfig.put(TopicConfig.MIN_COMPACTION_LAG_MS_CONFIG,"0");
     * 当一个producer的ack设置为all（或者-1）时，此项设置的意思是认为新记录写入成功时需要的最少副本写入成功数量。
     * 如果此最小数量没有达到，则producer抛出一个异常（NotEnoughReplicas
     * 或者NotEnoughReplicasAfterAppend）。 你可以同时使用min.insync.replicas
     * 和ack来加强数据持久话的保障。一个典型的情况是把一个topic的副本数量设置为3,
     * min.insync.replicas的数量设置为2,producer的ack模式设置为all，这样当没有足够的副本没有写入数据时，producer会抛出一个异常。
     * topicConfig.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,"1");
     * 如果设置为true，会在新日志段创建时预分配磁盘空间
     * topicConfig.put(TopicConfig.PREALLOCATE_CONFIG,"true");
     * 当保留策略为删除（delete）时，此设置控制在删除就日志段来清理磁盘空间前，保存日志段的partition能增长到的最大尺寸。
     * 默认情况下没有尺寸大小限制，只有时间限制。。由于此项指定的是partition层次的限制，它的数量乘以分区数才是topic层面保留的数量。
     * topicConfig.put(TopicConfig.RETENTION_BYTES_CONFIG,"-1");
     * 当保留策略为删除（delete）时，此设置用于控制删除旧日志段以清理磁盘空间前，日志保留的最长时间。默认为7天。
     * 这是consumer在多久内必须读取数据的一个服务等级协议（SLA）。
     * topicConfig.put(TopicConfig.RETENTION_MS_CONFIG,"604800000");
     * 此项用于控制日志段的大小，日志的清理和持久话总是同时发生，所以大的日志段代表更少的文件数量和更小的操作粒度。
     * topicConfig.put(TopicConfig.SEGMENT_BYTES_CONFIG,"1073741824");
     * 此项用于控制映射数据记录offsets到文件位置的索引的大小。我们会给索引文件预先分配空间，然后在日志滚动时收缩它。 一般情况下你不需要改动这个设置。
     * topicConfig.put(TopicConfig.SEGMENT_INDEX_BYTES_CONFIG,"10485760");
     * 从预订的段滚动时间中减去最大的随机抖动，避免段滚动时的惊群（thundering herds）
     * topicConfig.put(TopicConfig.SEGMENT_JITTER_MS_CONFIG,"0");
     * 此项用户控制kafka强制日志滚动时间，在此时间后，即使段文件没有满，也会强制滚动，以保证持久化操作能删除或压缩就数据。默认7天
     * topicConfig.put(TopicConfig.SEGMENT_MS_CONFIG,"604800000");
     * 是否把一个不在isr中的副本被选举为leader作为最后手段，即使这样做会带来数据损失
     * topicConfig.put(TopicConfig.UNCLEAN_LEADER_ELECTION_ENABLE_CONFIG,"false");
     *
     * @param topicName         topic名称
     * @param numPartitions     分区数量
     * @param replicationFactor 复制因子，建议大于等于 2
     * @param topicConfig       topic配置
     * @throws Exception
     */
    private boolean createTopic(String topicName, int numPartitions, short replicationFactor,
                                Map<String, String> topicConfig) throws Exception {
        NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
        newTopic.configs(topicConfig);
        CreateTopicsResult createTopicResult =
                this.adminClient.createTopics(Collections.singleton(newTopic));
        createTopicResult.values().get(topicName).get();

        return true;
    }

    public List<KafkaPartitionOffsetDTO> getPartitionOffsets(String topic, String groupId)
            throws ExecutionException, InterruptedException {
        // 允许重试一次，以防止连接问题
        int times = 0;
        for (; ; ) {
            KafkaConsumer<String, String> consumer = null;
            String clientId = getClientId();
            try {
                consumer = buildKafkaConsumer(QUERY_END_OFFSET_GROUP_ID, clientId);
                List<PartitionInfo> allPartitions = consumer.partitionsFor(topic);
                if (CollectionUtils.isEmpty(allPartitions)) {
                    return Collections.emptyList();
                }
                Map<Integer, KafkaPartitionOffsetDTO> partitionOffsetMap = allPartitions.stream()
                        .map(e -> new KafkaPartitionOffsetDTO(e.partition()))
                        .collect(Collectors.toMap(KafkaPartitionOffsetDTO::getPartition, e -> e));

                List<TopicPartition> topicPartitions =
                        allPartitions.stream()
                                .map(e -> new TopicPartition(topic, e.partition()))
                                .collect(Collectors.toList());
                // Get beginning offset
                Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(topicPartitions);
                beginningOffsets.forEach((k, v) -> {
                    partitionOffsetMap.putIfAbsent(k.partition(), new KafkaPartitionOffsetDTO(k.partition()));
                    partitionOffsetMap.get(k.partition()).setBeginning(v);
                });
                // Get end offset
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);
                endOffsets.forEach((k, v) -> {
                    partitionOffsetMap.putIfAbsent(k.partition(), new KafkaPartitionOffsetDTO(k.partition()));
                    partitionOffsetMap.get(k.partition()).setEnd(v);
                });
                // Get group offset
                this.adminClient.listConsumerGroupOffsets(groupId)
                        .partitionsToOffsetAndMetadata()
                        .get()
                        .forEach((k, v) -> {
                            partitionOffsetMap.putIfAbsent(k.partition(), new KafkaPartitionOffsetDTO(k.partition()));
                            partitionOffsetMap.get(k.partition()).setOffset(v.offset());
                        });
                // calculation lag and toList
                partitionOffsetMap.forEach((k, v) -> {
                    if (v.getEnd() != null && v.getOffset() != null) {
                        v.setLag(v.getEnd() - v.getOffset());
                    }
                });
                return partitionOffsetMap.values()
                        .stream()
                        .sorted(Comparator.comparing(KafkaPartitionOffsetDTO::getPartition))
                        .collect(Collectors.toList());
            } catch (ExecutionException | InterruptedException e) {
                if (!(e.getCause() instanceof TimeoutException) || ++times >= 2) {
                    throw e;
                }
            } finally {
                if (consumer != null) {
                    consumer.close(Duration.ofSeconds(30));
                }
                this.freeClientId(clientId);
            }
        }
    }

    /**
     * 判断topic是否存在
     *
     * @param topicName - Topic名称
     * @return 如果topic存在则返回true，否则返回false
     */
    public boolean isExistTopic(String topicName) throws ExecutionException, InterruptedException {
        try {
            getTopicPartitions(topicName);
        } catch (UnknownTopicOrPartitionException e) {
            return false;
        }
        return true;
    }

    /**
     * 获取指定topic的分区数量
     *
     * @param topicName topic名称
     * @return 返回分区数量
     * @throws UnknownTopicOrPartitionException 如果topic不存在，则抛出此异常
     */
    public int getTopicPartitions(String topicName) throws InterruptedException, ExecutionException {
        Objects.requireNonNull(topicName, "Topic is required when get num partitions");
        try {
            DescribeTopicsResult topicsResult =
                    this.adminClient.describeTopics(Collections.singletonList(topicName));
            Map<String, KafkaFuture<TopicDescription>> values = topicsResult.values();
            if (CollectionUtils.isEmpty(values) || !values.containsKey(topicName)) {
                throw new UnknownTopicOrPartitionException(String.format(
                        "No found topic[%s] for servers[%s]", topicName, bootstrapServers));
            }
            return values.get(topicName).get().partitions().size();
        } catch (ExecutionException e) {
            Throwable ex = e.getCause();
            if (ex instanceof UnknownTopicOrPartitionException) {
                throw new UnknownTopicOrPartitionException(String.format(
                        "No found topic[%s] for servers[%s]", topicName, bootstrapServers), e);
            }
            throw e;
        }
    }

    /**
     * 如果偏移量不存在或者超过了当前分区的范围，则将其重置为当前分区的开始位置。
     * @param topic    topic名称
     * @param groupId  groupID
     * @return 如果执行了重置偏移量操作则返回true，否则返回false
     */
    public boolean seekToBeginningIfNoOffsetOrOutOfRange(String topic, String groupId) {
        return this.resetOffsetIfOrOutOfRange( topic, groupId, -1);
    }

    /**
     * 如果偏移量不存在或者超过了当前分区的范围，则将其重置为当前分区的开始位置。
     *
     * @param endpoint 集群位置
     * @param topic    topic名称
     * @param groupId  groupID
     * @return 如果执行了重置偏移量操作则返回true，否则返回false
     */
    public boolean seekToEndIfNoOffsetOrOutOfRange(String endpoint, String topic, String groupId) {
        return this.resetOffsetIfOrOutOfRange(topic, groupId, 1);
    }

    /**
     * 重置指定topic group的offset
     *
     * @param topic          topic名称
     * @param groupId        groupID
     * @param beginningOrEnd 如果大于0，则重置为end，否则充值为beginning
     * @return 如果执行了重置偏移量操作则返回true，否则返回false
     */
    private boolean resetOffsetIfOrOutOfRange(String topic, String groupId, int beginningOrEnd) {
        SingleObject<Boolean> isUpdated = SingleObject.build(false);
        KafkaConsumer<String, String> consumer = null;
        String clientId = getClientId();
        try {
            consumer = buildKafkaConsumer(groupId, clientId);
            List<PartitionInfo> allPartitions = consumer.partitionsFor(topic);
            List<TopicPartition> topicPartitions = allPartitions.stream()
                    .map(e -> new TopicPartition(topic, e.partition()))
                    .collect(Collectors.toList());
            consumer.assign(topicPartitions);
            final KafkaConsumer<String, String> finalConsumer = consumer;
            topicPartitions.forEach(e -> {
                if (isOutOfRange(finalConsumer, e)) {
                    Map<TopicPartition, Long> newOffsets;
                    if (beginningOrEnd <= 0) {
                        newOffsets = finalConsumer.beginningOffsets(Collections.singletonList(e));
                    } else {
                        newOffsets = finalConsumer.endOffsets(Collections.singletonList(e));
                    }
                    newOffsets.forEach((k, v) -> {
                        Map<TopicPartition, OffsetAndMetadata> commits = Collections.singletonMap(
                                new TopicPartition(topic, k.partition()),
                                new OffsetAndMetadata(v));
                        finalConsumer.commitSync(commits);
                    });
                    isUpdated.set(true);
                }
            });
        } finally {
            if (consumer != null) {
                consumer.close(Duration.ofSeconds(30));
            }
            this.freeClientId(clientId);
        }
        return isUpdated.get();
    }

    private boolean isOutOfRange(KafkaConsumer<?, ?> consumer, TopicPartition topicPartition) {
        try {
            long position = consumer.position(topicPartition);
            Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(Collections.singletonList(topicPartition));
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(Collections.singletonList(topicPartition));

            for (Map.Entry<TopicPartition, Long> entry : beginningOffsets.entrySet()) {
                if (position < entry.getValue()) {
                    return true;
                }
            }
            for (Map.Entry<TopicPartition, Long> entry : endOffsets.entrySet()) {
                if (position > entry.getValue()) {
                    return true;
                }
            }
        } catch (NoOffsetForPartitionException e) {
            return true;
        }
        return false;
    }

    private AdminClient buildAdminClient(String bootstrapServers, long requestTimeoutMs) {
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 请求超时时间
        properties.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);

        return AdminClient.create(properties);
    }

    private KafkaConsumer<String, String> buildKafkaConsumer(String groupId, String clientId) {
        Properties prop = new Properties();
        prop.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "none");
        prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // 请求超时时间
        prop.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, DEFAULT_TIMEOUT_MS);
        return new KafkaConsumer<>(prop);
    }

    private String getClientId() {
        String clientId = consumerClientIdQueue.poll();
        if (StringUtils.isBlank(clientId)) {
            clientId = "query-consumer-" + consumerClientIdNumberAdder.getAndIncrement();
        }
        return clientId;
    }

    private void freeClientId(String clientId) {
        this.consumerClientIdQueue.offer(clientId);
    }

}
