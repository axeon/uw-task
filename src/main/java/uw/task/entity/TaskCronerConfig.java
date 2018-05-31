package uw.task.entity;

import java.io.Serializable;

/**
 * taskCronerConfig实体类。
 *
 * @author axeon
 * @version $Revision: 1.00 $ $Date: 2017-05-03 16:51:06
 */
public class TaskCronerConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 直接运行模式。
	 */
	public static final int RUN_TYPE_ANYWAY = 0;

	/**
	 * 运行在全局单例模式下。
	 */
	public static final int RUN_TYPE_SINGLETON = 1;

    /**
     * 什么都不记录
     */
    public static final int TASK_LOG_TYPE_NONE = -1;

    /**
     * 记录日志
     */
    public static final int TASK_LOG_TYPE_RECORD = 0;

    /**
     * 记录日志,含请求参数
     */
    public static final int TASK_LOG_TYPE_RECORD_TASK_PARAM = 1;

    /**
     * 记录日志,含返回参数
     */
    public static final int TASK_LOG_TYPE_RECORD_RESULT_DATA = 2;

    /**
     * 记录全部日志
     */
    public static final int TASK_LOG_TYPE_RECORD_ALL = 3;

	private long id;

	/**
	 * 执行类信息
	 */
	private String taskClass;

	/**
	 * 执行参数，可能用于区分子任务
	 */
	private String taskParam;

	/**
	 * 任务名称
	 */
	private String taskName;

	/**
	 * 任务描述
	 */
	private String taskDesc;

	/**
	 * cron表达式，默认5秒一次。
	 */
	private String taskCron = "*/5 * * * * ?";

	/**
	 * 0随意执行，1全局唯一执行
	 */
	private int runType = RUN_TYPE_SINGLETON;

	/**
	 * 运行目标，默认不指定
	 */
	private String runTarget = "";

	/**
	 * 失败率
	 */
	private int failRate;

	/**
	 * 接口失败率
	 */
	private int failPartnerRate;

	/**
	 * 程序失败率
	 */
	private int failProgramRate;

	/**
	 * 数据失败率
	 */
	private int failDataRate;

	/**
	 * 等待超时
	 */
	private int waitTimeout;

	/**
	 * 运行超时
	 */
	private int runTimeout;

	/**
	 * 状态值
	 */
	private int state = 1;

    /**
     * 详见 TaskLogObjectAsStringSerializer 日志类型说明
     */
    private int logType = TASK_LOG_TYPE_RECORD;

    /**
     * 日志字符串字段大小限制: 0 表示无限制
     */
    private int logLimitSize = 0;

	public TaskCronerConfig() {
	}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTaskClass() {
        return taskClass;
    }

    public void setTaskClass(String taskClass) {
        this.taskClass = taskClass;
    }

    public String getTaskParam() {
        return taskParam;
    }

    public void setTaskParam(String taskParam) {
        this.taskParam = taskParam;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public String getTaskCron() {
        return taskCron;
    }

    public void setTaskCron(String taskCron) {
        this.taskCron = taskCron;
    }

    public int getRunType() {
        return runType;
    }

    public void setRunType(int runType) {
        this.runType = runType;
    }

    public String getRunTarget() {
        return runTarget;
    }

    public void setRunTarget(String runTarget) {
        this.runTarget = runTarget;
    }

    public int getFailRate() {
        return failRate;
    }

    public void setFailRate(int failRate) {
        this.failRate = failRate;
    }

    public int getFailPartnerRate() {
        return failPartnerRate;
    }

    public void setFailPartnerRate(int failPartnerRate) {
        this.failPartnerRate = failPartnerRate;
    }

    public int getFailProgramRate() {
        return failProgramRate;
    }

    public void setFailProgramRate(int failProgramRate) {
        this.failProgramRate = failProgramRate;
    }

    public int getFailDataRate() {
        return failDataRate;
    }

    public void setFailDataRate(int failDataRate) {
        this.failDataRate = failDataRate;
    }

    public int getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(int waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public int getRunTimeout() {
        return runTimeout;
    }

    public void setRunTimeout(int runTimeout) {
        this.runTimeout = runTimeout;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getLogType() {
        return logType;
    }

    public void setLogType(int logType) {
        this.logType = logType;
    }

    public int getLogLimitSize() {
        return logLimitSize;
    }

    public void setLogLimitSize(int logLimitSize) {
        this.logLimitSize = logLimitSize;
    }
}
