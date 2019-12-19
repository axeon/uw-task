package uw.task.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uw.log.es.vo.LogBaseVo;

import java.io.Serializable;
import java.util.Date;

/**
 * taskCronerLog实体类。
 *
 * @author axeon
 */
@JsonIgnoreProperties({ "refObject","logType","logLimitSize"})
public class TaskCronerLog extends LogBaseVo implements Serializable {

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
     * 日志字符串字段大小限制: 0 表示无限制
     */
    private int logLimitSize;

    public TaskCronerLog(int logType, int logLimitSize) {
        this.logLevel = logType;
        this.logLimitSize = logLimitSize;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRefId() {
        return refId;
    }

    public void setRefId(long refId) {
        this.refId = refId;
    }

    public Object getRefObject() {
        return refObject;
    }

    public void setRefObject(Object refObject) {
        this.refObject = refObject;
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

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getTaskCron() {
        return taskCron;
    }

    public void setTaskCron(String taskCron) {
        this.taskCron = taskCron;
    }

    public Date getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public Date getRunDate() {
        return runDate;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public Date getNextDate() {
        return nextDate;
    }

    public void setNextDate(Date nextDate) {
        this.nextDate = nextDate;
    }

    public String getResultData() {
        return resultData;
    }

    public void setResultData(String resultData) {
        this.resultData = resultData;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getLogLimitSize() {
        return logLimitSize;
    }

    public void setLogLimitSize(int logLimitSize) {
        this.logLimitSize = logLimitSize;
    }
}
