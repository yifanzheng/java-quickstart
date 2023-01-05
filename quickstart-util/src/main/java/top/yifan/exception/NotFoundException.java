package top.yifan.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

/**
 * NotFoundException
 *
 * @author Star Zheng
 */
public class NotFoundException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 6463733466124171747L;

    public NotFoundException(String message) {
        super(null, message, Status.NOT_FOUND);
    }
}
