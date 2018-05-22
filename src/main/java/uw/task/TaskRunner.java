package uw.task;

import uw.task.entity.TaskContact;
import uw.task.entity.TaskRunnerConfig;

/**
 * 任务执行器。 所有的任务都通过实现此接口实现.
 * TaskParam和ResultData可通过泛型参数制定具体类型。
 * TP,TD应和TaskData的泛型参数完全一致，否则会导致运行时出错。
 *
 * @param <TP> taskParam参数
 * @param <RD> ResultData返回结果
 * @author axeon
 */
public abstract class TaskRunner<TP, RD> {

    /**
     * 执行任务。 业务层面的异常请使用TaskExcpetion返回。 同时通过setExeInfo来写入执行信息。
     *
     * @param taskData 数据
     * @throws Exception 异常
     * @return 指定的返回对象
     */
    public abstract RD runTask(TaskData<TP, RD> taskData) throws Exception;

    /**
     * 初始化配置信息。
     * @return TaskRunnerConfig配置
     */
    public abstract TaskRunnerConfig initConfig();

    /**
     * 初始化联系人信息。
     *
     * @return TaskContact联系人信息
     */
    public abstract TaskContact initContact();

}
