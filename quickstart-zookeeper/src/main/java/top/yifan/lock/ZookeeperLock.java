package top.yifan.lock;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yifan.client.ZookeeperClient;

import java.util.concurrent.TimeUnit;

/**
 * ZookeeperLock
 *
 * @author Star Zheng
 */
public class ZookeeperLock {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperLock.class);

    private InterProcessMutex mutex;

    public ZookeeperLock(ZookeeperClient client, String lockPath) {
        this.mutex = new InterProcessMutex(client.getClient(), lockPath);
    }

    private boolean tryLock(long timeout) {
        try {
            return this.mutex.acquire(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("get lock failed, message: {}", e.getMessage());
            return false;
        }
    }

    public void lock() throws Exception {
        this.mutex.acquire();
    }

    public void unlock() throws Exception {
        this.mutex.release();
    }

}
