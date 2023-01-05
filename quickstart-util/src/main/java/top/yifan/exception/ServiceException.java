package top.yifan.exception;

/**
 * @author Star Zheng
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 6002588936061538948L;

    public ServiceException(String message) {
        super(message);
    }
}
