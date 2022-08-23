package top.yifan;

/**
 * RDBMSConnectionConfiguration
 *
 * @author Star Zheng
 */
public interface RDBMSConnectionConfiguration {

    RDBMSType getType();

    String getServer();

    String getPort();

    String getDatabase();

    String getUsername();

    String getPassword();

    static RDBMSConnectionConfigurationImpl.Builder builder() {
        return new RDBMSConnectionConfigurationImpl.Builder();
    }

}
