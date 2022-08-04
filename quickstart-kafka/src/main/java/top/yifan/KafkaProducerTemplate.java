package top.yifan;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaProducerTemplate
 *
 * @author Star Zheng
 */
public class KafkaProducerTemplate {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerTemplate(Map<String, Object> configs) {
        this.kafkaTemplate = this.createKafkaTemplate(configs);
    }

    public KafkaProducerTemplate(String bootstrapServers) {
        this.kafkaTemplate = this.createConservativeKafkaTemplate(bootstrapServers);
    }

    /**
     * 异步发送消息
     *
     * @param topic     topic
     * @param partition 分区号
     * @param data      数据
     * @return 返回发送后的Future对象，调用Future.get()方法可以实现异步转同步
     */
    public ListenableFuture<SendResult<String, String>> send(String topic, int partition, String data) {
        return kafkaTemplate.send(topic, partition, String.valueOf(partition), data);
    }

    /**
     * 创建一个可靠的KafkaTemplate对象。
     *
     * <p>可靠意味着它将通过牺牲KafkaProducer的批次提交、缓存等特性，从而总是立即发送消息并等待全部broker的响应（保存消息副本的broker），
     * 所以在创建KafkaTemplate的过程中，除了bootstrapServers配置外，还重置了如下的配置：
     * <ul>
     *  <li>acks=-1/all</li>
     *  <li>max.request.size=10485760 (10M)</li>
     *  <li>batch.size=0</li>
     *  <li>serializer=string</li>
     * </ul>
     *
     * @param bootstrapServers Kafka服务器地址
     * @return 返回创建成功后的KafkaTemplate对象
     */
    private KafkaTemplate<String, String> createConservativeKafkaTemplate(String bootstrapServers) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ProducerConfig.ACKS_CONFIG, "all"); // 所有消息同步到slave节点后才会返回成功的确认消息给客户端。
        configs.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10485760); // set to 10MB, default: 1MB
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG, 0);
        configs.put(ProducerConfig.RETRIES_CONFIG, 3);
        configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 90 * 1000); // 如果90s的时间内没有新的数据发送，则断开链接
        configs.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip"); // 默认 none， 可选 GZIP、Snappy、LZ4、zstd(压缩比高)
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 60 * 1000); // 强制刷新元数据时间间隔, 60s

        // 注册拦截器
        // configs.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, Lists.newArrayList("top.yifan.interceptor.KafkaProducerInterceptor"));

        return createKafkaTemplate(configs);
    }

    private KafkaTemplate<String, String> createKafkaTemplate(Map<String, Object> producerConfigs) {
        if (CollectionUtils.isEmpty(producerConfigs)) {
            throw new IllegalArgumentException("Configuration cannot be empty");
        }
        DefaultKafkaProducerFactory<String, String> factory =
                new DefaultKafkaProducerFactory<>(producerConfigs);

        return new KafkaTemplate<>(factory);
    }
}
