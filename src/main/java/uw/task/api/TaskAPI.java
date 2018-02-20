package uw.task.api;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import uw.task.TaskData;
import uw.task.conf.TaskProperties;
import uw.task.entity.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 对应服务器端的API接口实现。
 *
 * @author axeon
 */
public class TaskAPI {

    private static final Logger log = LoggerFactory.getLogger(TaskAPI.class);

    /**
     * Task配置文件
     */
    private TaskProperties taskProperties;

    /**
     * Rest模板类
     */
    private RestTemplate restTemplate;

    /**
     * 专门给日志发送使用的线程池。
     */
    private ExecutorService taskLogService = null;

    /**
     * 本机的外网IP
     */
    private String hostIp = "";

    public TaskAPI(final TaskProperties taskProperties, final RestTemplate restTemplate) {
        this.taskProperties = taskProperties;
        this.restTemplate = restTemplate;
        this.taskLogService = new ThreadPoolExecutor(taskProperties.getTaskLogMinThreadNum(), taskProperties.getTaskLogMaxThreadNum(), 20L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("TaskLog-%d").build(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * @return the hostIp
     */
    public String getHostIp() {
        return hostIp;
    }

    /**
     * 更新当前主机目标配置。
     *
     * @return
     */
    public List<String> getServerTargetConfig() {
        List<String> targetConfig = null;
        // 判断是否是私有模式。
        boolean privacyMode = taskProperties.isPrivacyMode();
        try {
            String[] data = restTemplate.getForObject(
                    taskProperties.getServerHost() + "/taskapi/target/config?targetType={targetType}", String[].class,
                    privacyMode ? 1 : 0);
            targetConfig = Arrays.asList(data);
        } catch (Exception e) {
            log.error("TaskAPI.getHostConfig()服务端Target配置拉取异常:{}", e.getMessage());
        }
        if (targetConfig == null || targetConfig.size() == 0) {
            if (log.isWarnEnabled()) {
                log.warn("获得主机Target服务端配置失败，启用默认配置项!");
            }
            targetConfig = new ArrayList<>();
            if (!privacyMode) {
                targetConfig.add("");
            }
        }
        return targetConfig;
    }

    /**
     * 更新当前主机状态，返回主机IP地址。
     *
     * @return 主机IP地址
     */
    public String updateHostStatus() {
        String ip = "";
        TaskHostStatus taskHostStatus = new TaskHostStatus();
        taskHostStatus.setHostId(taskProperties.getHostId());
        taskHostStatus.setTaskProject(taskProperties.getProject());
        try {
            ip = restTemplate.postForObject(taskProperties.getServerHost() + "/taskapi/host/status", taskHostStatus,
                    String.class);
            hostIp = ip;
        } catch (Exception e) {
            log.error("TaskAPI.updateHostStatus()服务端主机状态更新异常:{}", e.getMessage());
        }
        return ip;
    }

    /**
     * 初始化CronerConfig。
     *
     * @param config
     */
    public TaskCronerConfig initTaskCronerConfig(TaskCronerConfig config) {
        try {
            config = restTemplate.postForObject(taskProperties.getServerHost() + "/taskapi/croner/config", config,
                    TaskCronerConfig.class);
        } catch (Exception e) {
            log.error("TaskAPI.initTaskCronerConfig上传Croner配置到服务端异常:{}", e.getMessage());
        }
        return config;
    }

    /**
     * 初始化RunnerConfig
     *
     * @param config
     */
    public TaskRunnerConfig initTaskRunnerConfig(TaskRunnerConfig config) {
        try {
            config = restTemplate.postForObject(taskProperties.getServerHost() + "/taskapi/runner/config", config,
                    TaskRunnerConfig.class);
        } catch (Exception e) {
            log.error("TaskAPI.initTaskRunnerConfig上传Runner配置到服务端异常:{}", e.getMessage());
        }
        return config;

    }

    /**
     * 初始化联系人信息。
     *
     * @param contact
     */
    public void initTaskContact(TaskContact contact) {
        try {
            restTemplate.postForLocation(taskProperties.getServerHost() + "/taskapi/contact", contact);
        } catch (Exception e) {
            log.error("TaskAPI.initTaskContact上传联系人信息到服务端异常:{}", e.getMessage());
        }
    }

    /**
     * 根据包名前缀获得TaskRunner队列列表。
     *
     * @param lastUpdateTime 最后更新时间
     * @return
     */
    public List<TaskRunnerConfig> getTaskRunnerQueueList(long lastUpdateTime) {
        List<TaskRunnerConfig> list = null;
        try {
            TaskRunnerConfig[] data = restTemplate.getForObject(
                    taskProperties.getServerHost() + "/taskapi/runner/queue?lastUpdateTime={lastUpdateTime}",
                    TaskRunnerConfig[].class, lastUpdateTime);
            list = Arrays.asList(data);
        } catch (Exception e) {
            log.error("TaskAPI.getTaskRunnerQueueList()服务端主机状态更新异常:{}", e.getMessage());
        }

        return list;
    }

    /**
     * 根据包名前缀获得TaskRunner配置列表。
     *
     * @param taskPackage
     * @param lastUpdateTime 最后更新时间
     * @return
     */
    public List<TaskRunnerConfig> getTaskRunnerConfigList(String taskPackage, long lastUpdateTime) {
        List<TaskRunnerConfig> list = null;
        try {
            TaskRunnerConfig[] data = restTemplate.getForObject(
                    taskProperties.getServerHost()
                            + "/taskapi/runner/config?taskPackage={taskPackage}&lastUpdateTime={lastUpdateTime}",
                    TaskRunnerConfig[].class, taskPackage, lastUpdateTime);
            list = Arrays.asList(data);
        } catch (Exception e) {
            log.error("TaskAPI.getTaskRunnerConfigList()服务端主机状态更新异常:{}", e.getMessage());
        }

        return list;
    }

    /**
     * 根据包名前缀获得TaskCroner配置列表。
     *
     * @param taskPackage
     * @param lastUpdateTime 最后更新时间
     * @return
     */
    public List<TaskCronerConfig> getTaskCronerConfigList(String taskPackage, long lastUpdateTime) {
        List<TaskCronerConfig> list = null;
        try {
            TaskCronerConfig[] data = restTemplate.getForObject(
                    taskProperties.getServerHost()
                            + "/taskapi/croner/config?taskPackage={taskPackage}&lastUpdateTime={lastUpdateTime}",
                    TaskCronerConfig[].class, taskPackage, lastUpdateTime);
            list = Arrays.asList(data);
        } catch (Exception e) {
            log.error("TaskAPI.getTaskCronerConfigList()服务端主机状态更新异常:{}", e.getMessage());
        }

        return list;
    }

    /**
     * 发送Runner任务日志。
     *
     * @param taskData
     */
    public void sendTaskRunnerLog(TaskData<?, ?> taskData) {
        taskLogService.submit(() -> {
            try {
                restTemplate.postForLocation(taskProperties.getServerHost() + "/taskapi/runner/log", taskData);
            } catch (Exception e) {
                log.error("TaskAPI.sendTaskRunnerLog日志发送到服务端异常:{}", e.getMessage());
            }
        });

    }

    /**
     * 发送CronLog日志。
     *
     * @param taskCronerLog
     */
    public void sendTaskCronerLog(TaskCronerLog taskCronerLog) {
        taskLogService.submit(() -> {
            try {
                restTemplate.postForLocation(taskProperties.getServerHost() + "/taskapi/croner/log", taskCronerLog);
            } catch (Exception e) {
                log.error("TaskAPI.sendTaskCronerLog日志发送到服务端异常:{}", e.getMessage());
            }
        });
    }

}
