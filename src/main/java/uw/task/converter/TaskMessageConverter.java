package uw.task.converter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import uw.task.TaskData;

import java.util.Date;

/**
 * 用于spring-amqp的消息转换器。
 *
 * @author axeon
 */
public class TaskMessageConverter implements MessageConverter {

    private static Logger log = LoggerFactory.getLogger(TaskMessageConverter.class);

    /**
     * 数据类型
     */
    public static final String CONTENT_TYPE_TASK_DATA = "UT_DATA";

    /**
     * kyro缓存。
     */
    private static final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            kryo.register(TaskData.class);
            kryo.register(Date.class);
            return kryo;
        }
    };

    /**
     * output缓存对象。限定缓存8k，最大8M。
     * 线程内使用，因为uw-task执行的特殊机制，可以减少内存复制。
     */
    private static final ThreadLocal<Output> outputLocal = new ThreadLocal<Output>() {
        @Override
        protected Output initialValue() {
            return new Output(8 * 1024, 8 * 1024 * 1024);
        }
    };

    /**
     * Construct with an internal {@link ObjectMapper} instance. The
     * {@link DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES} is set to false
     * on the {@link ObjectMapper}.
     */
    public TaskMessageConverter() {

    }


    @Override
    public Message toMessage(Object objectToConvert, MessageProperties messageProperties) throws MessageConversionException {
        byte[] bytes = new byte[0];
        if (objectToConvert instanceof TaskData) {
            messageProperties.setContentType(CONTENT_TYPE_TASK_DATA);
            try {
                //序列化操作
                Kryo kryo = kryoLocal.get();
                Output output = outputLocal.get();
                kryo.writeClassAndObject(output, objectToConvert);
                output.flush();
                //此时复制出数据
                bytes = output.toBytes();
                //重置output
                output.reset();
            } catch (Exception e) {
                throw new MessageConversionException("Failed to convert Message content. " + e.getMessage(), e);
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
            try {
                Kryo kryo = kryoLocal.get();
                Input input = new Input(message.getBody());
                content = kryo.readClassAndObject(input);
                //此处反序列化
            } catch (Exception e) {
                throw new MessageConversionException("Failed to convert Message content. " + e.getMessage(), e);
            }
        } else {
            if (log.isWarnEnabled()) {
                try {
                    log.warn("Could not convert incoming message with content-type [{}],message: {} ",
                            contentType, new String(message.getBody(), "UTF-8"));
                } catch (Exception e) {
                    log.warn("Could not convert incoming message with content-type [{}],message cannot be decode. " + e.getMessage(),
                            contentType);
                }
            }
        }
        return content;
    }


}
