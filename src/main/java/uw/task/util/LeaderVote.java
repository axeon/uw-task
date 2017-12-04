package uw.task.util;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import uw.task.conf.TaskProperties;

/**
 * 使用Reids的setnx+expire来选举Leader，来在多个运行实例中选举出一个运行全局任务的实例。
 *
 * @author axeon
 */
public class LeaderVote {

	private static final Logger log = LoggerFactory.getLogger(LeaderVote.class);

	/**
	 * REDIS前缀
	 */
	private static final String REDIS_TAG = "_LEADER_VOTE_";

	/**
	 * redis模板
	 */
	private final StringRedisTemplate stringRedisTemplate;

	/**
	 * task配置
	 */
	private final TaskProperties taskProperties;

	public LeaderVote(final RedisConnectionFactory redisConnectionFactory, TaskProperties taskProperties) {
		this.stringRedisTemplate = new StringRedisTemplate(redisConnectionFactory);
        this.stringRedisTemplate.afterPropertiesSet();
		this.taskProperties = taskProperties;
	}

	/**
	 * hashmap表。
	 */
	private ConcurrentHashMap<String, VoteInfo> map = new ConcurrentHashMap<>();

	/**
	 * 返回当前是否是Leader.
	 *
	 * @param name
	 * @return the isLeader
	 */
	public boolean isLeader(String name) {
		VoteInfo vi = getVoteInfo(name);
		if (vi != null) {
			return vi.isLeader();
		} else {
			return false;
		}
	}

	/**
	 * 初始化一个voteInfo信息。
	 *
	 * @param name
	 * @return
	 */
	private synchronized VoteInfo getVoteInfo(String name) {
		VoteInfo vi = map.get(name);
		if (vi == null) {
			synchronized (map) {
				vi = new VoteInfo(name);
				map.putIfAbsent(name, vi);
			}
		}
		return vi;
	}

	private long runTimes = 0;

	/**
	 * 20秒检查一次leader状态。
	 */
	public void batchCheckLeaderStatus() {
		// 启动的前90秒，3秒检测一次状态，后面每隔18秒检测一次状态。
		runTimes++;
		if (runTimes > 30) {
			if (runTimes % 6 != 0) {
				return;
			}
		}

		try {
			for (Entry<String, VoteInfo> kv : map.entrySet()) {
				kv.getValue().checkLeader();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 投票信息
	 *
	 * @author axeon
	 */
	private class VoteInfo {

		/**
		 * 关联对象。
		 */
		private BoundValueOperations<String, String> bvo;

		/**
		 * 是否是Leader
		 */
		private boolean isLeader = false;

		/**
		 * 默认构造器。
		 * 
		 * @param bvo
		 */
		public VoteInfo(String name) {
			this.bvo = stringRedisTemplate.boundValueOps(REDIS_TAG + name);
			checkLeader();
		}

		/**
		 * 返回是否是Leader
		 * 
		 * @return
		 */
		public boolean isLeader() {
			return isLeader;
		}

		/**
		 * 检查是否是Leader.
		 */
		public boolean checkLeader() {
			// 使用setnx来抢leader身份
			bvo.setIfAbsent(taskProperties.getHostId());
			// 再次确认身份，并更新expire。
			if (taskProperties.getHostId().equals(bvo.get())) {
				bvo.expire(60, TimeUnit.SECONDS);
				isLeader = true;
			} else {
				isLeader = false;
			}
			return isLeader;
		}
	}

}
