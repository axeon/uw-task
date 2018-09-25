package uw.task.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.log.es.vo.LogBaseVo;
import uw.task.TaskData;
import uw.task.util.TaskMessageConverter;

import java.util.Date;

/**
 * 专门用于发送日志给log-es。
 * 因为task参数的问题。
 *
 * @author axeon
 */
@JsonIgnoreProperties({"logType","logLimitSize","taskData"})
public class TaskRunnerLog extends LogBaseVo {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunnerLog.class);

    private TaskData taskData;

    /**
     * log类型。
     */
    private int logType;

    /**
     * logLimitSize。
     */
    private int logLimitSize;

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
    public String getTaskParam() {
        Object value = taskData.getTaskParam();
        if(value != null) {
            if (logType == TaskRunnerConfig.TASK_LOG_TYPE_RECORD_ALL ||
                    logType == TaskRunnerConfig.TASK_LOG_TYPE_RECORD_TASK_PARAM) {
                String taskParam = null;
                try {
                    taskParam = TaskMessageConverter.getTaskObjectMapper().writeValueAsString(value);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                if (taskParam != null) {
                    if (logLimitSize > 0 && taskParam.length() > logLimitSize) {
                        taskParam = taskParam.substring(0, logLimitSize);
                    }
                    return taskParam;
                }
                return "JSON序列化出错,请注意排查程序: task_class = " + getTaskClass();
            }
        }
        return null;
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
    public String getResultData() {
        Object value = taskData.getResultData();
        if (value != null) {
            if (logType == TaskRunnerConfig.TASK_LOG_TYPE_RECORD_ALL ||
                    logType == TaskRunnerConfig.TASK_LOG_TYPE_RECORD_RESULT_DATA) {
                String resultData = null;
                try {
                    resultData = TaskMessageConverter.getTaskObjectMapper().writeValueAsString(value);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                if (resultData != null) {
                    if (logLimitSize > 0 && resultData.length() > logLimitSize) {
                        resultData = resultData.substring(0, logLimitSize);
                    }
                    return resultData;
                }
                return "JSON序列化出错,请注意排查程序: task_class = " + getTaskClass();
            }
        }
        return null;
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
