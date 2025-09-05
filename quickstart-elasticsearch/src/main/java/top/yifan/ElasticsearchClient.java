package top.yifan;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * ElasticsearchManager
 *
 * <p>
 * For: since elasticsearch 7.x。新特性：不再支持 type，参见：
 * https://www.elastic.co/guide/en/elasticsearch/reference/6.7/removal-of-types.html
 * </p>
 *
 * @author Star Zheng
 */
public class ElasticsearchClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchClient.class);

    private static RestHighLevelClient client = null;

    private final ElasticsearchProperties esProperties;

    public ElasticsearchClient(ElasticsearchProperties esProperties) {
        this.esProperties = esProperties;
    }

    public RestHighLevelClient getClient() {
        connectIfAbsent();
        return client;
    }

    private void connectIfAbsent() {
        if (client != null) {
            return;
        }
        synchronized (ElasticsearchClient.class) {
            if (client != null) {
                return;
            }
            String hosts = esProperties.getHosts();
            Integer port = esProperties.getPort();
            String protocol = esProperties.getProtocol();
            if (StringUtils.isAnyBlank(hosts, protocol)) {
                throw new IllegalArgumentException("Elasticsearch hosts or protocol can't be empty");
            }
            String[] hostArr = hosts.split(",");
            HttpHost[] httpHosts = new HttpHost[hostArr.length];
            for (int i = 0; i < hostArr.length; i++) {
                httpHosts[i] = new HttpHost(hostArr[i], port, protocol.trim());
            }
            client = new RestHighLevelClient(RestClient.builder(httpHosts));
            log.info("Connection Elasticsearch at: {}", hosts);
        }
    }


    private void connectWithAuthIfAbsent() {
        if (client != null) {
            return;
        }
        synchronized (ElasticsearchClient.class) {
            if (client != null) {
                return;
            }
            String hosts = esProperties.getHosts();
            Integer port = esProperties.getPort();
            String protocol = esProperties.getProtocol();
            if (StringUtils.isAnyBlank(hosts, protocol)) {
                throw new IllegalArgumentException("Elasticsearch hosts or protocol can't be empty");
            }
            String[] hostArr = hosts.split(",");
            HttpHost[] httpHosts = new HttpHost[hostArr.length];
            for (int i = 0; i < hostArr.length; i++) {
                httpHosts[i] = new HttpHost(hostArr[i], port, protocol.trim());
            }
            // 创健身份验证提供者
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("userName", "password"));

            client = new RestHighLevelClient(RestClient.builder(httpHosts)
                    .setHttpClientConfigCallback(httpClientBuilder
                            -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)));
        }
    }

    @Override
    public void close() throws IOException {
        if (client != null) {
            client.close();
        }
        log.info("[APP Destroy] Shutdown Elasticsearch RestHighLevelClient");
    }

}
