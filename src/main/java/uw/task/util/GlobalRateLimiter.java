package uw.task.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 基于redis实现的限速管理器。
 *
 * @author axeon
 */
public class GlobalRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(GlobalRateLimiter.class);

    private static final String REDIS_TAG = "_RATE_LIMIT_";

    private final RedisTemplate<String, Long> redisTemplate;

    private ConcurrentHashMap<String, RedisRateLimiter> map = new ConcurrentHashMap<>();

    public GlobalRateLimiter(final RedisConnectionFactory redisConnectionFactory) {
        redisTemplate = new RedisTemplate<String, Long>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<Long>(Long.class));
        redisTemplate.setExposeConnection(true);
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.afterPropertiesSet();
    }

    /**
     * 尝试获得限制允许状态。
     *
     * @param name
     * @return
     */
    public long tryAcquire(String name) {
        long wait = 0;
        try {
            RedisRateLimiter limiter = map.get(name);
            if (limiter != null) {
                wait = limiter.tryAcquire(1);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return wait;
    }

    /**
     * 初始化一个流量限速器。
     *
     * @param name           限速器名称，应是全局唯一的名称
     * @param limitRate      限速速率
     * @param limitTimeUnit  限速时间单位
     * @param limitTimeValue 限速时间数值
     * @return 如果成功则返回true。
     */
    public boolean initLimiter(String name, long limitRate, TimeUnit limitTimeUnit, long limitTimeValue) {
        boolean flag = false;
        try {
            RedisRateLimiter limiter = map.computeIfAbsent(name, key -> new RedisRateLimiter(name, limitRate, limitTimeUnit, limitTimeValue));
            if (limiter!=null) {
                limiter.setLimitRate(limitRate);
                limiter.setLimitTimeUnit(limitTimeUnit);
                limiter.setLimitTimeValue(limitTimeValue);
                flag = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return flag;
    }

    /**
     * redis流量限速器。
     *
     * @author axeon
     */
    class RedisRateLimiter {

        /**
         * 限速器名称
         */
        private String name;
        /**
         * 流量限速
         */
        private long limitRate = 1l;
        /**
         * ValueOperations实例
         */
        private ValueOperations<String, Long> operations;

        /**
         * 限速时间单位
         */
        private TimeUnit limitTimeUnit = TimeUnit.SECONDS;

        /**
         * 限速时间数值。
         */
        private long limitTimeValue = 1;

        /**
         * @return the limitRate
         */
        public long getLimitRate() {
            return limitRate;
        }

        /**
         * @param limitRate the limitRate to set
         */
        public void setLimitRate(long limitRate) {
            this.limitRate = limitRate;
        }

        /**
         * @return the limitTimeUnit
         */
        public TimeUnit getLimitTimeUnit() {
            return limitTimeUnit;
        }

        /**
         * @param limitTimeUnit the limitTimeUnit to set
         */
        public void setLimitTimeUnit(TimeUnit limitTimeUnit) {
            this.limitTimeUnit = limitTimeUnit;
        }

        /**
         * @return the limitTimeValue
         */
        public long getLimitTimeValue() {
            return limitTimeValue;
        }

        /**
         * @param limitTimeValue the limitTimeValue to set
         */
        public void setLimitTimeValue(long limitTimeValue) {
            this.limitTimeValue = limitTimeValue;
        }

        /**
         * 初始化一个流量限速器。
         *
         * @param limitRate      限速速率
         * @param limitTimeUnit  限速时间单位
         * @param limitTimeValue 限速时间数值
         */
        public RedisRateLimiter(String name, long limitRate, TimeUnit limitTimeUnit, long limitTimeValue) {
            super();
            this.name = REDIS_TAG + name;
            this.limitRate = limitRate;
            this.limitTimeUnit = limitTimeUnit;
            this.limitTimeValue = limitTimeValue;
            this.operations = redisTemplate.opsForValue();
        }

        /**
         * 检查是否超限。
         *
         * @param permits
         * @return 如果未超限则返回0，否则返回需要等待的秒数
         */
        synchronized long tryAcquire(int permits) {
            long expire = 0;
            long value = operations.increment(name, permits);
            if (value > 1) {
                if (value > limitRate) {
                    expire = redisTemplate.getExpire(name, TimeUnit.MILLISECONDS);
                    if (expire == -1) {
                        // 需要补刀
                        redisTemplate.expire(name, limitTimeValue, limitTimeUnit);
                        expire = 1000;
                    }
                }
            } else {
                redisTemplate.expire(name, limitTimeValue, limitTimeUnit);
            }
            if (expire <= 0) {
                return 0;
            } else {
                return expire;
            }
        }

    }

}
