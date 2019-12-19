package uw.task.container;

import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uw.task.TaskData;
import uw.task.TaskFactory;
import uw.task.TaskListenerManager;
import uw.task.TaskRunner;
import uw.task.api.TaskAPI;
import uw.task.conf.TaskMetaInfoManager;
import uw.task.conf.TaskProperties;
import uw.task.entity.TaskRunnerConfig;
import uw.task.entity.TaskRunnerLog;
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
     * TaskFactory
     */
    private TaskFactory taskFactory;

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
     * 任务配置
     */
    private TaskProperties taskProperties;

    /**
     * 默认构造器。
     *
     * @param taskProperties
     * @param taskAPI
     * @param localRateLimiter
     * @param globalRateLimiter
     * @param taskListenerManager
     */
    public TaskRunnerContainer(TaskProperties taskProperties, TaskAPI taskAPI, LocalRateLimiter localRateLimiter,
                               GlobalRateLimiter globalRateLimiter, TaskListenerManager taskListenerManager) {
        this.taskProperties = taskProperties;
        this.taskAPI = taskAPI;
        this.localRateLimiter = localRateLimiter;
        this.globalRateLimiter = globalRateLimiter;
        this.taskListenerManager = taskListenerManager;
    }

    public void setTaskFactory(TaskFactory taskFactory) {
        this.taskFactory = taskFactory;
    }

    /**
     * 执行任务
     *
     * @param taskData
     * @return
     */
    @SuppressWarnings("unchecked")
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
        taskData.setHostId(taskProperties.getHostId());
        // 增加执行信息
        taskData.setRanTimes(taskData.getRanTimes() + 1);

        // 限制标记，0时说明无限制
        long noLimitFlag = 0;
        // 对于RPC调用和本地调用来说，不受任何流控限制。
        if (taskData.getRunType() != TaskData.RUN_TYPE_GLOBAL_RPC && taskData.getRunType() != TaskData.RUN_TYPE_LOCAL) {
            if (taskConfig.getRateLimitType() != TaskRunnerConfig.RATE_LIMIT_NONE) {
                if (taskConfig.getRateLimitType() == TaskRunnerConfig.RATE_LIMIT_LOCAL) {
                    // 进程内限制
                    boolean flag = localRateLimiter.tryAcquire("", taskConfig.getRateLimitValue(), taskConfig.getRateLimitTime(), taskConfig.getRateLimitWait(), 1);
                    noLimitFlag = flag ? 0 : -1;
                } else if (taskConfig.getRateLimitType() == TaskRunnerConfig.RATE_LIMIT_LOCAL_TASK) {
                    // 进程内+任务名限制
                    boolean flag = localRateLimiter.tryAcquire(taskData.getTaskClass(), taskConfig.getRateLimitValue(), taskConfig.getRateLimitTime(), taskConfig.getRateLimitWait(), 1);
                    noLimitFlag = flag ? 0 : -1;
                } else if (taskConfig.getRateLimitType() == TaskRunnerConfig.RATE_LIMIT_LOCAL_TASK_TAG) {
                    // 进程内+任务名限制
                    String locker = taskData.getTaskClass() + "$" + taskData.getRateLimitTag();
                    boolean flag = localRateLimiter.tryAcquire(locker, taskConfig.getRateLimitValue(), taskConfig.getRateLimitTime(), taskConfig.getRateLimitWait(), 1);
                    noLimitFlag = flag ? 0 : -1;
                } else {
                    StringBuilder locker = new StringBuilder(50).append(taskData.getTaskClass());
                    switch (taskConfig.getRateLimitType()) {
                        case TaskRunnerConfig.RATE_LIMIT_GLOBAL_TAG:
                            locker.append("$").append(taskData.getRateLimitTag());
                            break;
                        case TaskRunnerConfig.RATE_LIMIT_GLOBAL_HOST:
                            locker.append("$@").append(taskAPI.getHostIp());
                            break;
                        case TaskRunnerConfig.RATE_LIMIT_GLOBAL_TASK:
                            locker.append("$");
                            break;
                        case TaskRunnerConfig.RATE_LIMIT_GLOBAL_TAG_HOST:
                            locker.append("$").append(taskData.getRateLimitTag()).append("@").append(taskAPI.getHostIp());
                            break;
                        case TaskRunnerConfig.RATE_LIMIT_GLOBAL_TASK_TAG:
                            locker.append("$").append(taskData.getRateLimitTag());
                            break;
                        case TaskRunnerConfig.RATE_LIMIT_GLOBAL_TASK_HOST:
                            locker.append("$@").append(taskAPI.getHostIp());
                            break;
                        case TaskRunnerConfig.RATE_LIMIT_GLOBAL_TASK_TAG_HOST:
                            locker.append("$").append(taskData.getRateLimitTag()).append("@").append(taskAPI.getHostIp());
                            break;
                    }
                    // 全局流量限制
                    // 检查是否超过流量限制
                    // 开始进行延时等待
                    long end = System.currentTimeMillis() + taskConfig.getRateLimitWait() * 1000;
                    while (System.currentTimeMillis() <= end) {
                        noLimitFlag = globalRateLimiter.tryAcquire(locker.toString(), taskConfig.getRateLimitValue(), taskConfig.getRateLimitTime(), 1);
                        if (noLimitFlag == 0) {
                            break;
                        }
                        try {
                            Thread.sleep(noLimitFlag);
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
        if (noLimitFlag == 0) {
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

        // 不管如何，都给设定结束日期。
        taskData.setFinishDate(new Date());
        // 保存日志与统计信息
        int logLevel = taskConfig.getLogLevel();
        if (logLevel > TaskRunnerConfig.TASK_LOG_TYPE_NONE) {
            TaskRunnerLog log = new TaskRunnerLog(taskData);
            log.setLogLevel(logLevel);
            log.setLogLimitSize(taskConfig.getLogLimitSize());
            taskAPI.sendTaskRunnerLog(log);
        }

        if (taskData.getRunType() == TaskData.RUN_TYPE_GLOBAL_RPC || taskData.getRunType() == TaskData.RUN_TYPE_LOCAL) {
            // 如果异常，根据任务设置，重新跑
            if (taskData.getRetryType() == TaskData.RETRY_TYPE_AUTO) {
                if (taskData.getState() > TaskData.STATUS_SUCCESS) {
                    //设置任务延时，原计划使用Math.pow(2,x)的，后来决定不用了
                    taskData.setTaskDelay(taskData.getRanTimes() * taskProperties.getTaskRpcRetryDelay());
                    if (taskData.getState() == TaskData.STATUS_FAIL_CONFIG) {
                        if (taskData.getRanTimes() < taskConfig.getRetryTimesByOverrated()) {
                            taskData = taskFactory.runTaskLocal(cleanTaskInfo(taskData));
                        }
                    } else if (taskData.getState() == TaskData.STATUS_FAIL_PARTNER) {
                        if (taskData.getRanTimes() < taskConfig.getRetryTimesByPartner()) {
                            taskData = taskFactory.runTaskLocal(cleanTaskInfo(taskData));
                        }
                    }
                }
            }
            return taskData;
        } else {
            // 如果异常，根据任务设置，重新跑
            if (taskData.getRetryType() == TaskData.RETRY_TYPE_AUTO) {
                if (taskData.getState() > TaskData.STATUS_SUCCESS) {
                    //设置任务延时，原计划使用Math.pow(2,x)的，后来决定不用了
                    taskData.setTaskDelay(taskData.getRanTimes() * taskProperties.getTaskQueueRetryDelay());
                    if (taskData.getState() == TaskData.STATUS_FAIL_CONFIG) {
                        if (taskData.getRanTimes() < taskConfig.getRetryTimesByOverrated()) {
                            taskFactory.sendToQueue(cleanTaskInfo(taskData));
                        }
                    } else if (taskData.getState() == TaskData.STATUS_FAIL_PARTNER) {
                        if (taskData.getRanTimes() < taskConfig.getRetryTimesByPartner()) {
                            taskFactory.sendToQueue(cleanTaskInfo(taskData));
                        }
                    }
                }
            }
            return null;
        }
    }

    /**
     * 清理任务信息。
     *
     * @param srcData
     */
    private TaskData cleanTaskInfo(TaskData srcData) {
        TaskData taskData = new TaskData();
        MiscUtils.copyTaskData(srcData, taskData);
        //先清除一些任务信息。
        taskData.setHostIp(null);
        taskData.setHostId(null);
        taskData.setRefObject(null);
        taskData.setConsumeDate(null);
        taskData.setRunDate(null);
        taskData.setFinishDate(null);
        taskData.setResultData(null);
        taskData.setErrorInfo(null);
        taskData.setState(TaskData.STATUS_UNKNOW);
        return taskData;
    }

}
