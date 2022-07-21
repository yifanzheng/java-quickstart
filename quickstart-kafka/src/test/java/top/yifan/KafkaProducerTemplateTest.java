package top.yifan;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

/**
 * @author sz7v
 */
public class KafkaProducerTemplateTest {

    @Test
    public void testSend() {
        KafkaProducerTemplate producerTemplate =
                new KafkaProducerTemplate("localhost:8092");
        User user = new User("star", "45");
        ListenableFuture<SendResult<String, String>> future =
                producerTemplate.send("ndhc_gdev_gdev_s7cwldb01_ndhc_sourcedb_dbo_test_binary_glaxfhlt", 0, JSON.toJSONString(user));
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
