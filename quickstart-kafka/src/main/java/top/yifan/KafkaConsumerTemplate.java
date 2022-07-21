package top.yifan;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KafkaConsumerTemplate
 *
 * @author Star Zheng
 */
public class KafkaConsumerTemplate {

    private final Consumer<String, String> kafkaConsumer;

    public KafkaConsumerTemplate(String bootstrapServers, String groupId, String clientId) {
        this.kafkaConsumer = this.createConservativeKafkaConsumer(bootstrapServers, groupId, clientId);
    }

    public KafkaConsumerTemplate(Map<String, Object> configs) {
        this.kafkaConsumer = this.createKafkaConsumer(configs);
    }

    /**
     * 订阅主题
     *
     * @param topics topics
     */
    public void subscribe(List<String> topics) {
        this.kafkaConsumer.subscribe(topics);
    }

    /**
     * 订阅指定分区进行消费
     *
     * @param partitions 分区列表
     */
    public void assign(List<TopicPartition> partitions) {
        this.kafkaConsumer.assign(partitions);
    }

    /**
     * 拉取消息
     *
     * @param ttl 超时时间
     * @return 消息记录集合
     */
    public ConsumerRecords<String, String> pollOnce(long ttl) {
        return this.kafkaConsumer.poll(Duration.ofMillis(ttl));
    }

    /**
     * 同步提交消息
     *
     * @param consumerRecord 消息记录
     */
    public void ackSync(ConsumerRecord<String, String> consumerRecord) {
        Map<TopicPartition, OffsetAndMetadata> commits = Collections.singletonMap(
                new TopicPartition(consumerRecord.topic(), consumerRecord.partition()),
                new OffsetAndMetadata(consumerRecord.offset() + 1));
        // ack 消息
        this.kafkaConsumer.commitSync(commits);
    }

    public void close() {
        this.kafkaConsumer.close();
    }

    private Consumer<String, String> createConservativeKafkaConsumer(String bootstrapServers, String groupId, String clientId) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);

        return createKafkaConsumer(configs);
    }

    private Consumer<String, String> createKafkaConsumer(Map<String, Object> consumerConfigs) {
        if (CollectionUtils.isEmpty(consumerConfigs)) {
            throw new IllegalArgumentException("Configuration cannot be empty");
        }
        DefaultKafkaConsumerFactory<String, String> factory = new DefaultKafkaConsumerFactory<>(consumerConfigs);

        return factory.createConsumer();
    }
}
