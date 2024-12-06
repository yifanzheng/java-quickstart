package top.yifan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 可重试的数据库连接装饰器类。值得注意的是，在执行SQL方法内必须抛出SQLException才会进行重试，
 * 所以请不要将其转译为其它异常。
 *
 * @author kt94
 */
public class RetryableConnection implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(RetryableConnection.class);

    private static final int MIN_RETRIES = 3;
    private static final int INIT_INTERVAL_TIME = 2 * 1000;
    private static final int MAX_INTERVAL_TIME = 60 * 1000;
    private static final int INCREMENT_INTERVAL_TIME = 3 * 1000;

    private Connection conn;
    private GetConnectionSupplier getConnection;
    private Interruptable interruptable;

    /**
     * @param getConnection - 获取连接的方法
     */
    public RetryableConnection(GetConnectionSupplier getConnection) {
        this.getConnection = getConnection;
    }

    public RetryableConnection(GetConnectionSupplier getConnection, Interruptable interruptable) {
        this.getConnection = getConnection;
        this.interruptable = interruptable;
    }

    public <R> R execute(ExecuteSQLFunction<R> fun, int numRetries)
            throws SQLException, InterruptedException {
        return this.execute(fun, numRetries, -1);
    }

    /**
     * 执行SQL语句，当发生可重试异常时，将自动重试指定的次数
     *
     * @param fun          - 具体的执行方法
     * @param numRetries   - 最大的重试次数
     * @param intervalTime - 重试的间隔时间，单位毫秒，如果小于0，则使用2的幂次方递增：2 -> 4 -> 8 -> 16 ...
     * @return 返回执行方法中的返回值
     * @throws SQLException - 如果执行的过程成发生任何SQL异常，则将抛出此异常
     */
    public <R> R execute(ExecuteSQLFunction<R> fun, int numRetries, int intervalTime)
            throws SQLException, InterruptedException {
        int times = 0;
        long sleepTime = intervalTime <= 0 ? INIT_INTERVAL_TIME : intervalTime;
        for (; ; ) {
            try {
                if (conn == null || conn.isClosed()) {
                    conn = getConnection.get();
                }
                return fun.apply(conn);
            } catch (SQLException e) {
                if (isInterrupted(fun)) {
                    throw e;
                }
                int minTimes = Math.min(MIN_RETRIES, numRetries);
                if (intervalTime <= 0) {
                    sleepTime = Math.min(sleepTime * 2, MAX_INTERVAL_TIME);
                } else {
                    sleepTime = Math.min(intervalTime + INCREMENT_INTERVAL_TIME * times, MAX_INTERVAL_TIME);
                }
                if (times < numRetries && (canRetry(fun, e, times) || times < minTimes)) {
                    times++;
                    if (conn == null || conn.isClosed()) {
                        log.warn("Execute SQL statement error[{}], and attempt={}/({} | {}), sleepTime={}ms" +
                                        "and detail: {}",
                                e.getErrorCode(), times, numRetries, MIN_RETRIES, sleepTime, e.getMessage());
                    } else {
                        log.warn("Execute SQL statement error[{}], and attempt={}/({} | {}), sleepTime={}ms, url={}" +
                                        "and detail: {}",
                                e.getErrorCode(), times, numRetries, MIN_RETRIES, sleepTime, conn.getMetaData().getURL(), e.getMessage());
                    }

                    Thread.sleep(sleepTime);
                    // 睡眠完成后需要再次检测是否已经中断，因为在睡眠期间可能发生中断操作
                    if (isInterrupted(fun)) {
                        throw e;
                    }
                    continue;
                }
                log.warn("SQLException error: {}", e.getErrorCode());
                throw e;
            }
        }
    }

    private boolean isInterrupted(ExecuteSQLFunction<?> fun) {
        // Block_1：中断重试
        if (interruptable != null && interruptable.isInterrupted()) {
            return true;
        }
        // 功能同 Block_1, 推荐使用上面这种方式
        return fun.isInterrupted();
    }

    private boolean canRetry(ExecuteSQLFunction<?> fun, SQLException e, int times) {
        if (fun.isKeepRetryWithOutDefaultRetry(e, times)) {
            return true;
        }
        // TODO 检测状态码是否属于可重试的范围
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        if (message.contains("Connection is not available")) {
            return true;
        }
        return message.trim().startsWith("Connection reset");
    }

    @Override
    public void close() {
        DBUtil.closeDBResources(conn);
    }

}
