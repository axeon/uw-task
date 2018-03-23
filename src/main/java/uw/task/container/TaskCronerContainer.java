package uw.task.container;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import uw.task.TaskCroner;
import uw.task.TaskData;
import uw.task.TaskListenerManager;
import uw.task.api.TaskAPI;
import uw.task.conf.TaskProperties;
import uw.task.entity.TaskCronerConfig;
import uw.task.entity.TaskCronerLog;
import uw.task.exception.TaskDataException;
import uw.task.exception.TaskPartnerException;
import uw.task.listener.CronerTaskListener;
import uw.task.util.GlobalSequenceManager;
import uw.task.util.LeaderVote;
import uw.task.util.MiscUtils;

/**
 * 跑TaskCroner的容器。
 *
 * @author axeon
 */
public class TaskCronerContainer {

    private static final Logger log = LoggerFactory.getLogger(TaskCronerContainer.class);

    private org.springframework.scheduling.TaskScheduler springTaskScheduler;

    private ScheduledExecutorService localExecutor;

    /**
     * cronerTask任务索引。
     */
    private final Map<Long, ScheduledFuture<?>> cronerTasks = new HashMap<Long, ScheduledFuture<?>>(16);

    /**
     * 选举
     */
    private LeaderVote leaderVote;

    /**
     * 服务端API
     */
    private TaskAPI taskAPI;

    /**
     * 监听管理器。
     */
    private TaskListenerManager listenerManager;

    /**
     * 全局序列发生器。
     */
    private GlobalSequenceManager sequence;

    /**
     * 任务配置
     */
    private TaskProperties taskProperties;

    public TaskCronerContainer(LeaderVote leaderVote, TaskAPI taskAPI, TaskListenerManager listenerManager,
                               GlobalSequenceManager sequence, TaskProperties taskProperties) {
        this.leaderVote = leaderVote;
        this.taskAPI = taskAPI;
        this.listenerManager = listenerManager;
        this.sequence = sequence;
        this.taskProperties = taskProperties;
        // 如果禁用任务注册，则croner线程数设置为1，节省资源。
        if (!taskProperties.isEnableTaskRegistry()) {
            taskProperties.setCronerThreadNum(1);
        }
        localExecutor = Executors.newScheduledThreadPool(taskProperties.getCronerThreadNum(),
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("TaskCroner-%d").build());
        log.info("TaskCronerContainer start with [{}] threads...", taskProperties.getCronerThreadNum());
        springTaskScheduler = new ConcurrentTaskScheduler(localExecutor);
    }

    /**
     * 配置任务
     *
     * @param croner
     * @param config
     * @return
     */
    public boolean configureTask(TaskCroner croner, TaskCronerConfig config) {
        if (config == null) {
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("准备配置ID:{},CRONER:{},CRON:{},", config.getId(), config.getTaskClass(), config.getTaskCron());
        }
        stopTask(config.getId());
        // 标记删除的，直接返回了。
        if (config.getState() < 1) {
            return false;
        }
        CronTrigger trigger = new CronTrigger(config.getTaskCron());
        TaskCronerLog taskCronerLog = new TaskCronerLog();
        if (log.isDebugEnabled()) {
            log.debug("正在配置ID:{},CRONER:{},CRON:{},", config.getId(), config.getTaskClass(), config.getTaskCron());
        }
        ScheduledFuture<?> future = this.springTaskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                // 判断全局唯一条件
                if (config.getRunType() == TaskCronerConfig.RUN_TYPE_SINGLETON
                        && !leaderVote.isLeader(config.getTaskClass())) {
                    if (log.isDebugEnabled()) {
                        log.debug("非许可运行实例，直接返回。。。");
                    }
                    return;
                }
                // 任务逻辑
                taskCronerLog.setId(sequence.nextId("task_croner_log"));
                taskCronerLog.setTaskClass(config.getTaskClass());
                taskCronerLog.setTaskParam(config.getTaskParam());
                taskCronerLog.setTaskCron(config.getTaskCron());
                taskCronerLog.setRunType(config.getRunType());
                taskCronerLog.setRunTarget(config.getRunTarget());
                taskCronerLog.setHostIp(taskAPI.getHostIp());
                taskCronerLog.setHostId(taskProperties.getHostId());
                taskCronerLog.setRunDate(new Date());
                String data = "";
                // 执行监听器操作
                ArrayList<CronerTaskListener> cronerListenerList = listenerManager.getCronerListenerList();
                try {
                    for (CronerTaskListener listener : cronerListenerList) {
                        listener.onPreExecute(taskCronerLog);
                    }
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }

                try {
                    data = croner.runTask(taskCronerLog);
                    taskCronerLog.setState(TaskData.STATUS_SUCCESS);
                } catch (TaskPartnerException e) {
                    // 出现TaskDataException，说明是数据错误。
                    taskCronerLog.setState(TaskData.STATUS_FAIL_PARTNER);
                    data = MiscUtils.exceptionToString(e);
                    log.error(e.getMessage(), e);
                } catch (TaskDataException e) {
                    // 出现TaskPartnerException，说明是合作方的错误。
                    taskCronerLog.setState(TaskData.STATUS_FAIL_DATA);
                    data = MiscUtils.exceptionToString(e);
                    log.error(e.getMessage(), e);
                } catch (Exception e) {
                    data = MiscUtils.exceptionToString(e);
                    taskCronerLog.setState(TaskData.STATUS_FAIL_PROGRAM);
                    log.error(e.getMessage(), e);
                }
                // 执行监听器操作
                try {
                    for (CronerTaskListener listener : cronerListenerList) {
                        listener.onPostExecute(taskCronerLog);
                    }
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
                taskCronerLog.setFinishDate(new Date());
                taskCronerLog.setResultData(data);
                taskCronerLog.setRefObject(null);
            }
        }, triggerContext -> {
            // 任务触发，可修改任务的执行周期
            Date nextExec = trigger.nextExecutionTime(triggerContext);
            if (log.isDebugEnabled()) {
                log.debug("正在调度ID:{},CRONER:{},CRON:{},下次执行时间:{}", config.getId(), config.getTaskClass(),
                        trigger.getExpression(), nextExec.toString());
            }
            // 在此处写入本次执行的信息
            if (taskCronerLog != null && taskCronerLog.getId() > 0) {
                if (triggerContext.lastScheduledExecutionTime() != null
                        && triggerContext.lastActualExecutionTime() != null
                        && triggerContext.lastCompletionTime() != null) {
                    taskCronerLog.setScheduleDate(triggerContext.lastScheduledExecutionTime());
                    // 写入下次计划执行日期。
                    taskCronerLog.setNextDate(nextExec);
                    // 入库
                    taskAPI.sendTaskCronerLog(taskCronerLog);
                }
            }
            return nextExec;
        });
        this.cronerTasks.put(config.getId(), future);
        return true;
    }

    /**
     * 停止一个任务。
     *
     * @param id 任务编号
     */
    public boolean stopTask(long id) {
        ScheduledFuture<?> future = this.cronerTasks.get(id);
        if (future == null) {
            return false;
        }
        future.cancel(true);
        return true;
    }

    /**
     * 销毁所有的task
     */
    public void stopAllTaskCroner() {
        log.info("croner destroy....");
        for (Entry<Long, ScheduledFuture<?>> kv : this.cronerTasks.entrySet()) {
            // 任务停止
            kv.getValue().cancel(true);
        }
        if (this.localExecutor != null) {
            this.localExecutor.shutdownNow();
        }
    }

}
