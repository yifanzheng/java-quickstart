package top.yifan.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import com.zaxxer.hikari.pool.HikariPool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yifan.MD5Util;
import top.yifan.RDBMSDriverLoader;
import top.yifan.UnsupportedTypeException;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象关系型数据库数据源池
 *
 * @author Star Zheng
 */
public abstract class AbstractRDBMSDataSourcePool {

    private final Logger log = LoggerFactory.getLogger(AbstractRDBMSDataSourcePool.class);

    private static final ConcurrentHashMap<RDBMSType, AbstractRDBMSDataSourcePool> ACTUAL_DATA_SOURCE_POOL = new ConcurrentHashMap<>();
    private static final Set<HikariPoolMXBeanExtension> MX_BEANS = ConcurrentHashMap.newKeySet();

    /**
     * 缓存不同环境的连接池容器，Map<环境标识, 连接池>
     */
    private static final ConcurrentHashMap<String, HikariDataSource> DATA_SOURCES = new ConcurrentHashMap<>();
    /**
     * 创建数据源时的锁，防止多线程同时创建导致死锁
     */
    private static final ConcurrentHashMap<String, Object> CREATE_DATA_SOURCE_LOCKS = new ConcurrentHashMap<>();

    private volatile boolean isLoadDBDriver = false;

    AbstractRDBMSDataSourcePool() {
        this.loadDriver();
    }

    /**
     * 根据配置获取一个连接，这个连接将被缓存在连接池中
     *
     * @param config - 数据库连接配置
     * @return 返回创建的连接
     * @throws SQLException 如果在创建连接池的过程中发生了PoolInitializationException异常，
     *                      则将其转译为此异常抛出，如果发生其它SQLException，则原样抛出
     */
    Connection getConnection(RDBMSConnectionConfiguration config) throws SQLException {
        this.checkConnectionParams(config);

        String mapKey = generateCacheKey(config);
        HikariDataSource dataSource = DATA_SOURCES.get(mapKey);
        if (dataSource == null) {
            try {
                this.addDataSourceIfAbsent(mapKey, config);
            } catch (HikariPool.PoolInitializationException e) {
                throw new SQLException(e);
            }
            dataSource = DATA_SOURCES.get(mapKey);
        }
        return dataSource.getConnection();
    }

    /**
     * 根据配置获取一个连接，这个连接不会被缓存在连接池或任何地方
     *
     * @param config - 数据库连接配置
     * @return 返回创建的连接
     * @throws SQLException 如果发生SQLException，则原样抛出
     */
    Connection getConnectionWithNoPool(RDBMSConnectionConfiguration config) throws SQLException {
        this.checkConnectionParams(config);

        String jdbc = this.generateJdbcUrlWithConfig(config);
        String username = config.getUsername();
        String password = config.getPassword();
        return DriverManager.getConnection(jdbc, username, password);
    }

    private void checkConnectionParams(RDBMSConnectionConfiguration config) {
        Objects.requireNonNull(config.getServer(), "Server is required");
        Objects.requireNonNull(config.getDatabase(), "Database is required");
        Objects.requireNonNull(config.getUsername(), "Username is required");
        Objects.requireNonNull(config.getPassword(), "Password is required");
    }

    static Set<HikariPoolMXBeanExtension> listAllPoolMXBeans() {
        return MX_BEANS;
    }

    private void loadDriver() {
        if (isLoadDBDriver) {
            return;
        }
        synchronized (this) {
            if (isLoadDBDriver) {
                return;
            }
            RDBMSDriverLoader.loadDriver(getDriverName());
            isLoadDBDriver = true;
        }
    }

    private String generateCacheKey(RDBMSConnectionConfiguration config) {
        String server = config.getServer();
        String database = config.getDatabase();
        String username = config.getUsername();
        String password = config.getPassword();

        server = server.trim();
        database = database.trim();
        username = username.trim();

        String sourceKey = server + database + username + password;
        return MD5Util.toMD5(sourceKey);
    }

    private void addDataSourceIfAbsent(String mapKey, RDBMSConnectionConfiguration config) {
        if (DATA_SOURCES.containsKey(mapKey)) {
            return;
        }
        String jdbc = this.generateJdbcUrlWithConfig(config);
        String password = config.getPassword();

        // 构建连接池配置对象
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbc);
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(password);

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");                 // 是否自定义配置
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");               // 连接池默认大小，官方推荐 250 - 500
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");          // 单条语句最大长度，默认256，官方推荐2048
        hikariConfig.addDataSourceProperty("sendStringParametersAsUnicode", false);   // 是否使用Unicode编码替换字符串格式
        hikariConfig.addDataSourceProperty("connectionTimeout", "90000");             // 从池中获取连接的最大等待时间，超时抛异常，默认值30s
        hikariConfig.addDataSourceProperty("verifyServerCertificate", false);
        hikariConfig.addDataSourceProperty("allowPublicKeyRetrieval", true);
        hikariConfig.addDataSourceProperty("useSSL", false);

        hikariConfig.setConnectionTimeout(90 * 1000L); // 从连接池获取连接的超时时间：90s
        hikariConfig.setMaxLifetime(15 * 60 * 1000L);  // 最大的生命周期：15min
        hikariConfig.setMinimumIdle(0);                // 最小的空闲连接数
        hikariConfig.setMaximumPoolSize(20);           // 允许的最大连接数
        hikariConfig.setIdleTimeout(10 * 60 * 1000L);  // 空闲连接的超时时间：10min
        hikariConfig.setRegisterMbeans(true);
        CREATE_DATA_SOURCE_LOCKS.putIfAbsent(mapKey, new Object());
        synchronized (CREATE_DATA_SOURCE_LOCKS.get(mapKey)) {
            if (DATA_SOURCES.containsKey(mapKey)) {
                return;
            }
            // 创建新的连接池
            HikariDataSource newDataSource = new HikariDataSource(hikariConfig);
            HikariDataSource oldDataSource = DATA_SOURCES.putIfAbsent(mapKey, newDataSource);
            if (oldDataSource != null) {
                newDataSource.close();
                return;
            }
            createMXBean(newDataSource);
        }
        log.info("New DataSource -> Server: {}, DB: {}, User: {}",
                config.getServer(), config.getDatabase(),
                config.getUsername());
    }


    private void createMXBean(HikariDataSource newDataSource) {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (" + newDataSource.getPoolName() + ")");
            HikariPoolMXBean poolProxy = JMX.newMBeanProxy(mBeanServer, poolName, HikariPoolMXBean.class);
            HikariPoolMXBeanExtension mxBeanExtension = new HikariPoolMXBeanExtension();
            mxBeanExtension.setMxBean(poolProxy);
            mxBeanExtension.setJdbc(newDataSource.getJdbcUrl());
            mxBeanExtension.setUsername(newDataSource.getUsername());
            MX_BEANS.add(mxBeanExtension);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    private String generateJdbcUrlWithConfig(RDBMSConnectionConfiguration connectionConfig) {
        String server = connectionConfig.getServer();
        if (StringUtils.isBlank(server)) {
            throw new IllegalArgumentException("Server is required");
        }
        // 如果server本身就是IP地址，那么直接生成JDBC URL即可
        String ipAddress = server.split(":")[0].trim();
        if (validIP(ipAddress)) {
            return this.generateJdbcUrl(connectionConfig);
        }

        throw new IllegalArgumentException("Server is invalid.");
    }

    protected abstract String generateJdbcUrl(RDBMSConnectionConfiguration connectionConfig);

    protected abstract String getDriverName();

    static void registerInstance(RDBMSType type, AbstractRDBMSDataSourcePool pool) {
        ACTUAL_DATA_SOURCE_POOL.putIfAbsent(type, pool);
    }

    static AbstractRDBMSDataSourcePool findInstance(RDBMSType type) {
        if (type == null) {
            throw new UnsupportedTypeException("Unsupported DB type: null");
        }
        AbstractRDBMSDataSourcePool pool = ACTUAL_DATA_SOURCE_POOL.get(type);
        if (pool == null) {
            throw new UnsupportedTypeException("Unsupported DB type: " + type);
        }
        return pool;
    }

    public boolean validIP(String ip) {
        try {
            if (StringUtils.isBlank(ip)) {
                return false;
            }
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }
            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }
            return !ip.endsWith(".");
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
