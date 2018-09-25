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
		long seqId = -1;
		try {
			RedisSequence sequence = map.get(name);
			if (sequence == null) {
				initSequence(name, 10000);
				sequence = map.get(name);
			}
			if (sequence != null) {
				seqId = sequence.nextId();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return seqId;
	}

	/**
	 * 初始化一个序列发生器
	 *
	 * @param name
	 *            名称
	 * @param incrementNum
	 *            每次递增的数量
	 * @return
	 */
	public synchronized boolean initSequence(String name, int incrementNum) {
		boolean flag = false;
		try {
			RedisSequence seq = map.get(name);
			if (seq == null) {
				synchronized (map) {
					seq = new RedisSequence(name, incrementNum);
					map.putIfAbsent(name, seq);
				}
				flag = true;
			} else {
				if (seq.getIncrementNum() != incrementNum) {
					seq.setIncrementNum(incrementNum);
				}
				flag = false;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return flag;
	}

	/**
	 * redis序列发生器。
	 *
	 * @author axeon
	 */
	class RedisSequence {

		/**
		 * 序列名称
		 */
		private String name;

		/**
		 * 当前数值
		 */
		private AtomicLong currentId = new AtomicLong(0);

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
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name
		 *            the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the incrementNum
		 */
		public int getIncrementNum() {
			return incrementNum;
		}

		/**
		 * @param incrementNum
		 *            the incrementNum to set
		 */
		public void setIncrementNum(int incrementNum) {
			this.incrementNum = incrementNum;
		}

		/**
		 * 初始化一个序列器
		 *
		 * @param name
		 *            序列名称
		 * @param incrementNum
		 *            增长数
		 */
		public RedisSequence(String name, int incrementNum) {
			super();
			this.name = name;
			this.incrementNum = incrementNum;
			counter = new RedisAtomicLong(REDIS_TAG + name, redisConnectionFactory);
			currentId.set(counter.getAndAdd(incrementNum));
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
