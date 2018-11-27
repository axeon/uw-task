package uw.task.util;

import org.springframework.cglib.beans.BeanCopier;
import uw.task.TaskData;

public class MiscUtils {

    /**
     * taskData的copy器。
     */
    private static BeanCopier beanCopier = BeanCopier.create(TaskData.class, TaskData.class, false);

    /**
     * copy taskData对象。
     *
     * @param srcData
     * @param dstData
     */
    public static void copyTaskData(TaskData srcData, TaskData dstData) {
        beanCopier.copy(srcData, dstData, null);
    }

    /**
     * 打印异常信息，屏蔽掉spring自己的堆栈输出。
     *
     * @param e 需要打印的异常信息
     * @return
     */
    public static String exceptionToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");

        StackTraceElement[] trace = e.getStackTrace();
        for (StackTraceElement traceElement : trace) {
            if (traceElement.getClassName().startsWith("org.spring")) {
                continue;
            }
            if (traceElement.getClassName().startsWith("sun.")) {
                continue;
            }
            sb.append("\tat ").append(traceElement).append("\n");
        }
        Throwable ourCause = e.getCause();
        if (ourCause != null) {
            sb.append("CAUSE BY").append(ourCause.toString()).append("\n");
            trace = ourCause.getStackTrace();
            for (StackTraceElement traceElement : trace) {
                if (traceElement.getClassName().startsWith("org.spring")) {
                    continue;
                }
                if (traceElement.getClassName().startsWith("sun.")) {
                    continue;
                }
                sb.append("\tat ").append(traceElement).append("\n");
            }
        }
        return sb.toString();
    }

}
