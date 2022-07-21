package top.yifan;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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


    public List<KafkaPartitionOffsetDTO> getPartitionOffsets(String topic, String groupId)
            throws ExecutionException, InterruptedException {
        // 允许重试一次，以防止连接问题
        int times = 0;
        for (; ; ) {
            KafkaConsumer<String, String> consumer = null;
            String clientId = getClientId();
            try {
                consumer = buildKafkaConsumer(clientId);
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

    private AdminClient buildAdminClient(String bootstrapServers, long requestTimeoutMs) {
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 请求超时时间
        properties.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);

        return AdminClient.create(properties);
    }

    private KafkaConsumer<String, String> buildKafkaConsumer(String clientId) {
        Properties prop = new Properties();
        prop.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, QUERY_END_OFFSET_GROUP_ID);
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
