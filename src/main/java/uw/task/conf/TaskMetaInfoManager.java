package uw.task.conf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import uw.task.TaskCroner;
import uw.task.TaskData;
import uw.task.TaskRunner;
import uw.task.entity.TaskCronerConfig;
import uw.task.entity.TaskRunnerConfig;

public class TaskMetaInfoManager {
	
	/**
	 * 运行主机配置
	 */
	static List<String> targetConfig = null;
	
	/**
	 * Runner任务实例缓存。
	 */
	@SuppressWarnings("rawtypes")
	static Map<String, TaskRunner> runnerMap = new HashMap<>();
	
	/**
	 * Cron任务实例缓存。
	 */
	static Map<String, TaskCroner> cronerMap = new HashMap<>();

	/**
	 * Runner任务配置缓存
	 */
	static ConcurrentHashMap<String, TaskRunnerConfig> runnerConfigMap = new ConcurrentHashMap<>();

	/**
	 * Cron任务配置缓存。
	 */
	static ConcurrentHashMap<String, TaskCronerConfig> cronerConfigMap = new ConcurrentHashMap<>();

	/**
	 * 系统队列表。
	 */
	static ConcurrentHashMap<String, String> sysQueueSet = new ConcurrentHashMap<>();


	/**
	 * 获得任务运行实例。
	 *
	 * @param taskClass
	 * @return
	 */
	public static TaskRunner<?, ?> getRunner(String taskClass) {
		return runnerMap.get(taskClass);
	}
	
	
	/**
	 * 检查一个runner是否可以在本地运行。
	 * 
	 * @param taskData
	 * @return
	 */
	public static boolean checkRunnerRunLocal(TaskData<?, ?> taskData) {
		boolean exists = runnerMap.containsKey(taskData.getTaskClass());
		boolean matchTarget = false;
		if (targetConfig != null) {
			if (targetConfig.contains(taskData.getRunTarget())) {
				matchTarget = true;
			}
		}
		return exists && matchTarget;
	}
	

	/**
	 * 根据服务器端Queue列表，返回合适的key。
	 * 
	 * @return
	 */
	public static String getFitQueue(TaskData<?, ?> data) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(data.getTaskClass()).append("#");
		if (data.getTaskTag() != null && data.getTaskTag().length() > 0) {
			sb.append(data.getTaskTag());
		}
		sb.append("$");
		if (data.getRunTarget() != null && data.getRunTarget().length() > 0) {
			sb.append(data.getRunTarget());
		}
		String all = sb.toString();
		if (sysQueueSet.containsKey(all)) {
			return all;
		}
		// 检测去除目标的情况
		if (!all.endsWith("$")) {
			String test = all.substring(0, all.lastIndexOf('$') + 1);
			if (sysQueueSet.containsKey(test)) {
				return test;
			}
		}
		// 检测去除TAG的情况
		if (!all.contains("#$")) {
			String test = all.substring(0, all.indexOf('#') + 1) + all.substring(all.lastIndexOf('$'), all.length());
			if (sysQueueSet.containsKey(test)) {
				return test;
			}
		}
		// 两个都去除的情况
		if (!all.endsWith("#$")) {
			String test = data.getTaskClass() + "#$";
			if (sysQueueSet.containsKey(test)) {
				return test;
			}
		}
		// 最后都没匹配到，返回原始数据
		return all;
	}



	/**
	 * 获得任务配置
	 *
	 * @param data
	 * @return
	 */
	public static TaskRunnerConfig getRunnerConfig(TaskData<?, ?> data) {
		TaskRunnerConfig config = null;
		StringBuilder sb = new StringBuilder(100);
		sb.append(data.getTaskClass()).append("#");
		if (data.getTaskTag() != null && data.getTaskTag().length() > 0) {
			sb.append(data.getTaskTag());
		}
		sb.append("$");
		if (data.getRunTarget() != null && data.getRunTarget().length() > 0) {
			sb.append(data.getRunTarget());
		}
		String all = sb.toString();
		config = runnerConfigMap.get(all);
		if (config != null) {
			return config;
		}
		// 检测去除目标的情况
		if (!all.endsWith("$")) {
			String test = all.substring(0, all.lastIndexOf('$') + 1);
			config = runnerConfigMap.get(test);
			if (config != null) {
				return config;
			}
		}
		// 检测去除TAG的情况
		if (!all.contains("#$")) {
			String test = all.substring(0, all.indexOf('#') + 1) + all.substring(all.lastIndexOf('$'), all.length());
			config = runnerConfigMap.get(test);
			if (config != null) {
				return config;
			}
		}
		// 两个都去除的情况
		if (!all.endsWith("#$")) {
			String test = data.getTaskClass() + "#$";
			config = runnerConfigMap.get(test);
			if (config != null) {
				return config;
			}
		}
		// 匹配不上了。。。
		return null;
	}
	
	/**
	 * 更新系统队列表。
	 * 
	 * @param config
	 */
	static void updateSysQueue(final TaskRunnerConfig config) {
		String key = getRunnerConfigKey(config);
		if (config.getState() < 1) {
			sysQueueSet.remove(key);
		} else {
			sysQueueSet.put(key, "");
		}
	}
	

	/**
	 * 获得croner配置键。 使用taskClass#Id$target来配置
	 * 
	 * @return
	 */
	public static String getCronerConfigKey(TaskCronerConfig config) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(config.getTaskClass()).append("#");
		if (config.getTaskParam() != null && config.getTaskParam().length() > 0) {
			sb.append(config.getId());
		}
		sb.append("$");
		if (config.getRunTarget() != null && config.getRunTarget().length() > 0) {
			sb.append(config.getRunTarget());
		}
		return sb.toString();
	}

	/**
	 * 获得Runner配置结合Host。
	 * 
	 * @return
	 */
	public static String getRunnerConfigKey(TaskRunnerConfig config) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(config.getTaskClass()).append("#");
		if (config.getTaskTag() != null && config.getTaskTag().length() > 0) {
			sb.append(config.getTaskTag());
		}
		sb.append("$");
		if (config.getRunTarget() != null && config.getRunTarget().length() > 0) {
			sb.append(config.getRunTarget());
		}
		return sb.toString();
	}


}
