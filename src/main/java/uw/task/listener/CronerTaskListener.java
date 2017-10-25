package uw.task.listener;

import uw.task.entity.TaskCronerLog;

/**
 * Runner任务的监听器。
 *
 * @author axeon
 */
public interface CronerTaskListener {

    /**
     * 执行前的监听器。
     *
     * @param data
     */
    public void onPreExecute(TaskCronerLog data);

    /**
     * 执行后的监听器。
     *
     * @param data
     */
    public void onPostExecute(TaskCronerLog data);

}
