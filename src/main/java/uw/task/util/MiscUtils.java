package uw.task.util;

public class MiscUtils {

	/**
	 * 打印异常信息，屏蔽掉spring自己的堆栈输出。
	 *
	 * @param e
	 *            需要打印的异常信息
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
