package top.yifan.client;

import io.etcd.jetcd.Client;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

public class EtcdClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(EtcdClient.class);

    /**
     * create client using endpoints,
     * example: "http://etcd0:2379", "http://etcd1:2379", "http://etcd2:2379"
     */
    private final String endpoints;

    private Client client;

    private volatile boolean isActived = false;

    public EtcdClient(String endpoints) {
        this.endpoints = endpoints;
    }

    public Client getClient() {
        connectIfAbsent();
        return client;
    }

    public boolean isClosed() {
        return !isActived;
    }

    private void connectIfAbsent() {
        if (client != null && isActived) {
            return;
        }
        synchronized (this) {
            if (client != null && isActived) {
                return;
            }
            if (StringUtils.isBlank(endpoints)) {
                throw new IllegalArgumentException("ETCD endpoints can't be empty");
            }
            String[] endpointArr = endpoints.split(",");
            client = Client.builder().endpoints(endpointArr).build();
            isActived = true;
            log.info("Connection ETCD at: {}", endpoints);
        }
    }

    @Override
    public void close() throws IOException {
        if (client != null) {
            client.close();
            isActived = false;
            log.info("[APP Destroy] Closed ETCD");
        }
    }
}