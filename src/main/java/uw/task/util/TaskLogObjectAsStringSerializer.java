package uw.task.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.httpclient.http.ObjectMapper;
import uw.task.conf.TaskMetaInfoManager;
import uw.task.entity.TaskCronerConfig;
import uw.task.entity.TaskCronerLog;
import uw.task.entity.TaskRunnerConfig;
import uw.task.entity.TaskRunnerLog;

import java.io.IOException;

/**
 * 任务日志序列化器
 * XXX: 注意此序列化器只用于TaskCronerLog、TaskRunnerLog对象
 *
 * @author liliang
 * @since 2018-05-31
 */
public class TaskLogObjectAsStringSerializer<T> extends JsonSerializer<T> {

    /**
     * 什么都不记录
     */
    public static final int TASK_LOG_TYPE_NONE = -1;

    /**
     * 记录日志
     */
    public static final int TASK_LOG_TYPE_RECORD = 0;

    /**
     * 记录日志,含请求参数
     */
    public static final int TASK_LOG_TYPE_RECORD_TASK_PARAM = 1;

    /**
     * 记录日志,含返回参数
     */
    public static final int TASK_LOG_TYPE_RECORD_RESULT_DATA = 2;

    /**
     * TaskRunner请求参数
     */
    private static final String TASK_RUNNER_FIELD_TASK_PARAM = "taskParam";

    /**
     * TaskRunner返回参数
     */
    private static final String TASK_RUNNER_FIELD_RESULT_DATA = "resultData";

    /**
     * TaskCroner请求参数
     */
    private static final String TASK_CRONER_FIELD_TASK_PARAM = "taskParam";

    /**
     * TaskCroner返回参数
     */
    private static final String TASK_CRONER_FIELD_RESULT_DATA = "resultData";

    /**
     * 记录全部日志
     */
    public static final int TASK_LOG_TYPE_RECORD_ALL = 3;

    private static final Logger logger = LoggerFactory.getLogger(TaskLogObjectAsStringSerializer.class);

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Object taskLogObject = gen.getCurrentValue();
        String fieldName = gen.getOutputContext().getCurrentName();
        int logType = TASK_LOG_TYPE_RECORD_ALL;
        long logLimitSize = 0;
        boolean needLog = true;
        if (taskLogObject instanceof TaskRunnerLog) {
            TaskRunnerLog runnerLog = (TaskRunnerLog) taskLogObject;
            String taskClass = runnerLog.getTaskClass();
            if (StringUtils.isNotBlank(taskClass)) {
                TaskRunnerConfig taskRunnerConfig = TaskMetaInfoManager.getTaskRunnerConfig(taskClass);
                if (taskRunnerConfig != null) {
                    logType = taskRunnerConfig.getLogType();
                    logLimitSize = taskRunnerConfig.getLogLimitSize();
                    needLog = logType == TASK_LOG_TYPE_RECORD_ALL ||
                            (fieldName.equals(TASK_RUNNER_FIELD_TASK_PARAM) && logType > TASK_LOG_TYPE_RECORD_TASK_PARAM) ||
                            (fieldName.equals(TASK_RUNNER_FIELD_RESULT_DATA) && logType > TASK_LOG_TYPE_RECORD_RESULT_DATA);
                }
            }
        } else if (taskLogObject instanceof TaskCronerLog) {
            TaskCronerLog cronerLog = (TaskCronerLog) taskLogObject;
            String taskClass = cronerLog.getTaskClass();
            if (StringUtils.isNotBlank(taskClass)) {
                TaskCronerConfig taskCronerConfig = TaskMetaInfoManager.getTaskCronerConfig(taskClass);
                if (taskCronerConfig != null) {
                    logType = taskCronerConfig.getLogType();
                    logLimitSize = taskCronerConfig.getLogLimitSize();
                    needLog = logType == TASK_LOG_TYPE_RECORD_ALL ||
                            (fieldName.equals(TASK_CRONER_FIELD_TASK_PARAM) && logType > TASK_LOG_TYPE_RECORD_TASK_PARAM) ||
                            (fieldName.equals(TASK_CRONER_FIELD_RESULT_DATA) && logType > TASK_LOG_TYPE_RECORD_RESULT_DATA);
                }
            }
        }
        if (needLog) {
            try {
                // 是否需要截断
                if (logLimitSize > 0) {
                    okio.Buffer buffer = new okio.Buffer();
                    ObjectMapper.DEFAULT_JSON_MAPPER.write(buffer.outputStream(),value);
                    gen.writeString(buffer.readByteString().utf8());
                } else {
                    gen.writeString(ObjectMapper.DEFAULT_JSON_MAPPER.toString(value));
                }
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
        } else {
            gen.writeString("");
        }
    }
}
