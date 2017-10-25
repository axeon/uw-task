package uw.task.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.JedisPoolConfig;
import uw.task.TaskListenerManager;
import uw.task.TaskScheduler;
import uw.task.api.TaskAPI;
import uw.task.container.TaskCronerContainer;
import uw.task.container.TaskRunnerContainer;
import uw.task.util.*;

import javax.annotation.PreDestroy;

/**
 * 自动装配类 Created by Acris on 2017/5/23.
 */
@Configuration
@EnableConfigurationProperties({TaskProperties.class})
@AutoConfigureAfter({RedisAutoConfiguration.class, RabbitAutoConfiguration.class})
public class TaskAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(TaskAutoConfiguration.class);

    /**
     * 服务端任务配置。
     */
    private TaskServerConfig serverConfig;

    /**
     * 定时任务容器。
     */
    private TaskCronerContainer taskCronerContainer;

    /**
     * Leader选举器
     */
    private LeaderVote leaderVote;

    /**
     * 声明 taskScheduler bean
     *
     * @param context
     * @param taskProperties
     * @param restTemplate
     * @param taskListenerManager
     * @return TaskScheduler
     */
    @Bean
    public TaskScheduler taskScheduler(final ApplicationContext context, final TaskProperties taskProperties,
                                       @Qualifier("tokenRestTemplate") final RestTemplate restTemplate, final TaskListenerManager taskListenerManager) {
        // task自定义的rabbit连接工厂
        ConnectionFactory taskRabbitConnectionFactory = getTaskRabbitConnectionFactory(taskProperties);
        // task自定义的redis连接工厂
        RedisConnectionFactory taskRedisConnectionFactory = getTaskRedisConnectionFactory(taskProperties);
        // 本地限速器。
        LocalRateLimiter localRateLimiter = new LocalRateLimiter();
        // 全局限速器
        GlobalRateLimiter globalRateLimiter = new GlobalRateLimiter(taskRedisConnectionFactory);
        // 全局sequence管理器
        GlobalSequenceManager globalSequenceManager = new GlobalSequenceManager(taskRedisConnectionFactory);
        // Leader选举器
        leaderVote = new LeaderVote(taskRedisConnectionFactory, taskProperties);
        // taskAPI
        TaskAPI taskAPI = new TaskAPI(taskProperties, restTemplate);
        // rabbit模板
        RabbitTemplate rabbitTemplate = getTaskRabbitTemplate(taskRabbitConnectionFactory);
        // rabiit管理器
        RabbitAdmin rabbitAdmin = new RabbitAdmin(taskRabbitConnectionFactory);
        // taskCronerContainer
        taskCronerContainer = new TaskCronerContainer(leaderVote, taskAPI, taskListenerManager,
                globalSequenceManager, taskProperties);
        // taskRunnerContainer
        TaskRunnerContainer taskRunnerContainer = new TaskRunnerContainer(taskProperties, taskAPI, localRateLimiter,
                globalRateLimiter, taskListenerManager);
        // 初始化TaskServerConfig
        serverConfig = new TaskServerConfig(context, taskProperties, taskRabbitConnectionFactory,
                taskRunnerContainer, taskCronerContainer, taskAPI, rabbitAdmin);
        // 返回TaskScheduler
        TaskScheduler taskScheduler =  new TaskScheduler(taskProperties, rabbitTemplate, taskRunnerContainer, globalSequenceManager);
        // taskRunnerContainer错误重试需要TaskScheduler
        taskRunnerContainer.setTaskScheduler(taskScheduler);
        return taskScheduler;
    }

    /**
     * 声明任务监听管理器bean
     *
     * @return TaskListenerManager
     */
    @Bean
    public TaskListenerManager taskListenerManager() {
        return new TaskListenerManager();
    }

    /**
     * ApplicationContext初始化完成或刷新后执行init方法
     */
    @EventListener(ContextRefreshedEvent.class)
    public void handleContextRefresh() {
        serverConfig.init();
    }

    /**
     * 更新主机状态。10秒更新一次。
     */
    @Scheduled(initialDelay = 0, fixedRate = 10000)
    public void updateStatus() {
        serverConfig.updateStatus();
    }

    /**
     * 5分钟加载一次。 用于同步当前系统内的队列资源。
     */
    @Scheduled(fixedRate = 300000)
    public void loadSysQueue() {
        serverConfig.loadSysQueue();
    }

    /**
     * 注册当前所有的服务。 每隔1分钟刷新一次。
     */
    @Scheduled(initialDelay = 0, fixedRate = 60000)
    public void updateConfig() {
        serverConfig.updateConfig();
    }


    /**
     * 20秒检查一次leader状态。
     */
    @Scheduled(initialDelay = 3000, fixedRate = 3000)
    public void batchCheckLeaderStatus() {
        leaderVote.batchCheckLeaderStatus();
    }


    @PreDestroy
    public void destroy() {
        serverConfig.stopAllTaskRunner();
        taskCronerContainer.stopAllTaskCroner();
    }


    /**
     * 获得任务自定义的rabbitConnectionFactory
     *
     * @param taskProperties
     * @return
     */
    private ConnectionFactory getTaskRabbitConnectionFactory(TaskProperties taskProperties) {
        RabbitConnectionFactoryBean factory = new RabbitConnectionFactoryBean();
        if (taskProperties.getRabbitmq().getHost() != null) {
            factory.setHost(taskProperties.getRabbitmq().getHost());
        }
        factory.setPort(taskProperties.getRabbitmq().getPort());
        if (taskProperties.getRabbitmq().getUsername() != null) {
            factory.setUsername(taskProperties.getRabbitmq().getUsername());
        }
        if (taskProperties.getRabbitmq().getPassword() != null) {
            factory.setPassword(taskProperties.getRabbitmq().getPassword());
        }
        if (taskProperties.getRabbitmq().getVirtualHost() != null) {
            factory.setVirtualHost(taskProperties.getRabbitmq().getVirtualHost());
        }
        if (taskProperties.getRabbitmq().getRequestedHeartbeat() != null) {
            factory.setRequestedHeartbeat(taskProperties.getRabbitmq().getRequestedHeartbeat());
        }
        if (taskProperties.getRabbitmq().getConnectionTimeout() != null) {
            factory.setConnectionTimeout(taskProperties.getRabbitmq().getConnectionTimeout());
        }
        try {
            factory.afterPropertiesSet();
        } catch (Exception e) {
            log.error("配置RabbitConnectionFactoryBean出错", e);
        }
        com.rabbitmq.client.ConnectionFactory connectionFactory;

        CachingConnectionFactory cachingConnectionFactory = null;
        try {
            connectionFactory = factory.getObject();
            cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
            cachingConnectionFactory.setPublisherConfirms(taskProperties.getRabbitmq().isPublisherConfirms());
            cachingConnectionFactory.setPublisherReturns(taskProperties.getRabbitmq().isPublisherReturns());
        } catch (Exception e) {
            log.error("获取ConnectionFactory出错", e);
        }
        return cachingConnectionFactory;
    }

    /**
     * 转换器用Jackson2JsonMessageConverter，用于json转换实体类对象。
     *
     * @param connectionFactory
     * @return RabbitTemplate
     */
    private RabbitTemplate getTaskRabbitTemplate(final ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new TaskMessageConverter());
        // template.setBeforePublishPostProcessors(new GZipPostProcessor());
        // template.setAfterReceivePostProcessors(new GUnzipPostProcessor());
        template.setReplyTimeout(180000);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 自定义的redis链接工厂类。
     *
     * @param taskProperties
     * @return
     */
    private RedisConnectionFactory getTaskRedisConnectionFactory(final TaskProperties taskProperties) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(taskProperties.getRedis().getPool().getMaxIdle());
        config.setMinIdle(taskProperties.getRedis().getPool().getMinIdle());
        config.setMaxWaitMillis(taskProperties.getRedis().getPool().getMaxWait());
        config.setMaxTotal(taskProperties.getRedis().getPool().getMaxActive());

        JedisConnectionFactory factory = new JedisConnectionFactory(config);
        factory.setHostName(taskProperties.getRedis().getHost());
        factory.setPort(taskProperties.getRedis().getPort());
        factory.setDatabase(taskProperties.getRedis().getDatabase());
        factory.setTimeout(taskProperties.getRedis().getTimeout());
        if (taskProperties.getRedis().getPassword() != null) {
            factory.setPassword(taskProperties.getRedis().getPassword());
        }
        factory.afterPropertiesSet();
        return factory;
    }
}
