package top.yifan;

/**
 * EtcdException
 *
 * @author Star Zheng
 */
public class EtcdException extends RuntimeException {

    private static final long serialVersionUID = -7538064052587147790L;

    public EtcdException(Throwable cause) {
        super(cause);
    }

    public EtcdException(String message, Throwable cause) {
        super(message, cause);
    }
}
