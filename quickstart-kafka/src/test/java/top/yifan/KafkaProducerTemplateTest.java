package top.yifan;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import top.yifan.entity.User;

import java.util.concurrent.ExecutionException;

/**
 * KafkaProducerTemplateTest
 *
 * @author Star Zheng
 */
public class KafkaProducerTemplateTest {

    @Test
    public void testSend() {
        KafkaProducerTemplate producerTemplate =
                new KafkaProducerTemplate("localhost:9092");
        User user = new User("star", "45");
        ListenableFuture<SendResult<String, String>> future =
                producerTemplate.send("quickstart", 0, JSON.toJSONString(user));
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
