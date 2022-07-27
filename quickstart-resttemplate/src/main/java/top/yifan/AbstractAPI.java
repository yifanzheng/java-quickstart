package top.yifan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 抽象的REST API实现类
 * <p>
 * 提供普通超时连接和长超时连接的RestTemplate属性，以及可重试的方法等
 * </p>
 * @author Star Zheng
 */
public abstract class AbstractAPI {

    private static final Logger log = LoggerFactory.getLogger(AbstractAPI.class);
    
    /** 默认间隔时间 3000，单位毫秒 */
    private static final int DEFAULT_INTERVAL = 3000;
    
    protected RestTemplate restTemplate;
    
    protected RestTemplate longerRestTemplate;
    
    public AbstractAPI() {
    }
    
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setLongerRestTemplate(RestTemplate longerRestTemplate) {
        this.longerRestTemplate = longerRestTemplate;
    }

    /**
     * 支持重试请求的HTTP调用方法，使用默认的重试间隔时间 {@link #DEFAULT_INTERVAL}
     * 
     * @see #retryable(RetryableFunction, int, int)
     */
    protected <R> R retryable(RetryableFunction<R> fun, int maxRetryTimes) {
        createRestTemplateIfAbsent();
        return this.retryable(restTemplate, fun, maxRetryTimes, DEFAULT_INTERVAL);
    }
    
    /**
     * 支持重试请求的HTTP调用方法，一个简单的示例：
     * <pre>
     * this.retryable(new RetryableFunction\<Object\>() {
     *       
     *       \@Override
     *       public Object apply(RestTemplate restTemplate) throws RestClientException {
     *           restTemplate.getForEntity(xxx);
     *           return null;
     *       }
     *       
     *   }, 10);
     * </pre>
     * 更多请参见：{@link RetryableFunction}
     * 
     * @param fun - 执行具体方法的接口实现
     * @param maxRetryTimes - 最大重试次数
     * @param interval - 每次重试的间隔时间，单位毫秒
     * @return 返回执行结果
     */
    protected <R> R retryable(RetryableFunction<R> fun, int maxRetryTimes, int interval) {
        createRestTemplateIfAbsent();
        return this.retryable(restTemplate, fun, maxRetryTimes, interval);
    }
    
    /**
     * 支持长连接重试请求的HTTP调用方法，使用默认的重试间隔时间 {@link #DEFAULT_INTERVAL}
     *
     * @see #retryableLonger(RetryableFunction, int, int)
     */
    protected <R> R retryableLonger(RetryableFunction<R> fun, int maxRetryTimes) {
        createLongerRestTemplateIfAbsent();
        return this.retryable(longerRestTemplate, fun, maxRetryTimes, DEFAULT_INTERVAL);
    }
    
    /**
     * 支持长连接重试请求的HTTP调用方法，类似于： {@link #retryable(RetryableFunction, int)}
     * 
     * @param fun - 执行具体方法的接口实现
     * @param maxRetryTimes - 最大重试次数
     * @param interval - 每次重试的间隔时间，单位毫秒
     * @return 返回执行的结果
     */
    protected <R> R retryableLonger(RetryableFunction<R> fun, int maxRetryTimes, int interval) {
        createLongerRestTemplateIfAbsent();
        return this.retryable(longerRestTemplate, fun, maxRetryTimes, interval);
    }
    
    protected String fullURL(String endpoint, String uri) {
        return URLUtil.fullURL(endpoint, uri);
    }
    
    private <R> R retryable(RestTemplate restTemplate, RetryableFunction<R> fun, 
            int maxRetryTimes, int interval) {
        for (int times = 1; ; times ++) {
            try {
                return fun.apply(restTemplate);
            } catch (RestClientException e) {
                if (times <= maxRetryTimes && fun.isRetry(e)) {
                    log.warn("Call rest api error, and attempt={}/{} after 3 seconds, message: {}",
                            times, maxRetryTimes, e.getMessage());
                    ThreadUtil.sleep(interval);
                    continue;
                }
                throw e;
            }
        }
    }
    
    private void createRestTemplateIfAbsent() {
        if (restTemplate != null) {
            return;
        }
        HttpConfiguration httpConfig = new HttpConfiguration();
        final RestTemplate newRestTemplate = new RestTemplate();
        newRestTemplate.setRequestFactory(
                new HttpComponentsClientHttpRequestFactory(httpConfig.httpClient()));
        restTemplate = newRestTemplate;
        log.info("[{}]: Cannot get [restTemplate] object from spring environment, it will create one automatically", this.getClass().getSimpleName());
    }
    
    private void createLongerRestTemplateIfAbsent() {
        if (longerRestTemplate != null) {
            return;
        }
        HttpConfiguration httpConfig = new HttpConfiguration();
        final RestTemplate newLongerRestTemplate = new RestTemplate();
        newLongerRestTemplate.setRequestFactory(
                new HttpComponentsClientHttpRequestFactory(httpConfig.longerHttpClient()));
        longerRestTemplate = newLongerRestTemplate;
        log.info("[{}]: Cannot get [longerRestTemplate] object from spring environment, " +
                "it will create one automatically", this.getClass().getSimpleName());
    }
    
}
