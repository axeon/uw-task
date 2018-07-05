package uw.task.container;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uw.auth.client.AuthClientProperties;
import uw.task.TaskData;
import uw.task.TaskListenerManager;
import uw.task.TaskRunner;
import uw.task.TaskScheduler;
import uw.task.api.TaskAPI;
import uw.task.conf.TaskMetaInfoManager;
import uw.task.conf.TaskProperties;
import uw.task.entity.TaskRunnerConfig;
import uw.task.exception.TaskDataException;
import uw.task.exception.TaskPartnerException;
import uw.task.listener.RunnerTaskListener;
import uw.task.util.GlobalRateLimiter;
import uw.task.util.LocalRateLimiter;
import uw.task.util.MiscUtils;

/**
 * 在此处接受MQ信息，并进行处理。
 *
 * @author axeon
 */
public class TaskRunnerContainer {

	private static final Logger log = LoggerFactory.getLogger(TaskRunnerContainer.class);

	/**
	 * TaskScheduler
	 */
	private TaskScheduler taskScheduler;

	/**
	 * 服务端任务API
	 */
	private TaskAPI taskAPI;

	/**
	 * 全局流量限制服务
	 */
	private GlobalRateLimiter globalRateLimiter;

	/**
	 * 本地流量限制服务
	 */
	private LocalRateLimiter localRateLimiter;

	/**
	 * 监听管理器。
	 */
	private TaskListenerManager taskListenerManager;

	/**
	 * 主机配置
	 */
	private AuthClientProperties authClientProperties;

	/**
	 * 默认构造器。
	 * 
	 * @param authClientProperties
	 * @param taskAPI
	 * @param localRateLimiter
	 * @param globalRateLimiter
	 * @param taskListenerManager
	 */
	public TaskRunnerContainer(final AuthClientProperties authClientProperties,
                               final TaskAPI taskAPI,
                               final LocalRateLimiter localRateLimiter,
                               final GlobalRateLimiter globalRateLimiter,
                               final TaskListenerManager taskListenerManager) {
		this.authClientProperties = authClientProperties;
		this.taskAPI = taskAPI;
		this.localRateLimiter = localRateLimiter;
		this.globalRateLimiter = globalRateLimiter;
		this.taskListenerManager = taskListenerManager;
	}

	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	/**
	 * 执行任务
	 *
	 * @param taskData
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public TaskData process(TaskData taskData) {
		// 设置开始消费时间
		taskData.setConsumeDate(new Date());
		// 获得任务实例
		TaskRunner<?, ?> taskRunner = TaskMetaInfoManager.getRunner(taskData.getTaskClass());
		// 获得任务设置数据
		TaskRunnerConfig taskConfig = TaskMetaInfoManager.getRunnerConfig(taskData);
		// 设置运行标记
		taskData.setHostIp(taskAPI.getHostIp());
		// 设置主机ID
		taskData.setHostId(authClientProperties.getHostId());
		// 增加执行信息
		taskData.setRanTimes(taskData.getRanTimes() + 1);

		// 限制标记，0时说明无限制
		long nolimitFlag = 0;
		// 对于RPC调用来说，根本就不受任何流控限制。
		if (taskData.getRunType() != TaskData.RUN_TYPE_GLOBAL_RPC) {

			if (taskConfig != null && taskConfig.getRateLimitType() != TaskRunnerConfig.RATE_LIMIT_TYPE_NONE) {
				if (taskConfig.getRateLimitType() == TaskRunnerConfig.RATE_LIMIT_TYPE_PROCESS) {
					// 进程内限制
					localRateLimiter.initLimiter("", taskConfig.getRateLimitValue(), taskConfig.getRateLimitTime());
					boolean flag = localRateLimiter.tryAcquire("", taskConfig.getRateLimitWait(), TimeUnit.SECONDS);
					nolimitFlag = flag ? 0 : -1;
				} else if (taskConfig.getRateLimitType() == TaskRunnerConfig.RATE_LIMIT_TYPE_TASK_PROCESS) {
					// 进程内+任务名限制
					localRateLimiter.initLimiter(taskData.getTaskClass(), taskConfig.getRateLimitValue(),
							taskConfig.getRateLimitTime());
					boolean flag = localRateLimiter.tryAcquire(taskData.getTaskClass(), taskConfig.getRateLimitWait(),
							TimeUnit.SECONDS);
					nolimitFlag = flag ? 0 : -1;
				} else if (taskConfig.getRateLimitType() == TaskRunnerConfig.RATE_LIMIT_TYPE_TAG_PROCESS) {
					// 进程内+任务名限制
					String locker = taskData.getTaskClass() + "$" + String.valueOf(taskData.getRateLimitTag());
					localRateLimiter.initLimiter(locker, taskConfig.getRateLimitValue(), taskConfig.getRateLimitTime());
					boolean flag = localRateLimiter.tryAcquire(locker, taskConfig.getRateLimitWait(), TimeUnit.SECONDS);
					nolimitFlag = flag ? 0 : -1;
				} else {
					String locker = taskData.getTaskClass();
					switch (taskConfig.getRateLimitType()) {
					case TaskRunnerConfig.RATE_LIMIT_TYPE_TAG:
						locker = "$" + String.valueOf(taskData.getRateLimitTag());
						break;
					case TaskRunnerConfig.RATE_LIMIT_TYPE_IP:
						locker = "$@" + taskAPI.getHostIp();
						break;
					case TaskRunnerConfig.RATE_LIMIT_TYPE_TASK:
						locker += "$";
						break;
					case TaskRunnerConfig.RATE_LIMIT_TYPE_TASK_TAG:
						locker += "$" + String.valueOf(taskData.getRateLimitTag());
						break;
					case TaskRunnerConfig.RATE_LIMIT_TYPE_TASK_IP:
						locker += "$@" + taskAPI.getHostIp();
						break;
					case TaskRunnerConfig.RATE_LIMIT_TYPE_TAG_IP:
						locker = "$" + String.valueOf(taskData.getRateLimitTag()) + "@" + taskAPI.getHostIp();
						break;
					}
					// 全局流量限制
					// 检查是否超过流量限制
					globalRateLimiter.initLimiter(locker, taskConfig.getRateLimitValue(), TimeUnit.SECONDS,
							taskConfig.getRateLimitTime());
					// 开始进行延时等待
					long end = System.currentTimeMillis() + taskConfig.getRateLimitWait() * 1000;
					while (System.currentTimeMillis() <= end) {
						nolimitFlag = globalRateLimiter.tryAcquire(locker);
						if (nolimitFlag == 0) {
							break;
						}
						try {
							Thread.sleep(nolimitFlag);
						} catch (InterruptedException e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			}

		}
		// 执行任务延时设定。
		if (taskData.getTaskDelay() > 0 && taskData.getQueueDate() != null) {
			long delaySleep = taskData.getTaskDelay()
					- (System.currentTimeMillis() - taskData.getQueueDate().getTime());
			if (delaySleep > 0) {
				try {
					Thread.sleep(delaySleep);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		// 设置开始执行时间
		taskData.setRunDate(new Date());

		// 如果允许，则开始执行。
		if (nolimitFlag == 0) {
			ArrayList<RunnerTaskListener> runnerListenerList = taskListenerManager.getRunnerListenerList();
			try {
				// 执行任务
				taskData.setResultData(taskRunner.runTask(taskData));
				taskData.setState(TaskData.STATUS_SUCCESS);
			} catch (TaskDataException e) {
				// 出现TaskDataException，说明是数据错误。
				taskData.setState(TaskData.STATUS_FAIL_DATA);
				taskData.setErrorInfo(MiscUtils.exceptionToString(e));
				log.error(e.getMessage(), e);
			} catch (TaskPartnerException e) {
				// 出现TaskPartnerException，说明是合作方的错误。
				taskData.setState(TaskData.STATUS_FAIL_PARTNER);
				taskData.setErrorInfo(MiscUtils.exceptionToString(e));
				log.error(e.getMessage(), e);
			} catch (Throwable e) {
				// 设置异常状态
				taskData.setState(TaskData.STATUS_FAIL_PROGRAM);
				// 设置异常信息，自动屏蔽spring自己的输出。
				taskData.setErrorInfo(MiscUtils.exceptionToString(e));
				log.error(e.getMessage(), e);
			}
			// 执行监听器操作
			try {
				for (RunnerTaskListener listener : runnerListenerList) {
					listener.onPostExecute(taskData);
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			}
			// 清除refObject。
			taskData.setRefObject(null);
		} else {
			taskData.setErrorInfo("RateLimit!!!" + taskConfig.getRateLimitValue() + "/" + taskConfig.getRateLimitTime()
					+ "s, Wait " + taskConfig.getRateLimitWait() + "s!");
			taskData.setState(TaskData.STATUS_FAIL_CONFIG);
		}
		// 如果异常，根据任务设置，重新跑
		if (taskData.getState() > TaskData.STATUS_SUCCESS) {
			if (taskData.getState() == TaskData.STATUS_FAIL_CONFIG) {
				if (taskData.getRanTimes() < taskConfig.getRetryTimesByOverrated()) {
					taskScheduler.sendToQueue(taskData);
				}
			} else if (taskData.getState() == TaskData.STATUS_FAIL_PARTNER) {
				if (taskData.getRanTimes() < taskConfig.getRetryTimesByPartner()) {
					taskScheduler.sendToQueue(taskData);
				}
			}
		}
		// 不管如何，都给设定结束日期。
		taskData.setFinishDate(new Date());
		// 保存日志与统计信息
		taskAPI.sendTaskRunnerLog(taskData);
		if (taskData.getRunType() == TaskData.RUN_TYPE_GLOBAL_RPC || taskData.getRunType() == TaskData.RUN_TYPE_LOCAL) {
			return taskData;
		} else {
			return null;
		}
	}

}