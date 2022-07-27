package top.yifan;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 用于HTTP请求时的可重试的方法调用接口，详情参见：{@link AbstractAPI#retryable(RetryableFunction, int)}
 * 
 * @author Star Zheng
 *
 * @param <R> 返回对象类型
 */
public interface RetryableFunction<R> {
    
    /**
     * 调用HTTP请求的具体实现
     * 
     * @param restTemplate - RestTemplate对象
     * @return 返回HTTP返回的对象
     * @throws RestClientException 如果请求过程中发生任何HHTP异常，则转译为此异常抛出
     */
    R apply(RestTemplate restTemplate) throws RestClientException;
    
    /**
     * 是否重试，如果返回false，将不再继续重试。默认仅ResourceAccessException类型异常会重试
     * 
     * @param e - 异常详情
     * @return 返回 True或者False
     */
    default boolean isRetry(RestClientException e) {
        if (e instanceof ResourceAccessException) {
            return true;
        }
        return false;
    }
    
}
