package top.yifan.exception;

public class ZipException extends RuntimeException {

    private static final long serialVersionUID = -9004968315042742895L;

    public ZipException(String message) {
        super(message);
    }

    public ZipException(String message, Throwable cause) {
        super(message, cause);
    }
}