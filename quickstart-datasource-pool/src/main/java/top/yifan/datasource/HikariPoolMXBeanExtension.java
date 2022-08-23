package top.yifan.datasource;

import com.zaxxer.hikari.HikariPoolMXBean;

/**
 * HikariPoolMXBeanExtension
 *
 * @author Star Zheng
 */
public class HikariPoolMXBeanExtension {

    private String jdbc;

    private String username;

    private HikariPoolMXBean mxBean;

    public String getJdbc() {
        return jdbc;
    }

    public void setJdbc(String jdbc) {
        this.jdbc = jdbc;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public HikariPoolMXBean getMxBean() {
        return mxBean;
    }

    public void setMxBean(HikariPoolMXBean mxBean) {
        this.mxBean = mxBean;
    }
}
