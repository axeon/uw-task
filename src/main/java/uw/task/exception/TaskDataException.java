package uw.task.exception;

/**
 * 数据异常，用于抛出各种数据错误的信息。
 *
 * @author axeon
 */
public class TaskDataException extends Exception {

    /**
     * <code>serialVersionUID</code> 的注释
     */
    private static final long serialVersionUID = 8713460933603499992L;

    public TaskDataException() {
        super();
    }

    public TaskDataException(String msg) {
        super(msg);
    }

    public TaskDataException(Throwable nestedThrowable) {
        super(nestedThrowable);
    }

    public TaskDataException(String msg, Throwable nestedThrowable) {
        super(msg, nestedThrowable);
    }

}