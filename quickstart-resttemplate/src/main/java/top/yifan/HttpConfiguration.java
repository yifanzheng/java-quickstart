package top.yifan;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpConfiguration {

    @Bean
    @Primary
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate rest = builder.build();
        rest.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient()));
        return rest;
    }

    @Bean(name = "longerRestTemplate")
    public RestTemplate longerRestTemplate(RestTemplateBuilder builder) {
        RestTemplate rest = builder.build();
        rest.setRequestFactory(new HttpComponentsClientHttpRequestFactory(longerHttpClient()));
        return rest;
    }

    @Bean
    public HttpClient httpClient() {
        // 默认超时时间60s
        return createHttpClient(60 * 1000);
    }

    @Bean(name = "longerHttpClient")
    public HttpClient longerHttpClient() {
        // 默认超时时间10分钟
        return createHttpClient(10 * 60 * 1000);
    }

    private HttpClient createHttpClient(int socketTimeout) {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(1000);
        connectionManager.setDefaultMaxPerRoute(100);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout) // 服务器返回数据(response)的时间，超过该时间抛出read timeout
                .setConnectTimeout(30 * 1000) // 连接上服务器(握手成功)的时间，超出该时间抛出connect timeout
                .setConnectionRequestTimeout(10 * 1000) //从连接池中获取连接的超时时间，超过该时间未拿到可用连接，会抛出org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
    }

}
