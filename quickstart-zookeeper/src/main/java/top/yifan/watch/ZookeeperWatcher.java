package top.yifan.watch;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yifan.client.ZookeeperClient;

import java.io.Closeable;

/**
 * ZookeeperWatcher
 *
 * @author sz7v
 */
public class ZookeeperWatcher {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperWatcher.class);

    private CuratorFramework client;

    public ZookeeperWatcher(ZookeeperClient zookeeperClient) {
        this.client = zookeeperClient.createConnect();
    }

    public Closeable watch(String nodePath) throws Exception {
        client.start();
        TreeCache cache = TreeCache.newBuilder(client, nodePath).build();
        cache.getListenable().addListener((client, event) -> {
            if (event.getType() == TreeCacheEvent.Type.INITIALIZED) {
                log.info("Watch node[{}] success.", nodePath);
                return;
            }
            if (event.getData().getPath().equals(nodePath) && event.getType() == TreeCacheEvent.Type.NODE_ADDED) {
                System.out.println(new String(event.getData().getData()));
                System.out.println("type=" + event.getType() + " path=" + event.getData().getPath());
                return;
            }
            if (event.getData().getPath().equals(nodePath) && event.getType() == TreeCacheEvent.Type.NODE_UPDATED) {
                System.out.println(new String(event.getData().getData()));
                System.out.println("type=" + event.getType() + " path=" + event.getData().getPath());
                return;
            }
        });
        cache.start();

        return cache;
    }

    public Closeable watchChildrenForNodePath(String nodePath) throws Exception {
        client.start();
        PathChildrenCache cache = new PathChildrenCache(client, nodePath, true);
        cache.getListenable().addListener((curatorFramework, event) -> {
            if (event.getType() == PathChildrenCacheEvent.Type.INITIALIZED) {
                log.info("Watch children for node[{}] success.", nodePath);
                return;
            }
            if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED
                    || event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED
                    || event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                System.out.println(curatorFramework.getChildren().forPath(nodePath));
                System.out.println("Node changed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
            }
        });
        cache.start();
        return cache;
    }
}
