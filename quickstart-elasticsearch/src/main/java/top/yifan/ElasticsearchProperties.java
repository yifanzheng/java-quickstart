package top.yifan;

public class ElasticsearchProperties {
    
    private String hosts = "127.0.0.1";
    
    private Integer port = 9200;
    
    private String protocol = "http";

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
}
