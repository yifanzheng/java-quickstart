package top.yifan;

/**
 * 不支持的类型
 * 
 * @author Star Zheng
 *
 */
public class UnsupportedTypeException extends RuntimeException {
    
    private static final long serialVersionUID = -4097026919704045421L;

    public UnsupportedTypeException(String msg) {
        super(msg);
    }
    
}
