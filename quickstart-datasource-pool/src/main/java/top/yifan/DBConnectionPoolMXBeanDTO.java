package top.yifan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DBConnectionPoolMXBeanDTO
 *
 * @author Star Zheng
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DBConnectionPoolMXBeanDTO {

    private String jdbc;

    private String username;

    private Integer idleConnections;

    private Integer activeConnections;

    private Integer totalConnections;

    private Integer threadsAwaitingConnection;

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

    public Integer getIdleConnections() {
        return idleConnections;
    }

    public void setIdleConnections(Integer idleConnections) {
        this.idleConnections = idleConnections;
    }

    public Integer getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(Integer activeConnections) {
        this.activeConnections = activeConnections;
    }

    public Integer getTotalConnections() {
        return totalConnections;
    }

    public void setTotalConnections(Integer totalConnections) {
        this.totalConnections = totalConnections;
    }

    public Integer getThreadsAwaitingConnection() {
        return threadsAwaitingConnection;
    }

    public void setThreadsAwaitingConnection(Integer threadsAwaitingConnection) {
        this.threadsAwaitingConnection = threadsAwaitingConnection;
    }

}
