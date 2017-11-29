package uw.task;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import uw.task.conf.TaskMetaInfoManager;
import uw.task.conf.TaskProperties;
import uw.task.container.TaskRunnerContainer;
import uw.task.util.GlobalSequenceManager;
import uw.task.util.TaskMessageConverter;

/**
 * 任务执行器。 通过调用此类，可以实现队列执行，RPC调用，本地调用等功能。
 *
 * @author axeon
 */
public class TaskScheduler {

	private static final Logger log = LoggerFactory.getLogger(TaskScheduler.class);

	/**
	 * Task配置文件
	 */
	private TaskProperties taskProperties;

	/**
	 * rabbitTemplate模板.
	 */
	private AmqpTemplate rabbitTemplate;

	/**
	 * 全局sequence序列，主要用于taskLog日志。
	 */
	private GlobalSequenceManager globalSequenceManager;

	/**
	 * 用于本地执行任务的taskConsumer。
	 */
	private TaskRunnerContainer taskRunnerContainer;


	/**
	 * rpc异步调用线程池
	 */
	private ExecutorService taskRpcService = null;

	public TaskScheduler(TaskProperties taskProperties, AmqpTemplate rabbitTemplate,
			TaskRunnerContainer taskRunnerContainer, GlobalSequenceManager globalSequenceManager) {
		this.taskProperties = taskProperties;
		this.rabbitTemplate = rabbitTemplate;
		this.taskRunnerContainer = taskRunnerContainer;
		this.globalSequenceManager = globalSequenceManager;
		taskRpcService = new ThreadPoolExecutor(taskProperties.getTaskRpcMinThreadNum(), taskProperties.getTaskRpcMaxThreadNum(), 20L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(),
				new ThreadFactoryBuilder().setDaemon(true).setNameFormat("TaskRpc-%d").build());
	}

	/**
	 * 把任务发送到队列中
	 *
	 * @param taskdata
	 *            任务数据
	 */
	public void sendToQueue(final TaskData<?, ?> taskData) {
		taskData.setId(globalSequenceManager.nextId("task_runner_log"));
		taskData.setQueueDate(new Date());
		taskData.setRunType(TaskData.RUN_TYPE_GLOBAL);
		String queue = TaskMetaInfoManager.getFitQueue(taskData);
		rabbitTemplate.convertAndSend(queue, queue, taskData);
	}


	/**
	 * 同步执行任务，可能会导致阻塞。
	 *
	 * @param runTarget
	 *            目标主机配置名，如果没有，则为空
	 * @param taskData
	 *            任务数据
	 * @return
	 */
	public <TP, RD> TaskData<TP, RD> runTask(final TaskData<TP, RD> taskData,
			final TypeReference<TaskData<TP, RD>> typeRef) {
		taskData.setId(globalSequenceManager.nextId("task_runner_log"));
		taskData.setQueueDate(new Date());

		// 当自动RPC，并且本地有runner，而且target匹配的时候，运行在本地模式下。
		if (taskData.getRunType() == TaskData.RUN_TYPE_AUTO_RPC && TaskMetaInfoManager.checkRunnerRunLocal(taskData)) {
			// 启动本地运行模式。
			taskData.setRunType(TaskData.RUN_TYPE_LOCAL);
		} else {
			taskData.setRunType(TaskData.RUN_TYPE_GLOBAL_RPC);
		}

		if (taskData.getRunType() == TaskData.RUN_TYPE_LOCAL) {
			taskRunnerContainer.process(taskData);
			return taskData;
		} else {
			// 全局运行模式
			String queue = TaskMetaInfoManager.getFitQueue(taskData);
			@SuppressWarnings("unchecked")
			TaskData<TP, RD> retdata = (TaskData<TP, RD>) rabbitTemplate.convertSendAndReceive(queue, queue, taskData,
					new MessagePostProcessor() {

						@Override
						public Message postProcessMessage(Message message) throws AmqpException {
							MessageProperties mp = message.getMessageProperties();
							mp.setPriority(10);
							mp.setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
							mp.setExpiration("180000");
							return message;
						}
					});
			if (typeRef != null) {
				try {
					retdata = TaskMessageConverter.getTaskObjectMapper().convertValue(retdata, typeRef);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			return retdata;
		}
	}
	
	/**
	 * 远程运行任务，并返回future<TaskData<?,?>>。 如果需要获得数据，可以使用futrue.get()来获得。
	 * 此方法要谨慎使用，因为task存在限速，大并发下可能会导致线程数超。
	 * @param runTarget
	 *            目标主机配置名，如果没有，则为空
	 * @param taskData
	 *            任务数据
	 * @return
	 */
	public <TP, RD> Future<TaskData<TP, RD>> runTaskAsync(final TaskData<TP, RD> taskData,
			final TypeReference<TaskData<TP, RD>> typeRef) {
		taskData.setId(globalSequenceManager.nextId("task_runner_log"));
		taskData.setQueueDate(new Date());
		
		// 当自动RPC，并且本地有runner，而且target匹配的时候，运行在本地模式下。
		if (taskData.getRunType() == TaskData.RUN_TYPE_AUTO_RPC && TaskMetaInfoManager.checkRunnerRunLocal(taskData)) {
			// 启动本地运行模式。
			taskData.setRunType(TaskData.RUN_TYPE_LOCAL);
		} else {
			taskData.setRunType(TaskData.RUN_TYPE_GLOBAL_RPC);
		}
		if (taskData.getRunType() == TaskData.RUN_TYPE_LOCAL) {
			// 启动本地运行模式。
			Future<TaskData<TP, RD>> future = taskRpcService.submit(() -> {
				taskRunnerContainer.process(taskData);
				return taskData;
			});
			return future;
		} else {
			// 全局运行模式
			Future<TaskData<TP, RD>> future = taskRpcService.submit(() -> {
				String queue = TaskMetaInfoManager.getFitQueue(taskData);
				@SuppressWarnings("unchecked")
				TaskData<TP, RD> retdata = (TaskData<TP, RD>) rabbitTemplate.convertSendAndReceive(queue, queue,
						taskData, new MessagePostProcessor() {

							@Override
							public Message postProcessMessage(Message message) throws AmqpException {
								MessageProperties mp = message.getMessageProperties();
								mp.setPriority(10);
								mp.setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
								mp.setExpiration("180000");
								return message;
							}
						});
				if (typeRef != null) {
					try {
						retdata = TaskMessageConverter.getTaskObjectMapper().convertValue(retdata, typeRef);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
				return retdata;
			});
			return future;
		}
	}


}