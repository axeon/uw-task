package uw.task.entity;

import java.io.Serializable;

/**
 * taskCronerLog实体类。
 *
 * @author axeon
 */
public class TaskCronerLog implements Serializable {

    private static final long serialVersionUID = 1L;

	private long id;

    /**
     * 关联ID
     */
    private long refId;

    /**
     * 关联对象，此对象不会发送到服务器端。
     */
    private Object refObject;

    /**
     * 执行的类名
     */
    private String taskClass;
    
    /**
     * 执行参数，可能用于区分子任务
     */
    private String taskParam;


    /**
     * 运行类型。
     */
    private int runType;

    /**
     * 指定运行目标主机，可为空。
     */
    private String runTarget = "";

    /**
     * 实际运行主机IP，此信息由程序自动生成。
     */
    private String hostIp;

    /**
     * 实际运行主机ID，此信息由程序自动生成。
     */
    private String hostId;

    /**
     * 配置信息
     */
    private String taskCron;

    /**
     * 计划执行时间
     */
    private java.util.Date scheduleDate;

    /**
     * 开始运行时间
     */
    private java.util.Date runDate;

    /**
     * 运行结束日期
     */
    private java.util.Date finishDate;

    /**
     * 下次执行时间
     */
    private java.util.Date nextDate;

    /**
     * 执行信息，用于存储任务完成信息。
     */
    private String resultData;

    /**
     * 执行状态
     */
    private int state;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the refId
     */
    public long getRefId() {
        return refId;
    }

    /**
     * @param refId the refId to set
     */
    public void setRefId(long refId) {
        this.refId = refId;
    }

    /**
     * @return the refObject
     */
    public Object getRefObject() {
        return refObject;
    }

    /**
     * @param refObject the refObject to set
     */
    public void setRefObject(Object refObject) {
        this.refObject = refObject;
    }

    /**
     * @return the taskClass
     */
    public String getTaskClass() {
        return taskClass;
    }

    /**
     * @param taskClass the taskClass to set
     */
    public void setTaskClass(String taskClass) {
        this.taskClass = taskClass;
    }

    /**
	 * @return the taskParam
	 */
	public String getTaskParam() {
		return taskParam;
	}

	/**
	 * @param taskParam the taskParam to set
	 */
	public void setTaskParam(String taskParam) {
		this.taskParam = taskParam;
	}

	/**
     * @return the runType
     */
    public int getRunType() {
        return runType;
    }

    /**
     * @param runType the runType to set
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
     * @param runTarget the runTarget to set
     */
    public void setRunTarget(String runTarget) {
        this.runTarget = runTarget;
    }

    /**
     * @return the hostIp
     */
    public String getHostIp() {
        return hostIp;
    }

    /**
     * @param hostIp the hostIp to set
     */
    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    /**
     * @return the hostId
     */
    public String getHostId() {
        return hostId;
    }

    /**
     * @param hostId the hostId to set
     */
    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    /**
     * @return the taskCron
     */
    public String getTaskCron() {
        return taskCron;
    }

    /**
     * @param taskCron the taskCron to set
     */
    public void setTaskCron(String taskCron) {
        this.taskCron = taskCron;
    }

    /**
     * @return the scheduleDate
     */
    public java.util.Date getScheduleDate() {
        return scheduleDate;
    }

    /**
     * @param scheduleDate the scheduleDate to set
     */
    public void setScheduleDate(java.util.Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    /**
     * @return the runDate
     */
    public java.util.Date getRunDate() {
        return runDate;
    }

    /**
     * @param runDate the runDate to set
     */
    public void setRunDate(java.util.Date exeDate) {
        this.runDate = exeDate;
    }

    /**
     * @return the finishDate
     */
    public java.util.Date getFinishDate() {
        return finishDate;
    }

    /**
     * @param finishDate the finishDate to set
     */
    public void setFinishDate(java.util.Date finishDate) {
        this.finishDate = finishDate;
    }

    /**
     * @return the nextDate
     */
    public java.util.Date getNextDate() {
        return nextDate;
    }

    /**
     * @param nextDate the nextDate to set
     */
    public void setNextDate(java.util.Date nextDate) {
        this.nextDate = nextDate;
    }

    /**
     * @return the resultData
     */
    public String getResultData() {
        return resultData;
    }

    /**
     * @param resultData the resultData to set
     */
    public void setResultData(String resultData) {
        this.resultData = resultData;
    }

    /**
     * @return the status
     */
    public int getState() {
        return state;
    }

    /**
     * @param status the status to set
     */
    public void setState(int status) {
        this.state = status;
    }


}
