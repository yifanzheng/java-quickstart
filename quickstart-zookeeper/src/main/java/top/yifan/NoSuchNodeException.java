package top.yifan;

/**
 * NoSuchNodeException
 *
 * @author Star Zheng
 */
public class NoSuchNodeException extends RuntimeException {

    private static final long serialVersionUID = -5659584056093927091L;

    public NoSuchNodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
