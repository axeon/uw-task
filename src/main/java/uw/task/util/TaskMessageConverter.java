package uw.task.util;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractJsonMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
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
	private static ConcurrentHashMap<String, JavaType> dataTypeMap = new ConcurrentHashMap<>();

	/**
	 * json的mapper
	 */
	private static ObjectMapper jsonObjectMapper;

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
	 * 构建任务数据类型，并缓存
	 *
	 * @param taskClass
	 * @param taskRunner
	 */
	public static void constructTaskDataType(String taskClass, TaskRunner<?, ?> taskRunner) {
		Type interfaceType = taskRunner.getClass().getGenericSuperclass();
		// 有时候会有基类继承。。。解决这个问题。。。
		if (!(interfaceType instanceof ParameterizedType)) {
			interfaceType = taskRunner.getClass().getSuperclass().getGenericSuperclass();
		}
		if (interfaceType instanceof ParameterizedType) {
			Type[] runnerTypes = ((ParameterizedType) interfaceType).getActualTypeArguments();
			JavaType[] paramTypes = new JavaType[runnerTypes.length];
			for (int i = 0; i < runnerTypes.length; i++) {
				if (runnerTypes[i] instanceof ParameterizedType) {
					ParameterizedType pt = ((ParameterizedType) runnerTypes[i]);
					Type[] ts = pt.getActualTypeArguments();
					Class<?>[] cs = new Class[ts.length];
					for (int x = 0; x < ts.length; x++) {
						cs[x] = (Class<?>) ts[x];
					}
					paramTypes[i] = jsonObjectMapper.getTypeFactory()
							.constructParametricType((Class<?>) pt.getRawType(), cs);
				} else {
					paramTypes[i] = jsonObjectMapper.constructType(runnerTypes[i]);
				}
			}
			JavaType taskDataType = jsonObjectMapper.getTypeFactory().constructParametricType(TaskData.class,
					paramTypes);
			dataTypeMap.put(taskClass, taskDataType);
		}
	}

	@Override
	public Object fromMessage(Message message) throws MessageConversionException {
		Object content = null;
		MessageProperties properties = message.getMessageProperties();
		if (properties != null) {
			String contentType = properties.getContentType();
			if (contentType != null && contentType.equals(CONTENT_TYPE_TASK_DATA)) {
				String taskClass = (String) properties.getHeaders().get(CONTENT_TYPE_TASK_CLASS);
				JavaType type = null;
				if (taskClass != null) {
					type = getJavaTypeByTaskClass(taskClass);
				}
				if (type == null) {
					type = jsonObjectMapper.constructType(TaskData.class);
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
	protected Message createMessage(Object objectToConvert, MessageProperties messageProperties)
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
