package top.yifan;

/**
 * MySQLDataSourcePool
 *
 * @author Star Zheng
 */
public class MySQLDataSourcePool extends AbstractRDBMSDataSourcePool {

    @Override
    protected String generateJdbcUrl(RDBMSConnectionConfiguration connectionConfig) {
        return String.format("jdbc:mysql://%s:%s/%s?characterEncoding=utf-8&useUnicode=true&autoReconnect=true" +
                        "&failOverReadOnly=false&rewriteBatchedStatements=true&serverTimezone=GMT-8",
                connectionConfig.getServer(), connectionConfig.getPort(), connectionConfig.getDatabase());
    }

    @Override
    protected String getDriverName() {
        return "com.mysql.cj.jdbc.Driver";
    }

    static AbstractRDBMSDataSourcePool getInstance() {
        return MySQLDataSourcePoolHolder.INSTANCE;
    }

    private static class MySQLDataSourcePoolHolder {
        private static final AbstractRDBMSDataSourcePool INSTANCE = new MySQLDataSourcePool();
    }

}
