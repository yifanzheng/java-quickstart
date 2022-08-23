package top.yifan;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ExecuteSQLFunction
 * 
 * @author Star Zheng
 *
 * @param <R> 返回值类型
 */
@FunctionalInterface
public interface ExecuteSQLFunction<R> {
    
    R apply(Connection t) throws SQLException;
    
    /**
     * 是否中断执行，推荐使用 Interruptable接口
     */
    default boolean isInterrupted() { return false;}

    /**
     * 无法进行默认重试时，是否继续重试
     *
     * @param e SQL异常
     * @param times 重试次数
     * @return 返回true则继续重试，返回false则依据默认的规则
     */
    default boolean isKeepRetryWithOutDefaultRetry(SQLException e, int times) { return false; }
    
}
