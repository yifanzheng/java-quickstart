package top.yifan;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.Test;

import java.util.Collections;

/**
 * KafkaConsumerTemplateTest
 *
 * @author Star Zheng
 */
public class KafkaConsumerTemplateTest {

    @Test
    public void testConsumer() {
        String servers = "localhost:9092";
        String groupId = "5823657659514547373";
        String clientId = "star-0";
        KafkaConsumerTemplate kafkaConsumerTemplate = new KafkaConsumerTemplate(servers, groupId, clientId);
        kafkaConsumerTemplate.subscribe(Collections.singletonList("quickstart"));

        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumerTemplate.pollOnce(1000L);
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    System.out.println(consumerRecord.value());
                    // 业务
                    // 提交commit
                    kafkaConsumerTemplate.ackSync(consumerRecord);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            kafkaConsumerTemplate.close();
        }
    }
}
