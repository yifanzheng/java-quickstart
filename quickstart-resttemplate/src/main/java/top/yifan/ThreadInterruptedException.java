package top.yifan;

/**
 * 主要用于将InterruptedException转换为非受检的异常
 *
 * @author Star Zheng
 */
public class ThreadInterruptedException extends RuntimeException {

    public ThreadInterruptedException(InterruptedException exception) {
        super(exception);
    }

}