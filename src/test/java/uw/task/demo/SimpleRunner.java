package uw.task.demo;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import uw.task.TaskData;
import uw.task.entity.TaskContact;
import uw.task.entity.TaskRunnerConfig;
import uw.task.util.TaskMessageConverter;

/**
 * @author liliang
 * @since 2018-05-30
 */
public class SimpleRunner extends SimpleBaseRunner {
    @Override
    public String runTask(TaskData<String, String> taskData) throws Exception {
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

    public static void main(String[] args) {
        TaskMessageConverter converter = new TaskMessageConverter();

        SimpleRunner runner = new SimpleRunner();
        TaskData<String, String> taskData = new TaskData<>();
        taskData.setTaskParam("我是简单的Java String");
        taskData.setResultData("我是简单的Java String");
        taskData.setTaskClass("uw.task.demo.SimpleRunner");

        TaskMessageConverter.constructTaskDataType("uw.task.demo.SimpleRunner",runner);

        MessageProperties properties = new MessageProperties();
        Message message = converter.toMessage(taskData,properties);

        TaskData<String, String> fromData = (TaskData<String, String>)converter.fromMessage(message);
        String param = fromData.getTaskParam();
        String data = fromData.getResultData();
    }
}
