package uw.task.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * taskRunnerConfig实体类。
 *
 * @author axeon
 * @version $Revision: 1.00 $ $Date: 2017-05-03 14:00:50
 */
public class TaskRunnerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 限速类型：不限速
     */
    public static final int RATE_LIMIT_NONE = 0;

    /**
     * 限速类型：本地进程限速
     */
    public static final int RATE_LIMIT_LOCAL = 1;

    /**
     * 限速类型：本地TASK限速
     */
    public static final int RATE_LIMIT_LOCAL_TASK = 2;

    /**
     * 限速类型：本地TASK+TAG限速
     */
    public static final int RATE_LIMIT_LOCAL_TASK_TAG = 3;

    /**
     * 限速类型：全局主机HOST限速
     */
    public static final int RATE_LIMIT_GLOBAL_HOST = 4;

    /**
     * 限速类型：全局TAG限速
     */
    public static final int RATE_LIMIT_GLOBAL_TAG = 5;

    /**
     * 限速类型：全局TASK限速
     */
    public static final int RATE_LIMIT_GLOBAL_TASK = 6;

    /**
     * 限速类型：全局TAG+HOST限速
     */
    public static final int RATE_LIMIT_GLOBAL_TAG_HOST = 7;

    /**
     * 限速类型：全局TASK+IP限速
     */
    public static final int RATE_LIMIT_GLOBAL_TASK_HOST = 8;

    /**
     * 限速类型：全局TASK+TAG限速
     */
    public static final int RATE_LIMIT_GLOBAL_TASK_TAG = 9;

    /**
     * 限速类型：全局TASK+TAG+IP限速
     */
    public static final int RATE_LIMIT_GLOBAL_TASK_TAG_HOST = 10;

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
     * 任务名称
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String taskDesc;

    /**
     * 执行类信息
     */
    private String taskClass;

    /**
     * 执行类TAG，可能用于区分子任务
     */
    private String taskTag;

    /**
     * 消费者的数量
     */
    private int consumerNum = 1;

    /**
     * 预取任务数。
     */
    private int prefetchNum = 1;

    /**
     * 详见流量限制类型说明。
     */
    private int rateLimitType = RATE_LIMIT_NONE;

    /**
     * 流量限定数值，默认为10次
     */
    private int rateLimitValue = 10;

    /**
     * 流量限定时间(S)，默认为1秒
     */
    private int rateLimitTime = 1;

    /**
     * 当发生流量限制时，等待的秒数，默认300秒
     */
    private int rateLimitWait = 30;

    /**
     * 超过流量限制重试次数，默认不在重试，放弃任务。
     */
    private int retryTimesByOverrated = 0;

    /**
     * 对方接口错误重试次数，默认不再重试，放弃任务。
     */
    private int retryTimesByPartner = 0;

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
     * 配置失败率
     */
    private int failConfigRate;

    /**
     * 数据失败率
     */
    private int failDataRate;

    /**
     * 队列等待超时
     */
    private int queueTimeout;

    /**
     * 等待超时
     */
    private int waitTimeout;

    /**
     * 运行超时
     */
    private int runTimeout;

    /**
     * 创建日期。
     */
    private Date createDate;

    /**
     * 修改日期。
     */
    private Date modifyDate;

    /**
     * 状态值
     */
    private int state = 1;

    /**
     * 详见日志类型说明
     */
    private int logLevel = TASK_LOG_TYPE_RECORD;

    /**
     * 日志字符串字段大小限制: 0 表示无限制
     */
    private int logLimitSize = 0;

    public TaskRunnerConfig() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getTaskClass() {
        return taskClass;
    }

    public void setTaskClass(String taskClass) {
        this.taskClass = taskClass;
    }

    public String getTaskTag() {
        return taskTag;
    }

    public void setTaskTag(String taskTag) {
        this.taskTag = taskTag;
    }

    public int getConsumerNum() {
        return consumerNum;
    }

    public void setConsumerNum(int consumerNum) {
        this.consumerNum = consumerNum;
    }

    public int getPrefetchNum() {
        return prefetchNum;
    }

    public void setPrefetchNum(int prefetchNum) {
        this.prefetchNum = prefetchNum;
    }

    public int getRateLimitType() {
        return rateLimitType;
    }

    public void setRateLimitType(int rateLimitType) {
        this.rateLimitType = rateLimitType;
    }

    public int getRateLimitValue() {
        return rateLimitValue;
    }

    public void setRateLimitValue(int rateLimitValue) {
        this.rateLimitValue = rateLimitValue;
    }

    public int getRateLimitTime() {
        return rateLimitTime;
    }

    public void setRateLimitTime(int rateLimitTime) {
        this.rateLimitTime = rateLimitTime;
    }

    public int getRateLimitWait() {
        return rateLimitWait;
    }

    public void setRateLimitWait(int rateLimitWait) {
        this.rateLimitWait = rateLimitWait;
    }

    public int getRetryTimesByOverrated() {
        return retryTimesByOverrated;
    }

    public void setRetryTimesByOverrated(int retryTimesByOverrated) {
        this.retryTimesByOverrated = retryTimesByOverrated;
    }

    public int getRetryTimesByPartner() {
        return retryTimesByPartner;
    }

    public void setRetryTimesByPartner(int retryTimesByPartner) {
        this.retryTimesByPartner = retryTimesByPartner;
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

    public int getFailConfigRate() {
        return failConfigRate;
    }

    public void setFailConfigRate(int failConfigRate) {
        this.failConfigRate = failConfigRate;
    }

    public int getFailDataRate() {
        return failDataRate;
    }

    public void setFailDataRate(int failDataRate) {
        this.failDataRate = failDataRate;
    }

    public int getQueueTimeout() {
        return queueTimeout;
    }

    public void setQueueTimeout(int queueTimeout) {
        this.queueTimeout = queueTimeout;
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

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public int getLogLimitSize() {
        return logLimitSize;
    }

    public void setLogLimitSize(int logLimitSize) {
        this.logLimitSize = logLimitSize;
    }
}
