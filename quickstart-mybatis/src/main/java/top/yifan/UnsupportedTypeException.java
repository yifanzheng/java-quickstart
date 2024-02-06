package top.yifan;

/**
 * 不支持的类型
 */
public class UnsupportedTypeException extends RuntimeException {

    private static final long serialVersionUID = 4480900385389409890L;

    public UnsupportedTypeException(String msg) {
        super(msg);
    }
    
}