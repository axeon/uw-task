package uw.task.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import uw.log.es.LogClient;
import uw.task.TaskData;
import uw.task.conf.TaskMetaInfoManager;
import uw.task.entity.TaskCronerLog;
import uw.task.log.TaskDataLog;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * uw-task日志服务
 *
 * @author liliang
 * @since 2018-04-28
 */
public class TaskLogService {

    /**
     * 用于锁定Runner数据列表
     */
    private final Lock runnerLogLock = new ReentrantLock();

    /**
     * 用于存储要保存的RunnerLog
     */
    private List<TaskDataLog> runnerLogList = Lists.newArrayList();

    /**
     * 用于锁定Croner数据列表
     */
    private final Lock cronerLogLock = new ReentrantLock();

    /**
     * 用于存储要保存的CronerLog
     */
    private List<TaskCronerLog> cronerLogList = Lists.newArrayList();

    /**
     * 日志客户端
     */
    private final LogClient logClient;

    /**
     * Redis连接工厂
     */
    private final TaskMetricsService taskMetricsService;

    public TaskLogService(final LogClient logClient,final TaskMetricsService taskMetricsService){
        this.logClient = logClient;
        this.taskMetricsService = taskMetricsService;
    }

    /**
     * 写Runner日志
     * @param taskData
     */
    public void writeRunnerLog(TaskData taskData) {
        runnerLogLock.lock();
        try {
            runnerLogList.add(new TaskDataLog(taskData));
        } finally {
            runnerLogLock.unlock();
        }
    }

    /**
     * 写Croner日志
     * @param cronerLog
     */
    public void writeCronerLog(TaskCronerLog cronerLog) {
        cronerLogLock.lock();
        try {
            if (cronerLog.getRunTarget() == null) {
                cronerLog.setRunTarget("");
            }
            cronerLogList.add(cronerLog);
        } finally {
            cronerLogLock.unlock();
        }
    }

    /**
     * 定期批量写RunnerLog数据到日志服务器
     */
    public void sendRunnerLogToServer() {
        // 从中获得list数据。
        List<TaskDataLog> runnerLogData = null;
        runnerLogLock.lock();
        try {
            if (runnerLogList.size() > 0) {
                runnerLogData = runnerLogList;
                runnerLogList = Lists.newArrayList();
            }
        } finally {
            runnerLogLock.unlock();
        }
        if (runnerLogData == null){
            return;
        }

        // 统计metrics数据
        Map<String, long[]> statsMap = Maps.newHashMap();
        for (TaskDataLog log : runnerLogData) {
            // numAll,numFail,numFailProgram,numFailSetting,numFailPartner,numFailData,timeQueue,timeConsume,timeRun
            String key = TaskMetaInfoManager.getRunnerLogKey(log.getTaskData());
            long[] metrics = statsMap.computeIfAbsent(key,pk -> new long[10]);
            metrics[0] += 1;
            // state: 1: 成功;2: 程序错误;3: 配置错误;4: 对方错误;5: 数据错误
            if (log.getState() > 1) {
                if (log.getState() == 2) {
                    metrics[2] += 1;
                    metrics[1] += 1;
                } else if (log.getState() == 3) {
                    metrics[3] += 1;
                    metrics[1] += 1;
                } else if (log.getState() == 4) {
                    metrics[4] += 1;
                    metrics[1] += 1;
                } else if (log.getState() == 5) {
                    metrics[5] += 1;
                    metrics[1] += 1;
                }
            }
            if (log.getFinishDate() != null && log.getQueueDate() != null) {
                metrics[6] += (log.getFinishDate().getTime() - log.getQueueDate().getTime());
            }
            if (log.getConsumeDate() != null && log.getQueueDate() != null) {
                metrics[7] += (log.getConsumeDate().getTime() - log.getQueueDate().getTime());
            }
            if (log.getRunDate() != null && log.getConsumeDate() != null) {
                metrics[8] += (log.getRunDate().getTime() - log.getConsumeDate().getTime());
            }
            if (log.getFinishDate() != null && log.getRunDate() != null) {
                metrics[9] += (log.getFinishDate().getTime() - log.getRunDate().getTime());
            }
        }
        // 更新metrics数据。
        for (Map.Entry<String, long[]> kv : statsMap.entrySet()) {
            long[] metrics = kv.getValue();
            // 更新metric统计信息
            if (metrics[0] > 0) {
                // 执行的总数量+1
                taskMetricsService.runnerCounterAddAndGet(kv.getKey() + "." + "numAll", metrics[0]);
            }
            if (metrics[1] > 0) {
                // 失败的总数量+1
                taskMetricsService.runnerCounterAddAndGet(kv.getKey() + "." + "numFail", metrics[1]);
            }
            if (metrics[2] > 0) {
                // 程序失败的总数量+1
                taskMetricsService.runnerCounterAddAndGet(kv.getKey() + "." + "numFailProgram", metrics[2]);
            }
            if (metrics[3] > 0) {
                // 设置失败的总数量+1
                taskMetricsService.runnerCounterAddAndGet(kv.getKey() + "." + "numFailConfig", metrics[3]);
            }
            if (metrics[4] > 0) {
                // 接口失败的总数量+1
                taskMetricsService.runnerCounterAddAndGet(kv.getKey() + "." + "numFailPartner", metrics[4]);
            }
            if (metrics[5] > 0) {
                // 数据失败的总数量+1
                taskMetricsService.runnerCounterAddAndGet(kv.getKey() + "." + "numFailData", metrics[5]);
            }
            if (metrics[6] > 0) {
                // 总消耗时间
                taskMetricsService.runnerCounterAddAndGet(kv.getKey() + "." + "timeAll", metrics[6]);
            }
            if (metrics[7] > 0) {
                // 队列传输时间
                taskMetricsService.runnerCounterAddAndGet(kv.getKey() + "." + "timeQueue", metrics[7]);
            }
            if (metrics[8] > 0) {
                // 消费时间
                taskMetricsService.runnerCounterAddAndGet(kv.getKey() + "." + "timeWait", metrics[8]);
            }
            if (metrics[9] > 0) {
                // 执行时间
                taskMetricsService.runnerCounterAddAndGet(kv.getKey() + "." + "timeRun", metrics[9]);
            }
        }
        // 写入日志服务器
        logClient.bulkLog(runnerLogData);
    }

    /**
     * 统计信息。
     *
     * @author axeon
     *
     */
    static class CronerStatsInfo {
        long[] metrics = new long[8];

        Date nextRunDate = null;
    }

    /**
     * 定期批量写CronerLog数据到日志服务器
     */
    public void sendCronerLogToServer() {
        // 从中获得list数据。
        List<TaskCronerLog> cronerLogData = null;
        cronerLogLock.lock();
        try {
            if (cronerLogList.size() > 0) {
                cronerLogData = cronerLogList;
                cronerLogList = Lists.newArrayList();
            }
        } finally {
            cronerLogLock.unlock();
        }
        if (cronerLogData == null){
            return;
        }
        HashMap<String, CronerStatsInfo> statsMap = Maps.newHashMap();
        for (TaskCronerLog log : cronerLogData) {
            // numAll,numFail,numFailProgram,numFailPartner,numFailData,timeAll,timeWait,timeRun
            String key = TaskMetaInfoManager.getCronerLogKey(log);
            CronerStatsInfo statsInfo = statsMap.computeIfAbsent(key,pk -> new CronerStatsInfo());
            statsInfo.metrics[0] += 1;
            if (log.getState() > 1) {
                if (log.getState() == 2) {
                    statsInfo.metrics[2] += 1;
                    statsInfo.metrics[1] += 1;
                } else if (log.getState() == 4) {
                    statsInfo.metrics[3] += 1;
                    statsInfo.metrics[1] += 1;
                } else if (log.getState() == 5) {
                    statsInfo.metrics[4] += 1;
                    statsInfo.metrics[1] += 1;
                }
            }
            if (log.getFinishDate() != null && log.getScheduleDate() != null) {
                statsInfo.metrics[5] += (log.getFinishDate().getTime() - log.getScheduleDate().getTime());
            }
            if (log.getRunDate() != null && log.getScheduleDate() != null) {
                statsInfo.metrics[6] += (log.getRunDate().getTime() - log.getScheduleDate().getTime());
            }
            if (log.getFinishDate() != null && log.getRunDate() != null) {
                statsInfo.metrics[7] += (log.getFinishDate().getTime() - log.getRunDate().getTime());
            }
            statsInfo.nextRunDate = log.getNextDate();
        }
        // 更新metrics数据。
        for (Map.Entry<String, CronerStatsInfo> kv : statsMap.entrySet()) {
            CronerStatsInfo statsInfo = kv.getValue();
            // 更新metric统计信息
            if (statsInfo.metrics[0] > 0) {
                // 执行的总数量+1
                taskMetricsService.cronerCounterAddAndGet(kv.getKey() + "." + "numAll", statsInfo.metrics[0]);
            }
            if (statsInfo.metrics[1] > 0) {
                // 失败的总数量+1
                taskMetricsService.cronerCounterAddAndGet(kv.getKey() + "." + "numFail", statsInfo.metrics[1]);
            }
            if (statsInfo.metrics[2] > 0) {
                // 程序失败的总数量+1
                taskMetricsService.cronerCounterAddAndGet(kv.getKey() + "." + "numFailProgram", statsInfo.metrics[2]);
            }
            if (statsInfo.metrics[3] > 0) {
                // 接口失败的总数量+1
                taskMetricsService.cronerCounterAddAndGet(kv.getKey() + "." + "numFailPartner", statsInfo.metrics[3]);
            }
            if (statsInfo.metrics[4] > 0) {
                // 数据失败的总数量+1
                taskMetricsService.cronerCounterAddAndGet(kv.getKey() + "." + "numFailData", statsInfo.metrics[4]);
            }
            if (statsInfo.metrics[5] > 0) {
                // 总消耗时间
                taskMetricsService.cronerCounterAddAndGet(kv.getKey() + "." + "timeAll", statsInfo.metrics[5]);
            }
            if (statsInfo.metrics[6] > 0) {
                // 队列传输时间
                taskMetricsService.cronerCounterAddAndGet(kv.getKey() + "." + "timeWait", statsInfo.metrics[6]);
            }
            if (statsInfo.metrics[7] > 0) {
                // 执行时间
                taskMetricsService.cronerCounterAddAndGet(kv.getKey() + "." + "timeRun", statsInfo.metrics[7]);
            }
        }
        // 写入日志服务器
        logClient.bulkLog(cronerLogData);
    }
}
