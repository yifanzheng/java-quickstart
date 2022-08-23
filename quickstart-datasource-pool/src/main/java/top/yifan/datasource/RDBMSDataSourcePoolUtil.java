package top.yifan.datasource;


import com.zaxxer.hikari.HikariPoolMXBean;
import top.yifan.DBConnectionPoolMXBeanDTO;
import top.yifan.Interruptable;
import top.yifan.RetryableConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RDBMSDataSourcePoolUtil
 *
 * @author Star Zheng
 */
public class RDBMSDataSourcePoolUtil {

    static {
        AbstractRDBMSDataSourcePool.registerInstance(RDBMSType.MySQL, MySQLDataSourcePool.getInstance());
        AbstractRDBMSDataSourcePool.registerInstance(RDBMSType.SQLServer, SQLServerDataSourcePool.getInstance());
    }

    private RDBMSDataSourcePoolUtil() {}

    /**
     * 创建并返回一个支持重试的数据库连接
     */
    public static RetryableConnection getRetryableConnection(RDBMSConnectionConfiguration config) {
        return new RetryableConnection(() -> AbstractRDBMSDataSourcePool.findInstance(config.getType()).getConnection(config));
    }

    /**
     * 创建并返回一个支持重试且可自定义中断的数据库连接
     */
    public static RetryableConnection getRetryableConnection(RDBMSConnectionConfiguration config, Interruptable interruptable) {
        return new RetryableConnection(() -> AbstractRDBMSDataSourcePool.findInstance(config.getType()).getConnection(config), interruptable);
    }

    /**
     * 创建并返回一个普通的数据库连接
     */
    public static Connection getConnection(RDBMSConnectionConfiguration config)
            throws SQLException {
        return AbstractRDBMSDataSourcePool.findInstance(config.getType()).getConnection(config);
    }

    /**
     * 创建并返回一个不会被缓存在池中的普通的数据库连接
     */
    public static Connection getConnectionWithNoPool(RDBMSConnectionConfiguration config)
            throws SQLException {
        return AbstractRDBMSDataSourcePool.findInstance(config.getType()).getConnectionWithNoPool(config);
    }

    public static List<DBConnectionPoolMXBeanDTO> listAllPoolMXBeans() {
        Set<HikariPoolMXBeanExtension> mxBeans = AbstractRDBMSDataSourcePool.listAllPoolMXBeans();
        return mxBeans.stream().map(e -> {
            DBConnectionPoolMXBeanDTO dto = new DBConnectionPoolMXBeanDTO();
            dto.setJdbc(e.getJdbc());
            dto.setUsername(e.getUsername());
            HikariPoolMXBean mxBean = e.getMxBean();
            if (mxBean != null) {
                dto.setActiveConnections(mxBean.getActiveConnections());
                dto.setIdleConnections(mxBean.getIdleConnections());
                dto.setThreadsAwaitingConnection(mxBean.getThreadsAwaitingConnection());
                dto.setTotalConnections(mxBean.getTotalConnections());
            }
            return dto;
        }).collect(Collectors.toList());
    }

}
