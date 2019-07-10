package uw.task.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.SmartMessageConverter;
import org.springframework.core.ParameterizedTypeReference;
import uw.task.TaskData;
import uw.task.TaskRunner;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于spring-amqp的消息转换器。
 *
 * @author axeon
 */
public class TaskMessageConverter implements SmartMessageConverter {

    /**
     * 数据类型
     */
    public static final String CONTENT_TYPE_TASK_DATA = "TASK_DATA";
    /**
     * 数据类型
     */
    public static final String CONTENT_TYPE_TASK_CLASS = "TASK_CLASS";
    private static Logger log = LoggerFactory.getLogger(TaskMessageConverter.class);
    /**
     * 泛型类型缓存
     */
    private static ConcurrentHashMap<String, JavaType> dataTypeMap = new ConcurrentHashMap<>();

    /**
     * json的mapper
     */
    private static ObjectMapper jsonObjectMapper;

    static {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.registerModule(new JavaTimeModule());
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

    /**
     * 根据taskClass获得指定的JavaType
     *
     * @param taskClass
     * @return
     */
    public static JavaType getJavaTypeByTaskClass(String taskClass) {
        return dataTypeMap.get(taskClass);
    }

    /**
     * 获得ObjectMapper。
     */
    public static ObjectMapper getTaskObjectMapper() {
        return jsonObjectMapper;
    }

    /**
     * 根据消息，构造TaskData对象。
     *
     * @param message
     * @param valueTypeRef
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T constructTaskData(Message message, TypeReference valueTypeRef) {
        if (message == null || message.getBody() == null || message.getMessageProperties() == null) {
            return null;
        }
        try {
            return jsonObjectMapper.readValue(message.getBody(), valueTypeRef);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 构建任务数据类型，并缓存
     *
     * @param taskClass
     * @param taskRunner
     */
    public static void constructTaskDataType(String taskClass, TaskRunner<?, ?> taskRunner) {
        // 解决多层继承问题
        Class<?> pBizClz = taskRunner.getClass();
        Type interfaceType = pBizClz.getGenericSuperclass();
        while (!(interfaceType instanceof ParameterizedType)) {
            pBizClz = pBizClz.getSuperclass();
            interfaceType = pBizClz.getGenericSuperclass();
        }
        // 拿到泛参数
        Type[] runnerTypes = ((ParameterizedType) interfaceType).getActualTypeArguments();
        // 取JavaType数组
        JavaType[] javaType = new JavaType[runnerTypes.length];
        for (int i = 0; i < runnerTypes.length; i++) {
            javaType[i] = getJavaTypeFromType(runnerTypes[i]);
        }
        JavaType taskDataType = jsonObjectMapper.getTypeFactory().constructParametricType(TaskData.class,
                javaType);
        dataTypeMap.put(taskClass, taskDataType);
    }

    /**
     * 通过Type取JavaType
     *
     * @param type
     * @return
     */
    private static JavaType getJavaTypeFromType(final Type type) {
        Type pType = type;
        if (pType instanceof ParameterizedType) {
            // 根类型
            JavaType rootJavaType = null;
            while (pType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = ((ParameterizedType) pType);
                Type[] ts = parameterizedType.getActualTypeArguments();
                JavaType[] pJavaType = new JavaType[ts.length];
                for (int x = 0; x < ts.length; x++) {
                    pJavaType[x] = getJavaTypeFromType(ts[x]);
                }
                pType = pType.getClass().getGenericSuperclass();
                rootJavaType = jsonObjectMapper.getTypeFactory()
                        .constructParametricType((Class<?>) parameterizedType.getRawType(),
                                pJavaType);
            }
            return rootJavaType;
        }
        return jsonObjectMapper.getTypeFactory().constructType(type);
    }


    @Override
    public Message toMessage(Object objectToConvert, MessageProperties messageProperties) throws MessageConversionException {
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

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        Object content = null;
        if (message == null || message.getBody() == null || message.getMessageProperties() == null) {
            return null;
        }
        MessageProperties properties = message.getMessageProperties();
        String contentType = properties.getContentType();
        if (CONTENT_TYPE_TASK_DATA.equals(contentType)) {
            String taskClass = (String) properties.getHeaders().get(CONTENT_TYPE_TASK_CLASS);
            JavaType type;
            if (taskClass != null) {
                type = getJavaTypeByTaskClass(taskClass);
            } else {
                type = jsonObjectMapper.constructType(TaskData.class);
            }
            try {
                content = jsonObjectMapper.readValue(message.getBody(), type);
            } catch (Exception e) {
                throw new MessageConversionException("Failed to convert Message content", e);
            }
        } else {
            if (log.isWarnEnabled()) {
                try {
                    log.warn("Could not convert incoming message with content-type [{}],message: {} ",
                            contentType, new String(message.getBody(), "UTF-8"));
                } catch (Exception e) {
                    log.warn("Could not convert incoming message with content-type [{}],message cannot be decode. ",
                            contentType);
                }
            }
        }

        return content;
    }


    /**
     * A variant of {@link #fromMessage(Message)} which takes an extra
     * conversion context as an argument.
     *
     * @param message        the input message.
     * @param conversionHint an extra object passed to the {
     * @return the result of the conversion, or {@code null} if the converter cannot
     * perform the conversion.
     * @throws MessageConversionException if the conversion fails.
     * @see #fromMessage(Message)
     */
    @Override
    public Object fromMessage(Message message, Object conversionHint) throws MessageConversionException {
        Object content = null;
        if (message == null || message.getBody() == null || message.getMessageProperties() == null) {
            return null;
        }
        MessageProperties properties = message.getMessageProperties();
        String contentType = properties.getContentType();
        if (CONTENT_TYPE_TASK_DATA.equals(contentType)) {
            try {
                content = jsonObjectMapper.readValue(message.getBody(), jsonObjectMapper.getTypeFactory().constructType(
                        ((ParameterizedTypeReference<?>) conversionHint).getType()));
            } catch (Exception e) {
                throw new MessageConversionException("Failed to convert Message content", e);
            }
        } else {
            if (log.isWarnEnabled()) {
                try {
                    log.warn("Could not convert incoming message with content-type [{}],message: {} ",
                            contentType, new String(message.getBody(), "UTF-8"));
                } catch (Exception e) {
                    log.warn("Could not convert incoming message with content-type [{}],message cannot be decode. ",
                            contentType);
                }
            }
        }
        return content;
    }

}
