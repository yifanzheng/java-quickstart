package top.yifan;

/**
 * RDBMSDriverLoader
 *
 * @author Star Zheng
 */
public class RDBMSDriverLoader {

    private RDBMSDriverLoader() {

    }

    /**
     * 串行加载驱动器，以防止死锁
     *
     * @param driver 驱动器路径
     */
    public static synchronized void loadDriver(String driver) {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new DataException(e.getMessage());
        }
    }

}
