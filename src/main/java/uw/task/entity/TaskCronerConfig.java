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

	public TaskCronerConfig() {
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the taskName
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * @param taskName
	 *            the taskName to set
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * @return the taskTag
	 */
	public String getTaskParam() {
		return taskParam;
	}

	/**
	 * @param taskTag
	 *            the taskTag to set
	 */
	public void setTaskParam(String taskTag) {
		this.taskParam = taskTag;
	}

	/**
	 * @return the taskDesc
	 */
	public String getTaskDesc() {
		return taskDesc;
	}

	/**
	 * @param taskDesc
	 *            the taskDesc to set
	 */
	public void setTaskDesc(String taskDesc) {
		this.taskDesc = taskDesc;
	}

	/**
	 * @return the taskClass
	 */
	public String getTaskClass() {
		return taskClass;
	}

	/**
	 * @param taskClass
	 *            the taskClass to set
	 */
	public void setTaskClass(String taskClass) {
		this.taskClass = taskClass;
	}

	/**
	 * @return the cron
	 */
	public String getTaskCron() {
		return taskCron;
	}

	/**
	 * @param cron
	 *            the cron to set
	 */
	public void setTaskCron(String cron) {
		this.taskCron = cron;
	}

	/**
	 * @return the runType
	 */
	public int getRunType() {
		return runType;
	}

	/**
	 * @param runType
	 *            the runType to set
	 */
	public void setRunType(int runType) {
		this.runType = runType;
	}

	/**
	 * @return the runTarget
	 */
	public String getRunTarget() {
		return runTarget;
	}

	/**
	 * @param runTarget
	 *            the runTarget to set
	 */
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

	/**
	 * @return the status
	 */
	public int getState() {
		return state;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setState(int status) {
		this.state = status;
	}

}
