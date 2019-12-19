package uw.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uw.task.conf.TaskMetaInfoManager;
import uw.task.conf.TaskProperties;
import uw.task.container.TaskRunnerContainer;
import uw.task.exception.TaskRuntimeException;
import uw.task.util.GlobalSequenceManager;

import java.util.Date;
import java.util.concurrent.*;

/**
 * 任务执行器。 通过调用此类，可以实现队列执行，RPC调用，本地调用等功能。
 *
 * @author axeon
 */
public class TaskFactory {

    private static final Logger log = LoggerFactory.getLogger(TaskFactory.class);

    /**
     * rabbitTemplate模板.
     */
    private RabbitTemplate rabbitTemplate;

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


    public TaskFactory(TaskProperties taskProperties, RabbitTemplate rabbitTemplate,
                       TaskRunnerContainer taskRunnerContainer, GlobalSequenceManager globalSequenceManager) {
        this.rabbitTemplate = rabbitTemplate;
        this.taskRunnerContainer = taskRunnerContainer;
        this.globalSequenceManager = globalSequenceManager;
        taskRpcService = new ThreadPoolExecutor(taskProperties.getTaskRpcMinThreadNum(),
                taskProperties.getTaskRpcMaxThreadNum(), 20L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("TaskRpc-%d").build(), new ThreadPoolExecutor.CallerRunsPolicy());

    }

    /**
     * 把任务发送到队列中
     *
     * @param taskData 任务数据
     */
    public void sendToQueue(final TaskData<?, ?> taskData) {
        Message message = buildTaskQueueMessage(taskData);
        String queue = message.getMessageProperties().getConsumerQueue();
        rabbitTemplate.send(queue, queue, message);
    }

    /**
     * 构造Task消息对象，此方法用于提前构造TaskData。
     *
     * @param taskData
     * @return
     */
    private Message buildTaskQueueMessage(final TaskData taskData) {
        taskData.setId(globalSequenceManager.nextId("task_runner_log"));
        taskData.setQueueDate(new Date());
        taskData.setRunType(TaskData.RUN_TYPE_GLOBAL);
        Message msg = rabbitTemplate.getMessageConverter().toMessage(taskData, new MessageProperties());
        msg.getMessageProperties().setConsumerQueue(TaskMetaInfoManager.getFitQueue(taskData));
        return msg;
    }

    /**
     * 同步执行任务，可能会导致阻塞。
     * 在调用的时候，尤其要注意，taskData对象不可改变！
     *
     * @param taskData 任务数据
     * @return
     */
    @SuppressWarnings("unchecked")
    public <TP, RD> TaskData<TP, RD> runTask(final TaskData<TP, RD> taskData) {
        taskData.setId(globalSequenceManager.nextId("task_runner_log"));
        taskData.setQueueDate(new Date());
        // 当自动RPC，并且本地有runner，而且target匹配的时候，运行在本地模式下。
        if (taskData.getRunType() == TaskData.RUN_TYPE_AUTO_RPC && TaskMetaInfoManager.checkRunnerRunLocal(taskData)) {
            // 启动本地运行模式。
            taskData.setRunType(TaskData.RUN_TYPE_LOCAL);
        }
        if (taskData.getRunType() == TaskData.RUN_TYPE_LOCAL) {
            taskRunnerContainer.process(taskData);
            return taskData;
        } else {
            taskData.setRunType(TaskData.RUN_TYPE_GLOBAL_RPC);
            Message message = rabbitTemplate.getMessageConverter().toMessage(taskData, new MessageProperties());
            //加入优先级信息。
            MessageProperties mp = message.getMessageProperties();
            mp.setPriority(10);
            mp.setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
            mp.setExpiration("180000");
            // 全局运行模式
            String queue = TaskMetaInfoManager.getFitQueue(taskData);
            Message retMessage = rabbitTemplate.sendAndReceive(queue, queue, message);
            return (TaskData<TP, RD>) rabbitTemplate.getMessageConverter().fromMessage(retMessage);
        }
    }

    /**
     * 同步执行任务，可能会导致阻塞。
     * 在调用的时候，尤其要注意，taskData对象不可改变！
     *
     * @param taskData 任务数据
     * @return
     */
    @SuppressWarnings("unchecked")
    public <TP, RD> TaskData<TP, RD> runTaskLocal(final TaskData<TP, RD> taskData) {
        taskData.setId(globalSequenceManager.nextId("task_runner_log"));
        taskData.setQueueDate(new Date());
        // 当自动RPC，并且本地有runner，而且target匹配的时候，运行在本地模式下。
        if (taskData.getRunType() == TaskData.RUN_TYPE_AUTO_RPC && TaskMetaInfoManager.checkRunnerRunLocal(taskData)) {
            // 启动本地运行模式。
            taskData.setRunType(TaskData.RUN_TYPE_LOCAL);
        }
        if (taskData.getRunType() == TaskData.RUN_TYPE_LOCAL) {
            taskRunnerContainer.process(taskData);
            return taskData;
        } else {
            throw new TaskRuntimeException(taskData.getClass().getName() + " is not a local task! ");
        }
    }

    /**
     * 远程运行任务，并返回future<TaskData<?,?>>。 如果需要获得数据，可以使用futrue.get()来获得。
     * 此方法要谨慎使用，因为task存在限速，大并发下可能会导致线程数超。
     * 在调用的时候，尤其要注意，taskData对象不可改变！
     *
     * @param taskData 任务数据
     * @return
     */
    @SuppressWarnings("unchecked")
    public <TP, RD> Future<TaskData<TP, RD>> runTaskAsync(final TaskData<TP, RD> taskData) {
        taskData.setId(globalSequenceManager.nextId("task_runner_log"));
        taskData.setQueueDate(new Date());

        // 当自动RPC，并且本地有runner，而且target匹配的时候，运行在本地模式下。
        if (taskData.getRunType() == TaskData.RUN_TYPE_AUTO_RPC && TaskMetaInfoManager.checkRunnerRunLocal(taskData)) {
            // 启动本地运行模式。
            taskData.setRunType(TaskData.RUN_TYPE_LOCAL);
        }
        if (taskData.getRunType() == TaskData.RUN_TYPE_LOCAL) {
            // 启动本地运行模式。
            return taskRpcService.submit(() -> {
                taskRunnerContainer.process(taskData);
                return taskData;
            });
        } else {
            // 全局运行模式
            taskData.setRunType(TaskData.RUN_TYPE_GLOBAL_RPC);
            Message message = rabbitTemplate.getMessageConverter().toMessage(taskData, new MessageProperties());
            //加入优先级信息。
            MessageProperties mp = message.getMessageProperties();
            mp.setPriority(10);
            mp.setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
            mp.setExpiration("180000");
            String queue = TaskMetaInfoManager.getFitQueue(taskData);
            return taskRpcService.submit(() -> {
                Message retMessage = rabbitTemplate.sendAndReceive(queue, queue, message);
                return (TaskData<TP, RD>) rabbitTemplate.getMessageConverter().fromMessage(retMessage);
            });
        }
    }

    /**
     * 获得队列信息。
     *
     * @param queueName
     * @return 0 是消息数量 1 是消费者数量
     */
    public int[] getQueueInfo(String queueName) {
        AMQP.Queue.DeclareOk declareOk = this.rabbitTemplate.execute(new ChannelCallback<AMQP.Queue.DeclareOk>() {
            public AMQP.Queue.DeclareOk doInRabbit(Channel channel) throws Exception {
                return channel.queueDeclarePassive(queueName);
            }
        });
        return new int[]{declareOk.getMessageCount(), declareOk.getConsumerCount()};
    }

}
