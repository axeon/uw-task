package uw.task.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Json对象序列化成字符串而非Json对象
 * @author liliang
 * @since 2018-05-18
 */
public class ObjectAsStringSerializer<T> extends JsonSerializer<T> {

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        okio.Buffer buffer = new okio.Buffer();
        TaskMessageConverter.getTaskObjectMapper().writeValue(buffer.outputStream(),value);
        gen.writeString(new InputStreamReader(buffer.inputStream()),(int)buffer.size());
    }
}
