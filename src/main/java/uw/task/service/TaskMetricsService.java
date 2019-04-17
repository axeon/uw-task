package uw.task.service;

import com.google.common.collect.Maps;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import java.util.Map;

/**
 * Task统计服务
 *
 * @author liliang
 * @since 2018-04-28
 */
public class TaskMetricsService {

    /**
     * Runner原子统计的KV映射
     */
    private final Map<String, RedisAtomicLong> runnerAtomicIncrementMap = Maps.newConcurrentMap();

    /**
     * Croner原子统计的KV映射
     */
    private final Map<String, RedisAtomicLong> cronerAtomicIncrementMap = Maps.newConcurrentMap();

    /**
     * 用于统计Runner的Redis Key前缀
     */
    private static final String METRICS_RUNNER_REDIS_TAG = "MetricsRunner:";

    /**
     * 用于统计Croner的Redis Key前缀
     */
    private static final String METRICS_CRONER_REDIS_TAG = "MetricsCroner:";

    /**
     * runner数据表,与uw-task-center 共享,于服务端统计,并提供查询接口
     */
    private static final String METRICS_RUNNER_SET_REDIS_TAG = "MetricsRunnerSet";

    /**
     * runner数据表,与uw-task-center 共享,于服务端统计,并提供查询接口
     */
    private static final String METRICS_CRONER_SET_REDIS_TAG = "MetricsCronerSet";

    /**
     * Redis客户端
     */
    private StringRedisTemplate template;

    public TaskMetricsService(StringRedisTemplate template) {
        this.template = template;
    }

    /**
     * Runner原子增值
     *
     * @param name
     * @param addNum
     * @return
     */
    public long runnerCounterAddAndGet(String name, long addNum) {
        RedisAtomicLong counter = runnerAtomicIncrementMap.computeIfAbsent(name,
                pk -> {
                    // 向列表中加入key,用于入influxdb
                    template.boundSetOps(METRICS_RUNNER_SET_REDIS_TAG).add(name);
                    return new RedisAtomicLong(METRICS_RUNNER_REDIS_TAG + name,template.getConnectionFactory());
                });
        return counter.addAndGet(addNum);
    }

    /**
     * Croner原子增值
     *
     * @param name
     * @param addNum
     * @return
     */
    public long cronerCounterAddAndGet(String name, long addNum) {
        RedisAtomicLong counter = cronerAtomicIncrementMap.computeIfAbsent(name,
                pk -> {
                    // 向列表中加入key,用于入influxdb
                    template.boundSetOps(METRICS_CRONER_SET_REDIS_TAG).add(name);
                    return new RedisAtomicLong(METRICS_CRONER_REDIS_TAG + name,template.getConnectionFactory());
                });
        return counter.addAndGet(addNum);
    }
}
