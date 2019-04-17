package uw.task.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.management.ManagementFactory;

/**
 * 任务配置类。
 *
 * @author axeon
 */
@ConfigurationProperties(prefix = "uw.task")
public class TaskProperties {

    /**
     * 是否启用uw-task服务注册和主机注册，默认不启用。
     *
     */
    private boolean enableTaskRegistry = false;
    /**
     * 任务名，必须设置为基础包名。
     */
    private String project;

    /**
     * 私有模式。不运行全局target任务。
     */
    private boolean privacyMode = false;

    /**
     * croner线程数，默认在3个，建议按照实际croner任务数量*70%。
     */
    private int cronerThreadNum = 5;

    /**
     * RPC最小线程数,用于执行RPC调用，如不使用rpc，建议设置为1，否则按照最大并发量*10%设置。
     */
    private int taskRpcMinThreadNum = 1;

    /**
     * RPC最大线程数,用于执行RPC调用，超过此线程数，将会导致阻塞。
     */
    private int taskRpcMaxThreadNum = 100;

    /**
     * 队列任务重试延时毫秒数，默认2秒
     */
    private long taskQueueRetryDelay = 2000;

    /**
     *  rpc任务重试延时毫秒数，默认100毫秒
     */
    private long taskRpcRetryDelay = 100;

    /**
     * 运行主机ID
     */
    private String hostId = ManagementFactory.getRuntimeMXBean().getName();

    /**
     * 任务服务器
     */
    private String taskCenterHost;

    /**
     * Redis配置
     */
    private RedisProperties redis;

    /**
     * Rabbit MQ配置
     */
    private RabbitProperties rabbitmq;

    public static class RedisProperties extends org.springframework.boot.autoconfigure.data.redis.RedisProperties {

    }

    public static class RabbitProperties extends org.springframework.boot.autoconfigure.amqp.RabbitProperties {

    }

    public boolean isEnableTaskRegistry() {
        return enableTaskRegistry;
    }

    public void setEnableTaskRegistry(boolean enableTaskRegistry) {
        this.enableTaskRegistry = enableTaskRegistry;
    }

    /**
	 * @return the privacyMode
	 */
	public boolean isPrivacyMode() {
		return privacyMode;
	}

	/**
	 * @param privacyMode the privacyMode to set
	 */
	public void setPrivacyMode(boolean privacyMode) {
		this.privacyMode = privacyMode;
	}

	public int getTaskRpcMinThreadNum() {
		return taskRpcMinThreadNum;
	}

	public void setTaskRpcMinThreadNum(int taskRpcMinThreadNum) {
		this.taskRpcMinThreadNum = taskRpcMinThreadNum;
	}

	public int getTaskRpcMaxThreadNum() {
		return taskRpcMaxThreadNum;
	}

	public void setTaskRpcMaxThreadNum(int taskRpcMaxThreadNum) {
		this.taskRpcMaxThreadNum = taskRpcMaxThreadNum;
	}

	/**
     * @return the project
     */
    public String getProject() {
        return project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(String project) {
        this.project = project;
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
	 * @return the cronerThreadNum
	 */
	public int getCronerThreadNum() {
		return cronerThreadNum;
	}

	/**
	 * @param cronerThreadNum the cronerThreadNum to set
	 */
	public void setCronerThreadNum(int cronerThreadNum) {
		this.cronerThreadNum = cronerThreadNum;
	}

	/**
     * @return the taskCenterHost
     */
    public String getTaskCenterHost() {
        return taskCenterHost;
    }

    /**
     * @param taskCenterHost the taskCenterHost to set
     */
    public void setTaskCenterHost(String taskCenterHost) {
        this.taskCenterHost = taskCenterHost;
    }

    public RedisProperties getRedis() {
        return redis;
    }

    public void setRedis(RedisProperties redis) {
        this.redis = redis;
    }

    public RabbitProperties getRabbitmq() {
        return rabbitmq;
    }

    public void setRabbitmq(RabbitProperties rabbitmq) {
        this.rabbitmq = rabbitmq;
    }

    public long getTaskQueueRetryDelay() {
        return taskQueueRetryDelay;
    }

    public void setTaskQueueRetryDelay(long taskQueueRetryDelay) {
        this.taskQueueRetryDelay = taskQueueRetryDelay;
    }

    public long getTaskRpcRetryDelay() {
        return taskRpcRetryDelay;
    }

    public void setTaskRpcRetryDelay(long taskRpcRetryDelay) {
        this.taskRpcRetryDelay = taskRpcRetryDelay;
    }
}
