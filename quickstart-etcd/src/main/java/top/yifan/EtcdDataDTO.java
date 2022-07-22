package top.yifan;

/**
 * EtcdDataDTO
 * 
 * @author Star Zheng
 */
public class EtcdDataDTO {
    
    private String path;
    
    private String key;
    
    private String value;
    
    private Long ttl;

    private boolean dir;
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public boolean isDir() {
        return dir;
    }

    public void setDir(boolean dir) {
        this.dir = dir;
    }
    
}
