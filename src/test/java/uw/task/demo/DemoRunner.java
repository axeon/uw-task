package uw.task.demo;

import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import uw.task.TaskData;
import uw.task.entity.TaskContact;
import uw.task.entity.TaskRunnerConfig;
import uw.task.util.TaskMessageConverter;

import java.util.List;
import java.util.Map;

/**
 * @author liliang
 * @since 2018-05-30
 */
public class DemoRunner extends BaseRunner {
    @Override
    public List<Map<String, String>> runTask(TaskData<String, List<Map<String, String>>> taskData) throws Exception {
        return Lists.newArrayList();
    }

    @Override
    public TaskRunnerConfig initConfig() {
        return new TaskRunnerConfig();
    }

    @Override
    public TaskContact initContact() {
        return new TaskContact();
    }

    public static void main(String[] args) {
        TaskMessageConverter converter = new TaskMessageConverter();

        DemoRunner runner = new DemoRunner();
        TaskData<String, List<Map<String, String>>> taskData = new TaskData<String, List<Map<String, String>>>();
        taskData.setTaskClass("uw.task.demo.DemoRunner");

        List<Map<String, String>> data = Lists.newArrayList();
        Map<String,String> map = Maps.newHashMap();
        map.put("order","123123123");
        data.add(map);
        taskData.setResultData(data);

        TaskMessageConverter.constructTaskDataType("uw.task.demo.DemoRunner",runner);

        MessageProperties properties = new MessageProperties();
        Message message = converter.createMessage(taskData,properties);

        TaskData<String, List<Map<String, String>>> fromData = (TaskData<String, List<Map<String, String>>>)converter.fromMessage(message);
        data = fromData.getResultData();
        data.get(0);
    }
}
