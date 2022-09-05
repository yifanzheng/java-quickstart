package top.yifan.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yifan.ZookeeperException;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * ZookeeperClient
 *
 * @author sz7v
 */
public class ZookeeperClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperClient.class);

    private final String connectString;

    private CuratorFramework client;

    private volatile boolean isActived = false;

    public ZookeeperClient(String connectString) {
        this.connectString = connectString;
    }

    public CuratorFramework getClient() {
        connectIfAbsent();
        return client;
    }

    public CuratorFramework createConnect() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 以下 Zookeeper 连接配置在实际开发中，可从配置文件中获取
        CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                // Zookeeper 服务器地址字符串
                .connectString(connectString)
                // 会话超时时间
                .sessionTimeoutMs(30000)
                // 连接超时
                .connectionTimeoutMs(10000)
                // 重连策略
                .retryPolicy(retryPolicy)
                // 命名空间，表示当前客户端的父节点，我们可以用它来做业务区分
                //.namespace(namespace)
                .build();
        zkClient.start();
        try {
            // 阻塞直到连接成功
            boolean connected = zkClient.blockUntilConnected(10000, TimeUnit.MILLISECONDS);
            if (!connected) {
                throw new ZookeeperException("Connect to zookeeper failed with timeout 10000ms");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Connect to zookeeper failed, message: {}", e.getMessage());
            throw new ZookeeperException(e);
        }
        return zkClient;
    }

    private void connectIfAbsent() {
        if (client != null && isActived) {
            return;
        }
        synchronized (this) {
            if (client != null && isActived) {
                return;
            }
            if (StringUtils.isBlank(connectString)) {
                throw new IllegalArgumentException("Zookeeper connect string can't be empty");
            }
            // 以下 Zookeeper 连接配置在实际开发中，可从配置文件中获取
            client = this.createConnect();
            isActived = true;
            log.info("Connection Zookeeper at: {}", connectString);
        }
    }

    @Override
    public void close() throws IOException {
        if (client != null) {
            client.close();
            isActived = false;
        }
    }
}
