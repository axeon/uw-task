package uw.task.conf;

import io.lettuce.core.resource.ClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
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
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import uw.log.es.LogClient;
import uw.task.TaskListenerManager;
import uw.task.TaskScheduler;
import uw.task.api.TaskAPI;
import uw.task.container.TaskCronerContainer;
import uw.task.container.TaskRunnerContainer;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import uw.task.entity.TaskCronerLog;
import uw.task.entity.TaskRunnerLog;
import uw.task.service.TaskLogService;
import uw.task.service.TaskMetricsService;
import uw.task.util.GlobalRateLimiter;
import uw.task.util.GlobalSequenceManager;
import uw.task.util.LeaderVote;
import uw.task.util.LocalRateLimiter;
import uw.task.util.TaskMessageConverter;

/**
 * 自动装配类 Created by Acris on 2017/5/23.
 * @author Acris,liliang
 */
@Configuration
@EnableScheduling
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
     * 日志服务
     */
    private TaskLogService taskLogService;

    /**
     * 是否已初始化配置，保证只初始化一次；
     */
    private AtomicBoolean initFlag = new AtomicBoolean(false);

    /**
     * 声明 taskScheduler bean
     * @param context
     * @param taskProperties
     * @param restTemplate
     * @param taskListenerManager
     * @param clientResources
     * @param logClient
     * @return
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public TaskScheduler taskScheduler(final ApplicationContext context, final TaskProperties taskProperties,
                                       @Qualifier("tokenRestTemplate") final RestTemplate restTemplate,
                                       final TaskListenerManager taskListenerManager,
                                       final ClientResources clientResources,
                                       final LogClient logClient) {
        // task自定义的rabbit连接工厂
        ConnectionFactory taskRabbitConnectionFactory = getTaskRabbitConnectionFactory(taskProperties.getRabbitmq());
        // task自定义的redis连接工厂
        RedisConnectionFactory taskRedisConnectionFactory = getTaskRedisConnectionFactory(taskProperties.getRedis(), clientResources);
        // 本地限速器。
        LocalRateLimiter localRateLimiter = new LocalRateLimiter();
        // 全局限速器
        GlobalRateLimiter globalRateLimiter = new GlobalRateLimiter(taskRedisConnectionFactory);
        // 全局sequence管理器
        GlobalSequenceManager globalSequenceManager = new GlobalSequenceManager(taskRedisConnectionFactory);
        // Leader选举器
        leaderVote = new LeaderVote(taskRedisConnectionFactory, taskProperties);
        // 日志服务
        logClient.regLogObject(TaskCronerLog.class);
        logClient.regLogObject(TaskRunnerLog.class);
        StringRedisTemplate redisTemplate = new StringRedisTemplate(taskRedisConnectionFactory);
        TaskMetricsService taskMetricsService = new TaskMetricsService(redisTemplate);
        taskLogService = new TaskLogService(logClient,taskMetricsService,taskProperties);
        // taskAPI
        TaskAPI taskAPI = new TaskAPI(taskProperties, restTemplate,taskLogService);
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
        TaskScheduler taskScheduler = new TaskScheduler(taskProperties, rabbitTemplate, taskRunnerContainer, globalSequenceManager);
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
        if (initFlag.compareAndSet(false, true)) {
            serverConfig.init();
        }
    }

    /**
     * 一秒写一次RunnerLog
     */
    @Scheduled(initialDelay = 0, fixedRate = 1000)
    public void writeRunnerLog() {
        taskLogService.sendRunnerLogToServer();
    }

    /**
     * 一秒写一次CronerLog
     */
    @Scheduled(initialDelay = 0, fixedRate = 1000)
    public void writeCronerLog() {
        taskLogService.sendCronerLogToServer();
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
     * @param rabbitProperties RabbitMQ配置
     * @return ConnectionFactory
     */
    private ConnectionFactory getTaskRabbitConnectionFactory(TaskProperties.RabbitProperties rabbitProperties) {
        PropertyMapper map = PropertyMapper.get();
        RabbitConnectionFactoryBean factoryBean = new RabbitConnectionFactoryBean();
        map.from(rabbitProperties::determineHost).whenNonNull().to(factoryBean::setHost);
        map.from(rabbitProperties::determinePort).to(factoryBean::setPort);
        map.from(rabbitProperties::determineUsername).whenNonNull()
                .to(factoryBean::setUsername);
        map.from(rabbitProperties::determinePassword).whenNonNull()
                .to(factoryBean::setPassword);
        map.from(rabbitProperties::determineVirtualHost).whenNonNull()
                .to(factoryBean::setVirtualHost);
        map.from(rabbitProperties::getRequestedHeartbeat).whenNonNull()
                .asInt(Duration::getSeconds).to(factoryBean::setRequestedHeartbeat);
        RabbitProperties.Ssl ssl = rabbitProperties.getSsl();
        if (ssl.isEnabled()) {
            factoryBean.setUseSSL(true);
            map.from(ssl::getAlgorithm).whenNonNull().to(factoryBean::setSslAlgorithm);
            map.from(ssl::getKeyStoreType).to(factoryBean::setKeyStoreType);
            map.from(ssl::getKeyStore).to(factoryBean::setKeyStore);
            map.from(ssl::getKeyStorePassword).to(factoryBean::setKeyStorePassphrase);
            map.from(ssl::getTrustStoreType).to(factoryBean::setTrustStoreType);
            map.from(ssl::getTrustStore).to(factoryBean::setTrustStore);
            map.from(ssl::getTrustStorePassword).to(factoryBean::setTrustStorePassphrase);
        }
        map.from(rabbitProperties::getConnectionTimeout).whenNonNull()
                .asInt(Duration::toMillis).to(factoryBean::setConnectionTimeout);
        try {
            factoryBean.afterPropertiesSet();
        } catch (Exception e) {
            log.error("配置RabbitConnectionFactoryBean出错", e);
        }

        CachingConnectionFactory connFactory = null;
        try {
            connFactory = new CachingConnectionFactory(factoryBean.getObject());
        } catch (Exception e) {
            log.error("获取ConnectionFactory出错", e);
        }
        map.from(rabbitProperties::determineAddresses).to(connFactory::setAddresses);
        map.from(rabbitProperties::isPublisherConfirms).to(connFactory::setPublisherConfirms);
        map.from(rabbitProperties::isPublisherReturns).to(connFactory::setPublisherReturns);
        RabbitProperties.Cache.Channel channel = rabbitProperties.getCache().getChannel();
        map.from(channel::getSize).whenNonNull().to(connFactory::setChannelCacheSize);
        map.from(channel::getCheckoutTimeout).whenNonNull().as(Duration::toMillis)
                .to(connFactory::setChannelCheckoutTimeout);
        RabbitProperties.Cache.Connection connection = rabbitProperties.getCache()
                .getConnection();
        map.from(connection::getMode).whenNonNull().to(connFactory::setCacheMode);
        map.from(connection::getSize).whenNonNull()
                .to(connFactory::setConnectionCacheSize);

        try {
            connFactory.afterPropertiesSet();
        } catch (Exception e) {
            log.error("配置CachingConnectionFactory出错", e);
        }
        return connFactory;
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
        template.setReplyTimeout(180000);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 自定义的redis链接工厂类。
     *
     * @param redisProperties
     * @param clientResources
     * @return
     */
    private RedisConnectionFactory getTaskRedisConnectionFactory(final TaskProperties.RedisProperties redisProperties,
                                                                 final ClientResources clientResources) {
        RedisProperties.Pool pool = redisProperties.getLettuce().getPool();
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder;
        if (pool == null) {
            builder = LettuceClientConfiguration.builder();
        } else {
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxTotal(pool.getMaxActive());
            config.setMaxIdle(pool.getMaxIdle());
            config.setMinIdle(pool.getMinIdle());
            if (pool.getMaxWait() != null) {
                config.setMaxWaitMillis(pool.getMaxWait().toMillis());
            }
            builder = LettucePoolingClientConfiguration.builder().poolConfig(config);
        }

        if (redisProperties.getTimeout() != null) {
            builder.commandTimeout(redisProperties.getTimeout());
        }
        if (redisProperties.getLettuce() != null) {
            RedisProperties.Lettuce lettuce = redisProperties.getLettuce();
            if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
                builder.shutdownTimeout(redisProperties.getLettuce().getShutdownTimeout());
            }
        }
        builder.clientResources(clientResources);
        LettuceClientConfiguration config = builder.build();

        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
        standaloneConfig.setHostName(redisProperties.getHost());
        standaloneConfig.setPort(redisProperties.getPort());
        standaloneConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
        standaloneConfig.setDatabase(redisProperties.getDatabase());

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standaloneConfig, config);
        factory.afterPropertiesSet();
        return factory;
    }
}
