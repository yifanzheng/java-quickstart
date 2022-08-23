package top.yifan.datasource;

/**
 * @author sz7v
 */
public class SQLServerDataSourcePool extends AbstractRDBMSDataSourcePool {

    @Override
    protected String generateJdbcUrl(RDBMSConnectionConfiguration connectionConfig) {
        return String.format("jdbc:sqlserver://%s:%s;database=%s;",
                connectionConfig.getServer(), connectionConfig.getPort(), connectionConfig.getDatabase());
    }

    @Override
    protected String getDriverName() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    static AbstractRDBMSDataSourcePool getInstance() {
        return SQLServerDataSourcePool.SQLServerDataSourcePoolHolder.INSTANCE;
    }


    private static class SQLServerDataSourcePoolHolder {
        private static final AbstractRDBMSDataSourcePool INSTANCE = new SQLServerDataSourcePool();
    }
}
