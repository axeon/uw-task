package uw.task.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.RateLimiter;

/**
 * 基于guava RateLimiter实现的限速器。
 *
 * @author axeon
 */
public class LocalRateLimiter {

	private ConcurrentHashMap<String, RateLimiter> map = new ConcurrentHashMap<>();

	/**
	 * 尝试获得限制允许状态。
	 *
	 * @param name
	 * @return
	 */
	public boolean tryAcquire(String name, long timeout, TimeUnit unit) {
		RateLimiter limiter = map.get(name);
		if (limiter != null) {
			return limiter.tryAcquire(1, timeout, unit);
		} else {
			return true;
		}
	}

	/**
	 * 初始化一个流量限速器。
	 *
	 * @param name
	 *            限速器名称，应是全局唯一的名称
	 * @param limitRate
	 *            限速速率
	 * @param limitTimeSecond
	 *            限速时间数值
	 * @return 如果已经存在，则返回false，不再创建。
	 */
	public boolean initLimiter(String name, long limitRate, long limitTimeSecond) {
		RateLimiter limiter = map.get(name);
		double rate = (double) limitRate / (double) limitTimeSecond;
		if (rate == 0.0d) {
			rate = 1.0d;
		}
		if (limiter == null) {
			synchronized (map) {
				limiter = RateLimiter.create(rate);
				map.putIfAbsent(name, limiter);
			}
			return true;
		} else {
			if (limiter.getRate() != rate) {
				limiter.setRate(rate);
				return true;
			} else {
				return false;
			}
		}
	}

}
