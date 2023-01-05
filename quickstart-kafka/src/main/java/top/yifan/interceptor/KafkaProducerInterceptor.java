package top.yifan.interceptor;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * KafkaProducerInterceptor
 *
 * @author Star Zheng
 */
public class KafkaProducerInterceptor implements ProducerInterceptor<String, String> {

    /**
     * 发送消息之前触发
     */
    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> producerRecord) {

        return producerRecord;
    }

    /**
     * 消息成功提交或发送失败之后被调用
     */
    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
