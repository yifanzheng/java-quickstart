package top.yifan.lock;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.support.CloseableClient;
import io.grpc.stub.StreamObserver;
import top.yifan.ByteSequenceUtil;
import top.yifan.client.EtcdClient;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class EtcdDistributeLock {

    private EtcdClient etcdClient;

    private CloseableClient keepAliveClient;

    public EtcdDistributeLock(EtcdClient client) {
        this.etcdClient = client;
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
        Client client = this.etcdClient.getClient();
        try {
            Lease leaseClient = client.getLeaseClient();
            long leaseId = leaseClient.grant(ttl).get().getID();
            // 持续续约，防止锁到期了，业务代码还没有执行完成
            keepAliveClient = leaseClient.keepAlive(leaseId, new LockStreamObserver());
            // 进行加锁
            ByteSequence lockKey = client.getLockClient()
                    .lock(ByteSequenceUtil.fromString(key), leaseId)
                    .get().getKey();
            return lockKey.toString(StandardCharsets.UTF_8);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 释放分布式共享锁
     *
     * @param lockKey 通过lock方法得到的锁key
     */
    public void unlock(String lockKey) {
        Client client = this.etcdClient.getClient();
        try {
            // 关闭续约客户端，停止续约
            if (keepAliveClient != null) {
                keepAliveClient.close();
            }
            // 释放锁
            Lock lockClient = client.getLockClient();
            lockClient.unlock(ByteSequenceUtil.fromString(lockKey)).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private class LockStreamObserver implements StreamObserver<LeaseKeepAliveResponse> {
        @Override
        public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {

        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onCompleted() {

        }
    }

}