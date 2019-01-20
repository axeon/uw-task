package uw.task.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

/**
 * 基于redis实现的分布式序列。
 *
 * @author axeon
 */
public class GlobalSequenceManager {

	private static final Logger log = LoggerFactory.getLogger(GlobalSequenceManager.class);

	private static final String REDIS_TAG = "_SEQUENCE_";

    /**
     * 重试次数。
     */
    private static final int MAX_RETRY_TIMES = 3;

    /**
     * 序列递增步长值
     */
    private static final int SEQUENCE_INCREMENT_NUM = 10000;

	/**
	 * redis定制连接工厂
	 */
	private final RedisConnectionFactory redisConnectionFactory;

	/**
	 * sequenceMap
	 */
	private ConcurrentHashMap<String, RedisSequence> map = new ConcurrentHashMap<>();

	public GlobalSequenceManager(final RedisConnectionFactory redisConnectionFactory) {
		this.redisConnectionFactory = redisConnectionFactory;
	}

	/**
	 * 获得序列值
	 *
	 * @param name
	 * @return
	 */
	public long nextId(String name) {
	    int tryCount = 0;
	    do {
            try {
                RedisSequence redisSequence =  map.computeIfAbsent(name, key -> new RedisSequence(key, SEQUENCE_INCREMENT_NUM));
                return redisSequence.nextId();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                tryCount++;
            }
        } while (tryCount < MAX_RETRY_TIMES);
        return -1;
    }

	/**
	 * redis序列发生器。
	 *
	 * @author axeon
	 */
	class RedisSequence {

		/**
		 * 当前数值
		 */
		private AtomicLong currentId;

		/**
		 * 当前可以获取的最大id
		 */
		private long maxId;

		/**
		 * 增量数
		 */
		private int incrementNum;

		/**
		 * redis计数器
		 */
		private RedisAtomicLong counter;

		/**
		 * 初始化一个序列器
		 *
		 * @param name
		 *            序列名称
		 * @param incrementNum
		 *            增长数
		 */
		private RedisSequence(String name, int incrementNum) {
			this.incrementNum = incrementNum;
			counter = new RedisAtomicLong(REDIS_TAG + name, redisConnectionFactory);
            currentId = new AtomicLong((counter.getAndAdd(incrementNum)));
			maxId = currentId.get() + incrementNum;
		}

		/**
		 * 获得下一个ID
		 *
		 * @return 如果为超限则返回0，否则返回需要等待的秒数
		 */
		synchronized long nextId() {
			long value = currentId.incrementAndGet();
			if (value >= maxId) {
				currentId.set(counter.getAndAdd(incrementNum));
				maxId = currentId.get() + incrementNum;
			}
			return value;
		}
	}
}
