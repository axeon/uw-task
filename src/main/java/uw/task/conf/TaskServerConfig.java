package uw.task.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import uw.task.TaskCroner;
import uw.task.TaskRunner;
import uw.task.api.TaskAPI;
import uw.task.container.TaskCronerContainer;
import uw.task.container.TaskRunnerContainer;
import uw.task.entity.TaskContact;
import uw.task.entity.TaskCronerConfig;
import uw.task.entity.TaskRunnerConfig;
import uw.task.util.TaskMessageConverter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据从服务器端加载的信息来配置服务和任务项。
 *
 * @param
 * @author axeon
 */
public class TaskServerConfig {

    private static final Logger log = LoggerFactory.getLogger(TaskServerConfig.class);

    private ApplicationContext context;

    private ConnectionFactory taskConnectionFactory;

    private TaskRunnerContainer taskRunnerContainer;

    private TaskCronerContainer taskCronerContainer;

    private TaskProperties taskProperties;

    private TaskAPI taskAPI;

    private RabbitAdmin rabbitAdmin;

    /**
     * container缓存。key=队列名,value=container。
     */
    private ConcurrentHashMap<String, SimpleMessageListenerContainer> runnerContainerMap = new ConcurrentHashMap<>();

    /**
     * 启动时间。
     */
    private long startTime = System.currentTimeMillis();

    /**
     * 上次更新配置时间，初始值必须=0，用于标识整体加载。
     */
    private long lastUpdateTime = 0;

    /**
     * 队列名更新时间。
     */
    private long queueUpdateTime = 0;

    /**
     * 从服务器端拉取数据是否成功。
     */
    private boolean updateFlag = true;

    /**
     * 是否首次启动
     */
    private boolean isFirstRun = true;

    /**
     * 默认构造器
     *
     * @param taskProperties
     * @param taskRabbitConnectionFactory
     * @param taskRunnerContainer
     * @param taskCronerContainer
     * @param taskAPI
     * @param rabbitAdmin
     */
    public TaskServerConfig(ApplicationContext context, TaskProperties taskProperties,
                            ConnectionFactory taskRabbitConnectionFactory, TaskRunnerContainer taskRunnerContainer,
                            TaskCronerContainer taskCronerContainer, TaskAPI taskAPI, RabbitAdmin rabbitAdmin) {
        this.context = context;
        this.taskProperties = taskProperties;
        this.taskConnectionFactory = taskRabbitConnectionFactory;
        this.taskRunnerContainer = taskRunnerContainer;
        this.taskCronerContainer = taskCronerContainer;
        this.taskAPI = taskAPI;
        this.rabbitAdmin = rabbitAdmin;
    }

    /**
     * 启动时执行一次。
     */
    public void init() {
        initCronerMap();
        initRunnerMap();
    }

    /**
     * 是否开启任务注册。
     *
     * @return
     */
    public boolean isEnableTaskRegistry() {
        return taskProperties.isEnableTaskRegistry();
    }

    /**
     * 更新主机状态。10秒更新一次。
     */
    public void updateStatus() {
        if (taskProperties.isEnableTaskRegistry()) {
            if (log.isDebugEnabled()) {
                log.debug("正在提交主机状态报告...");
            }
            taskAPI.updateHostStatus();
        }
    }

    /**
     * 5分钟加载一次。 用于同步当前系统内的队列资源。
     */
    public void loadSysQueue() {
        List<TaskRunnerConfig> list = taskAPI.getTaskRunnerQueueList(queueUpdateTime);
        if (list != null) {
            for (TaskRunnerConfig config : list) {
                TaskMetaInfoManager.updateSysQueue(config);
            }
            queueUpdateTime = System.currentTimeMillis();
        }
    }

    /**
     * 注册当前所有的服务。 每隔1分钟刷新一次。
     */
    public void updateConfig() {

        if (!taskProperties.isEnableTaskRegistry()) {
            return;
        }
        long startUpdateTimeMills = System.currentTimeMillis();

        // 先拉主机配置，因为taskScheduler调用需要
        if (log.isDebugEnabled()) {
            log.debug("正在拉取主机配置...");
        }
        List<String> newTargetConfig = taskAPI.getServerTargetConfig();

        // 取得有变化的croner配置列表
        ConcurrentHashMap<String, TaskCronerConfig> updatedCronerConfigMap = updateCronerConfig();
        if (!updateFlag) {
            log.error("拉取TaskCroner服务器配置失败，进入Fail-Fast模式!");
            updateFlag = true;
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("发现服务器端拉取的[{}]个更新的TaskCroner配置...", updatedCronerConfigMap.size());
        }
        // 取得有变化的runner配置列表
        ConcurrentHashMap<String, TaskRunnerConfig> updatedRunnerConfigMap = updateRunnerConfig();
        if (!updateFlag) {
            log.error("拉取TaskRunner服务器配置失败，进入Fail-Fast模式!");
            updateFlag = true;
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("发现服务器端拉取的[{}]个更新的TaskRunner配置...", updatedRunnerConfigMap.size());
        }

        // 检查有没有需要删除的
        if (TaskMetaInfoManager.targetConfig != null) {
            for (String config : TaskMetaInfoManager.targetConfig) {
                if (!newTargetConfig.contains(config)) {
                    // 这个需要删除掉队列监听了。
                    if (log.isDebugEnabled()) {
                        log.debug("正在删除主机配置: [{}]对应的队列监听...", config);
                    }
                    try {
                        for (Entry<String, SimpleMessageListenerContainer> kv : runnerContainerMap.entrySet()) {
                            if (kv.getKey().endsWith("$" + config)) {
                                kv.getValue().stop();
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }

        // 检查是否有新增
        for (String host : newTargetConfig) {
            if (TaskMetaInfoManager.targetConfig != null && !TaskMetaInfoManager.targetConfig.contains(host)) {
                if (log.isDebugEnabled()) {
                    log.debug("正在新建主机配置: [{}]对应的队列监听...", host);
                }
                // 让croner&runner重新加载。
                updatedCronerConfigMap = TaskMetaInfoManager.cronerConfigMap;
                updatedRunnerConfigMap = TaskMetaInfoManager.runnerConfigMap;
            }
        }

        // 设置状态
        TaskMetaInfoManager.targetConfig = newTargetConfig;

        // 第一次执行初始化操作
        if (isFirstRun) {
            isFirstRun = false;
            // 初始化croner和runner
            for (Entry<String, TaskCroner> kv : TaskMetaInfoManager.cronerMap.entrySet()) {
                try {
                    // 拿到任务类名
                    TaskCroner tc = kv.getValue();
                    String taskClass = tc.getClass().getName();
                    if (log.isDebugEnabled()) {
                        log.debug("正在初始化TaskCroner: [{}]", taskClass);
                    }
                    TaskCronerConfig localConfig = tc.initConfig();
                    TaskContact contact = tc.initContact();
                    if (localConfig == null || contact == null) {
                        log.error("TaskCroner: [{}]默认配置或联系人信息为空，无法启动！", taskClass);
                        continue;
                    }
                    // 防止有人瞎胡搞
                    localConfig.setTaskParam("");
                    localConfig.setTaskClass(taskClass);
                    contact.setTaskClass(taskClass);
                    String configKey = TaskMetaInfoManager.getCronerConfigKey(localConfig);
                    TaskCronerConfig serverConfig = updatedCronerConfigMap.get(configKey);
                    // 上传配置
                    if (serverConfig == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("TaskCroner: [{}]未找到服务器端配置，上传默认配置...", taskClass);
                        }
                        serverConfig = uploadCronerInfo(localConfig, contact);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            // 获得当前主机上所有的TaskRunner
            for (Entry<String, TaskRunner> kv : TaskMetaInfoManager.runnerMap.entrySet()) {
                try {
                    // 拿到任务类名
                    TaskRunner<?, ?> tr = kv.getValue();
                    String taskClass = kv.getKey();
                    TaskRunnerConfig localConfig = tr.initConfig();
                    TaskContact contact = tr.initContact();
                    if (localConfig == null || contact == null) {
                        log.error("TaskRunner: [{}]默认配置或联系人信息为空，无法启动！", taskClass);
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("正在初始化TaskRunner: [{}]", taskClass);
                    }
                    localConfig.setTaskClass(taskClass);
                    contact.setTaskClass(taskClass);
                    String configKey = TaskMetaInfoManager.getRunnerConfigKey(localConfig);
                    TaskRunnerConfig serverConfig = updatedRunnerConfigMap.get(configKey);
                    // 上传配置
                    if (serverConfig == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("TaskRunner: [{}]未找到服务器端配置，上传默认配置...", taskClass);
                        }
                        serverConfig = uploadRunnerInfo(localConfig, contact);
                    }
                    // 构建类型，放在最后，防止出幺蛾子
                    TaskMessageConverter.constructTaskDataType(taskClass, kv.getValue());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            // 当未连接服务器的时候，必须用configMap。
            updatedCronerConfigMap = TaskMetaInfoManager.cronerConfigMap;
            updatedRunnerConfigMap = TaskMetaInfoManager.runnerConfigMap;
        }

        // 此时执行更新操作
        if (updatedCronerConfigMap != null) {
            for (TaskCronerConfig tcc : updatedCronerConfigMap.values()) {
                //必须是本项目任务，否则跳过
                if (!tcc.getTaskClass().startsWith(taskProperties.getProject())) {
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug("正在加载ID: [{}],TaskCroner: [{}],CRON: [{}],", tcc.getId(), tcc.getTaskClass(), tcc.getTaskCron());
                }
                TaskCroner tc = TaskMetaInfoManager.cronerMap.get(tcc.getTaskClass());
                if (tc != null) {
                    try {
                        // 对于非运行主机的配置，直接关闭。
                        if (!TaskMetaInfoManager.targetConfig.contains(tcc.getRunTarget())) {
                            tcc.setState(0);
                        }
                        taskCronerContainer.configureTask(tc, tcc);
                        if (log.isDebugEnabled()) {
                            log.debug("TaskCroner: [{}]正在设定新配置...", TaskMetaInfoManager.getCronerConfigKey(tcc));
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                } else {
                    log.warn("TaskCroner: [{}]更新配置时，未找到匹配的任务类!", tcc.getTaskClass());
                }
            }
        }
        // 改Runner配置，逐一循环并配置。
        if (updatedRunnerConfigMap != null) {
            for (TaskRunnerConfig trc : updatedRunnerConfigMap.values()) {
                //必须是本项目任务，否则跳过
                if (!trc.getTaskClass().startsWith(taskProperties.getProject())) {
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug("正在加载ID: [{}], TaskRunner: [{}]", trc.getId(), trc.getTaskClass());
                }
                if (TaskMetaInfoManager.runnerMap.containsKey(trc.getTaskClass())) {
                    // 启动任务
                    if (TaskMetaInfoManager.targetConfig.contains(trc.getRunTarget())) {
                        registerRunner(trc);
                    }
                } else {
                    log.warn("TaskRunner: [{}]更新配置时，未找到匹配的任务类!", trc.getTaskClass());
                }
            }
        }
        // 执行成功才会更新时间戳
        if (updateFlag) {
            // 比对数据库的时候，因为数据库缺少ms值，所以减去5000，提高匹配度。
            lastUpdateTime = startUpdateTimeMills - 5000;
        }
        // 最后重置一下状态为成功。
        updateFlag = true;
    }

    /**
     * 初始化conerMap。 因为getBeansOfType出来的key不对。
     */
    private void initCronerMap() {
        // 设置当前主机上所有的TaskCroner
        Map<String, TaskCroner> cronerInstanceMap = context.getBeansOfType(TaskCroner.class);
        for (Entry<String, TaskCroner> kv : cronerInstanceMap.entrySet()) {
            // 拿到任务类名
            TaskCroner tc = kv.getValue();
            String taskClass = tc.getClass().getName();
            TaskMetaInfoManager.cronerMap.put(taskClass, tc);
        }
    }

    /**
     * 初始化conerMap。 因为getBeansOfType出来的key不对。
     */
    private void initRunnerMap() {
        // 设置当前主机上所有的TaskCroner
        Map<String, TaskRunner> runnerInstanceMap = context.getBeansOfType(TaskRunner.class);
        for (Entry<String, TaskRunner> kv : runnerInstanceMap.entrySet()) {
            // 拿到任务类名
            TaskRunner tr = kv.getValue();
            String taskClass = tr.getClass().getName();
            TaskMetaInfoManager.runnerMap.put(taskClass, tr);
        }
    }

    /**
     * 更新所有Croner的配置
     *
     * @return
     */
    private ConcurrentHashMap<String, TaskCronerConfig> updateCronerConfig() {
        ConcurrentHashMap<String, TaskCronerConfig> map = new ConcurrentHashMap<>();
        List<TaskCronerConfig> list = taskAPI.getTaskCronerConfigList(taskProperties.getProject(), lastUpdateTime);
        if (list != null) {
            for (TaskCronerConfig config : list) {
                String key = TaskMetaInfoManager.getCronerConfigKey(config);
                map.put(key, config);
                TaskMetaInfoManager.cronerConfigMap.put(key, config);
            }
        } else {
            updateFlag = false;
        }
        return map;
    }

    /**
     * 更新所有Runner的配置
     *
     * @return
     */
    private ConcurrentHashMap<String, TaskRunnerConfig> updateRunnerConfig() {
        ConcurrentHashMap<String, TaskRunnerConfig> map = new ConcurrentHashMap<>();
        List<TaskRunnerConfig> list = taskAPI.getTaskRunnerConfigList(taskProperties.getProject(), lastUpdateTime);
        if (list != null) {
            for (TaskRunnerConfig config : list) {
                String key = TaskMetaInfoManager.getRunnerConfigKey(config);
                map.put(key, config);
                TaskMetaInfoManager.runnerConfigMap.put(key, config);
            }
        } else {
            updateFlag = false;
        }
        return map;
    }

    /**
     * 上传Runner信息。
     *
     * @param config
     * @param contact
     */
    private TaskRunnerConfig uploadRunnerInfo(TaskRunnerConfig config, TaskContact contact) {
        config = taskAPI.initTaskRunnerConfig(config);
        taskAPI.initTaskContact(contact);
        TaskMetaInfoManager.runnerConfigMap.put(TaskMetaInfoManager.getRunnerConfigKey(config), config);
        return config;
    }

    /**
     * 上传Croner信息。
     *
     * @param config
     * @param contact
     */
    private TaskCronerConfig uploadCronerInfo(TaskCronerConfig config, TaskContact contact) {
        config = taskAPI.initTaskCronerConfig(config);
        taskAPI.initTaskContact(contact);
        TaskMetaInfoManager.cronerConfigMap.put(TaskMetaInfoManager.getCronerConfigKey(config), config);
        return config;
    }

    /**
     * 注册单个任务
     *
     * @param runnerConfig
     */
    private void registerRunner(TaskRunnerConfig runnerConfig) {
        String queueName = TaskMetaInfoManager.getRunnerConfigKey(runnerConfig);
        SimpleMessageListenerContainer sysContainer = runnerContainerMap.computeIfAbsent(queueName, key -> {
            if (runnerConfig.getState() != 1) {
                log.warn("TaskRunner: [{}]状态为暂停，不进行注册。。。", queueName);
                return null;
            }
            if (log.isDebugEnabled()) {
                log.debug("TaskRunner: [{}]正在注册并启动监听...", queueName);
            }
            try {
                // 定义队列
                rabbitAdmin.declareQueue(new Queue(queueName, true));
                // 定义交换机
                rabbitAdmin.declareExchange(ExchangeBuilder.directExchange(queueName).durable(true).build());
                // 绑定
                rabbitAdmin
                        .declareBinding(new Binding(queueName, DestinationType.QUEUE, queueName, queueName, null));
                // 启动任务监听器
                SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
                container.setAutoStartup(false);
                container.setTaskExecutor(new SimpleAsyncTaskExecutor(queueName));
                // 提高启动consumer速度。
                container.setStartConsumerMinInterval(1000);
                container.setConsecutiveActiveTrigger(3);
                container.setStopConsumerMinInterval(20000);
                container.setConsecutiveIdleTrigger(3);
                container.setMaxConcurrentConsumers(runnerConfig.getConsumerNum());
                container.setConcurrentConsumers((int) Math.ceil(runnerConfig.getConsumerNum() * 0.1f));
                container.setPrefetchCount(runnerConfig.getPrefetchNum());
                container.setConnectionFactory(taskConnectionFactory);
                container.setAcknowledgeMode(AcknowledgeMode.AUTO);
                container.setQueueNames(queueName);
                MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(taskRunnerContainer, "process");
                // listenerAdapter.setReplyPostProcessor(new
                // GZipPostProcessor());
                listenerAdapter.setMessageConverter(new TaskMessageConverter());
                container.setMessageListener(listenerAdapter);
                container.setMessageConverter(new TaskMessageConverter());
                // container.setAfterReceivePostProcessors(new
                // GUnzipPostProcessor());
                container.setAutoStartup(true);
                container.afterPropertiesSet();
                container.start();
                return container;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return null;
        });
        if (sysContainer != null) {
            if (runnerConfig.getState() == 1) {
                try {
                    sysContainer.setMaxConcurrentConsumers(runnerConfig.getConsumerNum());
                    sysContainer.setConcurrentConsumers((int) Math.ceil(runnerConfig.getConsumerNum() * 0.1f));
                    sysContainer.setPrefetchCount(runnerConfig.getPrefetchNum());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                runnerContainerMap.remove(queueName);
                sysContainer.shutdown();
                sysContainer.stop();
            }
        }
    }

    /**
     * 停止所有的任务。
     */
    public void stopAllTaskRunner() {
        for (SimpleMessageListenerContainer container : runnerContainerMap.values()) {
            container.shutdown();
            container.stop();
        }
    }

}
