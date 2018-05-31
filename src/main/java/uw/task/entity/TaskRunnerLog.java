package uw.task.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uw.task.TaskData;
import uw.task.util.TaskLogObjectAsStringSerializer;

import java.util.Date;

/**
 * 专门用于发送日志给log-es。
 * 因为task参数的问题。
 *
 * @author axeon
 */
@JsonIgnoreProperties({"taskData"})
public class TaskRunnerLog {

    private TaskData taskData;

    /**
     * log类型。
     */
    private int logType;

    /**
     * logLimitSize。
     */
    private long logLimitSize;

    public TaskRunnerLog(TaskData taskData) {
        this.taskData = taskData;
    }

    public TaskData getTaskData() {
        return taskData;
    }

    /**
     * @return the id
     */
    public long getId() {
        return taskData.getId();
    }

    /**
     * @return the refId
     */
    public long getRefId() {
        return taskData.getRefId();
    }

    /**
     * @return the refSubId
     */
    public long getRefSubId() {
        return taskData.getRefSubId();
    }

    /**
     * @return the refTag
     */
    public String getRefTag() {
        return taskData.getRefTag();
    }

    /**
     * @return the rateLimitTag
     */
    public String getRateLimitTag() {
        return taskData.getRateLimitTag();
    }


    /**
     * @return the taskClass
     */
    public String getTaskClass() {
        return taskData.getTaskClass();
    }

    /**
     * @return the taskTag
     */
    public String getTaskTag() {
        return taskData.getTaskTag();
    }

    public long getTaskDelay() {
        return taskData.getTaskDelay();
    }

    /**
     * @return the taskParam
     */
    @JsonSerialize(using = TaskLogObjectAsStringSerializer.class,as = String.class)
    public Object getTaskParam() {

        return taskData.getTaskParam();
    }

    /**
     * @return the runType
     */
    public int getRunType() {
        return taskData.getRunType();
    }

    /**
     * @return the runTarget
     */
    public String getRunTarget() {
        return taskData.getRunTarget();
    }

    /**
     * @return the hostIp
     */
    public String getHostIp() {
        return taskData.getHostIp();
    }

    /**
     * @return the hostId
     */
    public String getHostId() {
        return taskData.getHostId();
    }

    /**
     * @return the queueDate
     */
    public Date getQueueDate() {
        return taskData.getQueueDate();
    }

    /**
     * @return the consumeDate
     */
    public Date getConsumeDate() {
        return taskData.getConsumeDate();
    }

    /**
     * @return the runDate
     */
    public Date getRunDate() {
        return taskData.getRunDate();
    }

    /**
     * @return the finishDate
     */
    public Date getFinishDate() {
        return taskData.getFinishDate();
    }

    /**
     * @return the resultData
     */
    @JsonSerialize(using = TaskLogObjectAsStringSerializer.class,as = String.class)
    public Object getResultData() {
//        if (logType==TaskRunnerConfig)
        return taskData.getResultData();
    }

    /**
     * @return the errorInfo
     */
    public String getErrorInfo() {
        return taskData.getErrorInfo();
    }

    /**
     * @return the ranTimes
     */
    public int getRanTimes() {
        return taskData.getRanTimes();
    }

    /**
     * @return the status
     */
    public int getState() {
        return taskData.getState();
    }
}
