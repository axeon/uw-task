package uw.task.demo;

import org.assertj.core.util.Lists;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import uw.task.TaskData;
import uw.task.util.TaskMessageConverter;

import java.util.List;

/**
 * @author liliang
 * @since 2018-05-30
 */
public class SimpleListRunner extends SimpleBaseListRunner {

    public static void main(String[] args) {
        TaskMessageConverter converter = new TaskMessageConverter();

        SimpleListRunner runner = new SimpleListRunner();
        TaskData<List<String>,List<String>> taskData = new TaskData<>();
        taskData.setTaskParam(Lists.newArrayList());
        taskData.setResultData(Lists.newArrayList());
        taskData.setTaskClass("uw.task.demo.SimpleListRunner");

        TaskMessageConverter.constructTaskDataType("uw.task.demo.SimpleListRunner",runner);

        MessageProperties properties = new MessageProperties();
        Message message = converter.toMessage(taskData,properties);

        TaskData<List<String>, List<String>> fromData = (TaskData<List<String>, List<String>>)converter.fromMessage(message);
        List<String> param = fromData.getTaskParam();
        List<String> data = fromData.getResultData();
    }
}
