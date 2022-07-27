package top.yifan;

/**
 * ServiceException
 *
 * @author Star Zheng
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
