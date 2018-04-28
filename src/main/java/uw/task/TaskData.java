package uw.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;

/**
 * TaskData用于任务执行的传值。以为任务完成后返回结构。 TaskParam和ResultData可通过泛型参数制定具体类型。
 * TP,TD应和TaskRunner的泛型参数完全一致，否则会导致运行时出错。
 *
 * @author axeon
 */
@JsonIgnoreProperties({ "refObject"})
public class TaskData<TP, RD> implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1333167065535557828L;

    /**
     * 任务状态:未设置
     */
    public static final int STATUS_UNKNOW = 0;

    /**
     * 任务状态:成功
     */
    public static final int STATUS_SUCCESS = 1;

    /**
     * 任务状态:程序错误
     */
    public static final int STATUS_FAIL_PROGRAM = 2;

    /**
     * 任务状态:配置错误，如超过流量限制
     */
    public static final int STATUS_FAIL_CONFIG = 3;

    /**
     * 任务状态:第三方接口错误
     */
    public static final int STATUS_FAIL_PARTNER = 4;

    /**
     * 任务状态:数据错误
     */
    public static final int STATUS_FAIL_DATA = 5;
    
    /**
     * 运行模式：本地运行
     */
    public static final int RUN_TYPE_LOCAL = 1;

    /**
     * 运行模式：全局运行
     */
    public static final int RUN_TYPE_GLOBAL = 3;

    /**
     * 运行模式：全局运行RPC返回结果
     */
    public static final int RUN_TYPE_GLOBAL_RPC = 5;
    
    /**
     * 运行模式：自动运行RPC返回结果，使用此模式，会自动选择本地还远程运行模式。
     */
    public static final int RUN_TYPE_AUTO_RPC = 6;
    
    /**
     * id，此序列值由框架自动生成，无需手工设置。
     */
    private long id;
    
    /**
     * 关联TAG，由调用方设定，用于第三方统计信息。
     */
    private String refTag;

    /**
     * 关联id，由调用方根据需要设置，用于第三方统计信息。
     */
    private long refId;

    /**
     * 关联子id，由调用方根据需要设置，用于第三方统计信息。
     */
    private long refSubId;

    /**
     * 关联对象，此对象不存入数据库，但可以通过Listener来访问。
     */
    private Object refObject;

    /**
     * 流量限制TAG。
     */
    private String rateLimitTag;
    
    /**
     * 需要执行的类名，此数值必须由调用方设置。
     */
    private String taskClass = "";

    /**
     * 任务标签，用于细分任务队列，支持多实例运行。
     */
    private String taskTag = "";
    
    /**
     * 任务延迟毫秒数。一般这个时间不宜太长，大多数情况下不要超过60秒。
     */
    private long taskDelay;
    
    /**
     * 执行参数，此数值必须有调用方设置。
     */
    private TP taskParam;

    /**
     * 任务运行类型，默认为自动RPC，根据情况选择本地还是远程运行。
     */
    private int runType = RUN_TYPE_AUTO_RPC;

    /**
     * 指定运行目标。
     */
    private String runTarget = "";

    /**
     * 任务运行时主机IP，此信息由框架自动设置。
     */
    private String hostIp;

    /**
     * 任务运行时主机ID（可能为docker的ContainerID），此信息由框架自动设置。
     */
    private String hostId;

    /**
     * 进入队列时间，此信息由框架自动设置。
     */
    private Date queueDate;

    /**
     * 开始消费时间，此信息由框架自动设置。
     */
    private Date consumeDate;

    /**
     * 开始运行时间，此信息由框架自动设置。
     */
    private Date runDate;

    /**
     * 运行结束日期，此信息由框架自动设置。
     */
    private Date finishDate;

    /**
     * 执行信息，用于存储框架自动设置。
     */
    private RD resultData;

    /**
     * 出错信息
     */
    private String errorInfo;

    /**
     * 已经执行的次数，此信息由框架自动设置。
     */
    private int ranTimes;

    /**
     * 执行状态，此信息由框架根据异常自动设置。
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
	 * @return the refSubId
	 */
	public long getRefSubId() {
		return refSubId;
	}

	/**
	 * @param refSubId the refSubId to set
	 */
	public void setRefSubId(long refSubId) {
		this.refSubId = refSubId;
	}

	/**
	 * @return the refTag
	 */
	public String getRefTag() {
		return refTag;
	}

	/**
	 * @param refTag the refTag to set
	 */
	public void setRefTag(String refTag) {
		this.refTag = refTag;
	}

	/**
	 * @return the rateLimitTag
	 */
	public String getRateLimitTag() {
		return rateLimitTag;
	}

	/**
	 * @param rateLimitTag the rateLimitTag to set
	 */
	public void setRateLimitTag(String rateLimitTag) {
		this.rateLimitTag = rateLimitTag;
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
	 * @return the taskTag
	 */
	public String getTaskTag() {
		return taskTag;
	}

	public long getTaskDelay() {
		return taskDelay;
	}

	public void setTaskDelay(long taskDelay) {
		this.taskDelay = taskDelay;
	}

	/**
	 * @param taskTag the taskTag to set
	 */
	public void setTaskTag(String taskTag) {
		this.taskTag = taskTag;
	}

	/**
     * @return the taskParam
     */
    public TP getTaskParam() {
        return taskParam;
    }

    /**
     * @param taskParam the taskParam to set
     */
    public void setTaskParam(TP taskParam) {
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
     * @return the queueDate
     */
    public Date getQueueDate() {
        return queueDate;
    }

    /**
     * @param queueDate the queueDate to set
     */
    public void setQueueDate(Date queueDate) {
        this.queueDate = queueDate;
    }

    /**
     * @return the consumeDate
     */
    public Date getConsumeDate() {
        return consumeDate;
    }

    /**
     * @param consumeDate the consumeDate to set
     */
    public void setConsumeDate(Date consumeDate) {
        this.consumeDate = consumeDate;
    }

    /**
     * @return the runDate
     */
    public Date getRunDate() {
        return runDate;
    }

    /**
     * @param runDate the runDate to set
     */
    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }

    /**
     * @return the finishDate
     */
    public Date getFinishDate() {
        return finishDate;
    }

    /**
     * @param finishDate the finishDate to set
     */
    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    /**
     * @return the resultData
     */
    public RD getResultData() {
        return resultData;
    }

    /**
     * @param resultData the resultData to set
     */
    public void setResultData(RD resultData) {
        this.resultData = resultData;
    }

    /**
     * @return the errorInfo
     */
    public String getErrorInfo() {
        return errorInfo;
    }

    /**
     * @param errorInfo the errorInfo to set
     */
    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    /**
     * @return the ranTimes
     */
    public int getRanTimes() {
        return ranTimes;
    }

    /**
     * @param ranTimes the ranTimes to set
     */
    public void setRanTimes(int ranTimes) {
        this.ranTimes = ranTimes;
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
