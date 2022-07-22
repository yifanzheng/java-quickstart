package top.yifan;

import com.alibaba.fastjson.JSON;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseTimeToLiveResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * EtcdTemplate
 * <p>
 * 提供关于ETCD 基础功能的服务
 * </p>
 * <p>
 * 注：ECTD 的设计中使用k-v扁平化结构替代了原先的目录结构，所以此类中提供的dir均为模拟的虚拟目录。
 * 即/a/b/c实际上是一个key值，而非三级目录
 * </p>
 */
public class EtcdTemplate {

    private static final Logger log = LoggerFactory.getLogger(EtcdTemplate.class);

    private final String parentDir;
    private final EtcdClient etcdClient;

    public EtcdTemplate(EtcdClient etcdClient, String parentDir) {
        this.etcdClient = etcdClient;
        this.parentDir = parentDir;
    }

    public Client getClient() {
        return etcdClient.getClient();
    }

    public String getParentPath() {
        return parentDir;
    }

    /**
     * 获取指定key的值，并转换为对应的对象
     *
     * @param key   - 指定的key
     * @param clazz - 返回结果的类型
     * @return 返回转换后的对象，如果为null，则返回Optional.empty();
     */
    public <T> Optional<T> getValue(String key, Class<T> clazz) {
        Optional<String> value = getValue(key);
        if (!value.isPresent()) {
            return Optional.empty();
        }
        T object = JSON.parseObject(value.get(), clazz);
        return Optional.ofNullable(object);
    }

    /**
     * 获取指定Key的值
     *
     * @param key - 指定的key
     * @return 返回指定key的值，如果不存在，则返回Optional.empty();
     */
    public Optional<String> getValue(String key) {
        Client client = etcdClient.getClient();
        KV kvClient = client.getKVClient();
        try {
            GetResponse getResponse = kvClient.get(ByteSequenceUtil.fromString(fullPath(key)))
                    .get();
            if (getResponse.getKvs().isEmpty()) {
                return Optional.empty();
            }
            String value = ByteSequenceUtil.toStringUtf8(getResponse.getKvs().get(0).getValue());
            return Optional.ofNullable(value);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtcdException(e);
        } catch (ExecutionException e) {
            throw new EtcdException(e);
        }
    }

    /**
     * 列出所有指定目录下的数据
     *
     * @param dir         - 目录路径
     * @param isFilterDir - 是否过滤目录节点
     * @return 返回所有数据的列表
     */
    public List<EtcdDataDTO> listData(String dir, boolean isFilterDir) {
        final List<EtcdDataDTO> result = new ArrayList<>();
        this.forEach(dir, e -> {
            EtcdDataDTO dto = new EtcdDataDTO();
            dto.setKey(ByteSequenceUtil.toStringUtf8(e.getKey()));
            dto.setValue(ByteSequenceUtil.toStringUtf8(e.getValue()));
            dto.setTtl(e.getLease());
            result.add(dto);
        }, isFilterDir);
        return result;
    }

    /**
     * 遍历指定目录
     *
     * @param dir         - 目录路径
     * @param fun         - 遍历目录时执行的方法
     * @param isFilterDir - 是否过滤目录节点
     */
    public void forEach(String dir, Consumer<KeyValue> fun, boolean isFilterDir) {
        Client client = etcdClient.getClient();
        KV kvClient = client.getKVClient();
        try {
            GetOption getOption = GetOption.newBuilder().withPrefix(ByteSequenceUtil.fromString(fullPath(dir)))
                    .build();
            GetResponse getResponse = kvClient.get(
                            ByteSequenceUtil.fromString(fullPath(dir)),
                            getOption)
                    .get();
            if (getResponse.getKvs().isEmpty()) {
                return;
            }
            getResponse.getKvs().forEach(fun);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtcdException(e);
        } catch (ExecutionException e) {
            throw new EtcdException(e);
        }
    }

    /**
     * 创建节点
     *
     * @param key   - 键
     * @param value - 值
     */
    public void createNode(String key, String value) {
        Client client = etcdClient.getClient();
        try {
            client.getKVClient().put(
                    ByteSequenceUtil.fromString(fullPath(key)),
                    ByteSequenceUtil.fromString(value)
            ).get();
            log.info("Add node: {}", key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtcdException(e);
        } catch (ExecutionException e) {
            throw new EtcdException("Create ETCD node[" + key + "] error", e);
        }
    }

    /**
     * 创建节点
     *
     * @param key   - 键
     * @param value - 值
     * @param ttl   - 超时时间，单位秒
     * @return 返回租约ID
     */
    public long createNode(String key, String value, int ttl) {
        Client client = etcdClient.getClient();
        try {
            CompletableFuture<LeaseGrantResponse> grant = client.getLeaseClient().grant(ttl);
            PutOption putOption = PutOption.newBuilder().withLeaseId(grant.get().getID()).build();
            client.getKVClient().put(
                    ByteSequenceUtil.fromString(fullPath(key)),
                    ByteSequenceUtil.fromString(value),
                    putOption
            ).get();
            return grant.get().getID();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtcdException(e);
        } catch (ExecutionException e) {
            throw new EtcdException("Create ETCD node[" + key + "] error", e);
        }
    }

    /**
     * 如果指定节点是不存在的，则创建该节点
     *
     * @param key   - 节点名称
     * @param value - 值
     * @param ttl   - 超时时间
     * @return 如果创建成功，则返回true，否则返回false
     */
    public boolean createIfAbsent(String key, String value, int ttl) {
        Client client = etcdClient.getClient();
        try {
            ByteSequence keyByte = ByteSequenceUtil.fromString(fullPath(key));
            ByteSequence valueByte = ByteSequenceUtil.fromString(value);
            CompletableFuture<LeaseGrantResponse> grant = client.getLeaseClient().grant(ttl);
            PutOption putOption = PutOption.newBuilder()
                    .withLeaseId(grant.get().getID()).build();
            Txn txn = client.getKVClient().txn();
            // 在If中，"" (> = <) null = false、"" > any string = false，
            // 所以这里只能使用Else才能保证它是不存在时才做的操作
            TxnResponse txnResponse = txn.If(new Cmp(keyByte, Cmp.Op.GREATER,
                            CmpTarget.value(ByteSequenceUtil.fromString(""))))
                    .Else(Op.put(keyByte, valueByte, putOption))
                    .commit()
                    .get();
            boolean succeeded = txnResponse.isSucceeded();
            // 这里的SUCCESSDED对应的是If(Cmp ...)的判断条件，所以应该取反
            return !succeeded;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtcdException(e);
        } catch (ExecutionException e) {
            throw new EtcdException("Create ETCD node[" + key + "] error", e);
        }
    }

    /**
     * 对指定的租约进行一次续约
     *
     * @param leaseId 租约ID
     */
    void keepAliveOnce(long leaseId) {
        Client client = etcdClient.getClient();
        try {
            client.getLeaseClient().keepAliveOnce(leaseId).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtcdException(e);
        } catch (ExecutionException e) {
            throw new EtcdException(e);
        }
    }

    /**
     * 维持心跳（一次），并且如果指定的key不存在，则创建新的节点
     *
     * <p>
     * 此方法可能会抛出异常，如果调用它的方法在一个独立的线程中，
     * 那么请注意这个异常可能会导致线程中断，请自行处理
     * </p>
     *
     * @param key   Key
     * @param value Value
     * @param ttl   TTL
     * @return 如果指定的key不存在并且创建了新的节点，那么将返回新节点的leaseID，否则返回0
     */
    public long keepAliveOnceAndCreateIfAbsent(String key, String value, int ttl) {
        Client client = etcdClient.getClient();
        try {
            GetResponse getResponse = client.getKVClient()
                    .get(ByteSequenceUtil.fromString(fullPath(key)))
                    .get();
            List<KeyValue> kvs = getResponse.getKvs();
            // 如果不存在指定的key，则创建
            if (kvs.isEmpty()) {
                return this.createNode(key, value, ttl);
            }
            for (KeyValue kv : kvs) {
                long leaseId = kv.getLease();
                keepAliveOnce(leaseId);
            }
            return 0L;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtcdException(e);
        } catch (ExecutionException e) {
            throw new EtcdException(e);
        }
    }

    /**
     * 获取 TTL 的值
     *
     * @param leaseId 租约ID
     * @return 返回TTL
     */
    public long getTTL(long leaseId) {
        Client client = etcdClient.getClient();
        try {
            LeaseTimeToLiveResponse response =
                    client.getLeaseClient()
                            .timeToLive(leaseId, LeaseOption.DEFAULT)
                            .get();
            return response.getTTl();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtcdException(e);
        } catch (ExecutionException e) {
            throw new EtcdException(e);
        }

    }

    /**
     * 移除指定 key 的节点
     *
     * @param key 节点的名称
     */
    public void removeNode(String key) {
        Client client = etcdClient.getClient();
        String fullPath = fullPath(key);
        try {
            client.getKVClient().delete(ByteSequenceUtil.fromString(fullPath))
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtcdException(e);
        } catch (ExecutionException e) {
            throw new EtcdException("Remove ETCD node[" + fullPath + "] error", e);
        }
    }

    public boolean removeNodeIfNotMod(String key, String value) {
        Client client = etcdClient.getClient();
        KV kvClient = client.getKVClient();
        String fullPath = fullPath(key);
        ByteSequence keyByte = ByteSequenceUtil.fromString(fullPath);
        ByteSequence valueByte = ByteSequenceUtil.fromString(value);
        try {
            TxnResponse txnResponse = kvClient.txn()
                    .If(new Cmp(keyByte, Cmp.Op.EQUAL, CmpTarget.value(valueByte)))
                    .Then(Op.delete(keyByte, DeleteOption.DEFAULT))
                    .commit().get();
            return txnResponse.isSucceeded();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtcdException(e);
        } catch (ExecutionException e) {
            throw new EtcdException("Remove ETCD node[" + fullPath + "] error", e);
        }

    }

    /**
     * 在指定的节点上获取一个分布式共享锁
     * <p>
     * 如果该节点已经被锁住，那么重复调用时它会等待该节点被释放或者超时
     * </p>
     *
     * @param key - 节点名称
     * @param ttl - 超时时间，单位秒
     * @return 返回被锁住的key值，通过此值用于解锁
     */
    public String lock(String key, long ttl) {
        Client client = etcdClient.getClient();
        Lock lockClient = client.getLockClient();
        try {
            LeaseGrantResponse lease = client.getLeaseClient()
                    .grant(ttl)
                    .get();
            ByteSequence lockKey = lockClient.lock(ByteSequenceUtil.fromString(fullPath(key)), lease.getID())
                    .get()
                    .getKey();
            return ByteSequenceUtil.toStringUtf8(lockKey);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtcdException(e);
        } catch (ExecutionException e) {
            throw new EtcdException(e);
        }
    }

    /**
     * 释放分布式共享锁
     *
     * <p>注意：此方法不会自动补充环境前缀，因为你必须使用 lock() 方法返回的 key 才能解锁</p>
     *
     * @param lockKey - 通过 Lock方法得到的锁所有权key
     */
    public void unlock(String lockKey) {
        Client client = etcdClient.getClient();
        Lock lockClient = client.getLockClient();
        try {
            lockClient.unlock(ByteSequenceUtil.fromString(lockKey))
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtcdException(e);
        } catch (ExecutionException e) {
            throw new EtcdException(e);
        }
    }

    /**
     * 监听指定前缀的节点
     *
     * @param prefix   前缀
     * @param listener 监听器
     */
    public void watchForPrefix(String prefix, Watch.Listener listener) {
        Client client = etcdClient.getClient();
        WatchOption watchOption = WatchOption.newBuilder()
                .withPrefix(ByteSequenceUtil.fromString(fullPath(prefix)))
                .build();
        Watch watchClient = client.getWatchClient();
        watchClient.watch(ByteSequenceUtil.fromString(fullPath(prefix)),
                watchOption, listener);
    }

    public String fullPath(String key) {
        if (StringUtils.isEmpty(parentDir)) {
            return key;
        }
        if (parentDir.endsWith("/") && key.startsWith("/")) {
            return parentDir + key.substring(1);
        }
        if (!parentDir.endsWith("/") && !key.startsWith("/")) {
            return parentDir + "/" + key;
        }
        return parentDir + key;
    }

}