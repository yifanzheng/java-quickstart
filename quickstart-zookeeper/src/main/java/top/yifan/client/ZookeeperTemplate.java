package top.yifan.client;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yifan.NoSuchNodeException;
import top.yifan.ZookeeperException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ZookeeperTemplate
 *
 * @author Star Zheng
 */
public class ZookeeperTemplate {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperTemplate.class);

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final CuratorFramework client;

    public ZookeeperTemplate(ZookeeperClient zookeeperClient) {
        client = zookeeperClient.getClient();
    }

    public void createPersistent(String nodePath) {
        try {
            client.create().creatingParentsIfNeeded().forPath(nodePath);
        } catch (KeeperException.NodeExistsException e) {
            log.warn("ZNode [{}] already exists.", nodePath, e);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void createEphemeral(String nodePath) {
        try {
            client.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(nodePath);
        } catch (KeeperException.NodeExistsException e) {
            log.warn("ZNode " + nodePath + " already exists, since we will only try to recreate a node on a session expiration" +
                    ", this duplication might be caused by a delete delay from the zk server, which means the old expired session" +
                    " may still holds this ZNode and the server just hasn't got time to do the deletion. In this case, " +
                    "we can just try to delete and create again.", e);
            deleteNode(nodePath);
            createEphemeral(nodePath);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void createPersistent(String nodePath, String data) {
        byte[] dataBytes = data.getBytes(CHARSET);
        try {
            client.create()
                    .creatingParentsIfNeeded()
                    .forPath(nodePath, dataBytes);
        } catch (KeeperException.NodeExistsException e) {
            try {
                client.setData().forPath(nodePath, dataBytes);
            } catch (Exception e1) {
                throw new IllegalStateException(e.getMessage(), e1);
            }
            log.warn("ZNode [{}] already exists.", nodePath, e);
        } catch (Exception e) {
            throw new ZookeeperException("Create Zookeeper node[" + nodePath + "] error", e);
        }
    }

    /**
     * 获取节点数据
     *
     * @param nodePath 节点路径
     * @return data string
     */
    public String getData(String nodePath) {
        try {
            byte[] dataBytes = client.getData().forPath(nodePath);
            return new String(dataBytes, CHARSET);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new ZookeeperException("Get Zookeeper node[" + nodePath + "] error", e);
        }
    }

    /**
     * 设定指定节点的数据
     *
     * @param nodePath 路径
     * @param data     数据
     * @return 节点Stat
     */
    public Stat setData(String nodePath, String data) {
        Preconditions.checkArgument(StringUtils.isBlank(nodePath), "NodePath cannot be empty");
        try {
            return client.setData().forPath(nodePath, data.getBytes(CHARSET));
        } catch (KeeperException.NoNodeException e) {
            throw new NoSuchNodeException("No such config for node path: " + nodePath, e);
        } catch (Exception e) {
            throw new ZookeeperException("Cannot set data for path: " + nodePath, e);
        }
    }

    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            throw new ZookeeperException(e.getMessage(), e);
        }
    }

    public void deleteNode(String nodePath) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(nodePath);
        } catch (KeeperException.NoNodeException ignore) {
        } catch (Exception e) {
            throw new ZookeeperException("Delete Zookeeper node[" + nodePath + "] error", e);
        }
    }

    /**
     * 检查一个路径是否存在
     *
     * @param nodePath 节点路径
     * @return 存在则返回true，否则返回false
     */
    public boolean isExists(String nodePath) {
        Preconditions.checkArgument(StringUtils.isBlank(nodePath), "NodePath cannot be empty");
        try {
            if (client.checkExists().forPath(nodePath) != null) {
                return true;
            }
        } catch (Exception e) {
            log.error("Cannot check exists for path: {}", nodePath, e);
        }
        return false;
    }

}
