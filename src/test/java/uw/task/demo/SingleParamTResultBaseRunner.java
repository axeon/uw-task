package uw.task.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import uw.task.TaskData;
import uw.task.TaskRunner;
import uw.task.entity.TaskContact;
import uw.task.entity.TaskRunnerConfig;

/**
 * @author liliang
 * @since 2018-05-30
 */
public abstract class SingleParamTResultBaseRunner extends TaskRunner<String,MyReturnData> {
    @Override
    public MyReturnData runTask(TaskData<String, MyReturnData> taskData) throws Exception {
        return null;
    }

    @Override
    public TaskRunnerConfig initConfig() {
        return null;
    }

    @Override
    public TaskContact initContact() {
        return null;
    }
}
