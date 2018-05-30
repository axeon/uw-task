package uw.task.util;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractJsonMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import uw.task.TaskData;
import uw.task.TaskRunner;

/**
 * 用于spring-amqp的消息转换器。
 *
 * @author axeon
 *
 */
public class TaskMessageConverter extends AbstractJsonMessageConverter {

    private static Log log = LogFactory.getLog(Jackson2JsonMessageConverter.class);

    /**
     * 数据类型
     */
    public static final String CONTENT_TYPE_TASK_DATA = "TASK_DATA";

    /**
     * 数据类型
     */
    public static final String CONTENT_TYPE_TASK_CLASS = "TASK_CLASS";

    /**
     * 泛型类型缓存
     */
    private static Map<String, TypeReference<?>> dataTypeMap = new ConcurrentHashMap<>();

    /**
     * json的mapper
     */
    private static ObjectMapper jsonObjectMapper;

    /**
     * 默认队列任务数据类型
     */
    private static final TypeReference<?> TASK_DATA_TYPE_REFERENCE = new TypeReference<TaskData<?,?>>(){};

    static {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(Include.NON_DEFAULT);
        jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Construct with an internal {@link ObjectMapper} instance. The
     * {@link DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES} is set to false
     * on the {@link ObjectMapper}.
     */
    public TaskMessageConverter() {
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        super.setBeanClassLoader(classLoader);
    }

    /**
     * 根据taskClass获得指定的JavaType
     *
     * @param taskClass
     * @return
     */
    public static TypeReference<?> getJavaTypeByTaskClass(String taskClass) {
        return dataTypeMap.get(taskClass);
    }

    /**
     * 获得ObjectMapper。
     */
    public static ObjectMapper getTaskObjectMapper() {
        return jsonObjectMapper;
    }

    /**
     * 构建任务数据类型，并缓存
     *
     * @param taskClass
     * @param taskRunner
     */
    public static <TP,TD> void  constructTaskDataType(String taskClass, TaskRunner<TP,TD> taskRunner) {
        dataTypeMap.put(taskClass, taskRunner.initTaskDataType());
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        Object content = null;
        MessageProperties properties = message.getMessageProperties();
        if (properties != null) {
            String contentType = properties.getContentType();
            if (contentType != null && contentType.equals(CONTENT_TYPE_TASK_DATA)) {
                String taskClass = (String) properties.getHeaders().get(CONTENT_TYPE_TASK_CLASS);
                TypeReference<?> type = null;
                if (taskClass != null) {
                    type = getJavaTypeByTaskClass(taskClass);
                } else {
                    type = TASK_DATA_TYPE_REFERENCE;
                }
                try {
                    content = jsonObjectMapper.readValue(message.getBody(), type);
                } catch (Exception e) {
                    throw new MessageConversionException("Failed to convert Message content", e);
                }
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Could not convert incoming message with content-type [" + contentType + "]");
                }
            }
        }
        return content;
    }

    @Override
    public Message createMessage(Object objectToConvert, MessageProperties messageProperties)
            throws MessageConversionException {
        byte[] bytes = new byte[0];
        if (objectToConvert instanceof TaskData) {
            @SuppressWarnings("rawtypes")
            TaskData taskData = (TaskData) objectToConvert;
            messageProperties.getHeaders().put(CONTENT_TYPE_TASK_CLASS, taskData.getTaskClass());
            messageProperties.setContentType(CONTENT_TYPE_TASK_DATA);
            try {
                bytes = jsonObjectMapper.writeValueAsBytes(objectToConvert);
            } catch (IOException e) {
                throw new MessageConversionException("Failed to convert Message content", e);
            }
        }
        messageProperties.setContentLength(bytes.length);
        return new Message(bytes, messageProperties);
    }
}