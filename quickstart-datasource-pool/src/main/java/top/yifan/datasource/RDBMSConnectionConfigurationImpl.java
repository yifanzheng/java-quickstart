package top.yifan.datasource;

/**
 * RDBMSConnectionConfigurationImpl
 *
 * @author Star Zheng
 */
public class RDBMSConnectionConfigurationImpl implements RDBMSConnectionConfiguration {

    private RDBMSType type;         // DB 类型
    private String server;   // 服务器地址
    private String port;     // 端口号
    private String database;        // 数据库名称
    private String username;        // 用户名
    private String password;        // 密码

    protected RDBMSConnectionConfigurationImpl() {
    }

    @Override
    public RDBMSType getType() {
        return type;
    }

    public void setType(RDBMSType type) {
        this.type = type;
    }

    @Override
    public String getServer() {
        return server;
    }

    private void setServer(String server) {
        this.server = server;
    }

    @Override
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static class Builder {

        private RDBMSType type;
        private String server;
        private String database;
        private String username;
        private String password;

        public RDBMSConnectionConfiguration build() {
            RDBMSConnectionConfigurationImpl config = new RDBMSConnectionConfigurationImpl();
            config.setType(type);
            config.setServer(server);
            config.setDatabase(database);
            config.setUsername(username);
            config.setPassword(password);
            return config;
        }

        public Builder type(RDBMSType type) {
            this.type = type;
            return this;
        }

        public Builder server(String server) {
            this.server = server;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

    }

}
