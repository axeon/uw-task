package uw.task.demo;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import uw.task.TaskData;
import uw.task.util.TaskMessageConverter;

/**
 * @author liliang
 * @since 2018-05-30
 */
public class SingleParamTResultRunner extends SingleParamTResultBaseRunner {
    public static void main(String[] args) {
        TaskMessageConverter converter = new TaskMessageConverter();

        SingleParamTResultRunner runner = new SingleParamTResultRunner();
        TaskData<String, MyReturnData> taskData = new TaskData<>();
        taskData.setTaskParam("我是简单的Java String");
        taskData.setResultData(new MyReturnData());
        taskData.setTaskClass("uw.task.demo.SingleParamTResultRunner");

        TaskMessageConverter.constructTaskDataType("uw.task.demo.SingleParamTResultRunner",runner);

        MessageProperties properties = new MessageProperties();
        Message message = converter.toMessage(taskData,properties);

        TaskData<String, MyReturnData> fromData = (TaskData<String, MyReturnData>)converter.fromMessage(message);
        String param = fromData.getTaskParam();
        MyReturnData data = fromData.getResultData();
    }
}
