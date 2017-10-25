package uw.task;

import java.util.ArrayList;

import uw.task.listener.CronerTaskListener;
import uw.task.listener.RunnerTaskListener;

/**
 * 任务监听管理器。
 *
 * @author axeon
 */
public class TaskListenerManager {

	/**
	 * runner监听器列表。
	 */
    private ArrayList<RunnerTaskListener> runnerListenerList = new ArrayList<>();

    /**
     * croner监听器列表。
     */
    private ArrayList<CronerTaskListener> cronerListenerList = new ArrayList<>();

    /**
     * 加入一个RunnerListener。
     *
     * @param listener
     */
    public void addRunnerListener(RunnerTaskListener listener) {
        runnerListenerList.add(listener);
    }

    /**
     * 加入一个RunnerListener.
     *
     * @param listener
     */
    public void addCronerListener(CronerTaskListener listener) {
        cronerListenerList.add(listener);
    }

    /**
     * 清除RunnerListener列表
     */
    public void clearRunnerListener() {
        runnerListenerList.clear();
    }

    /**
     * 清除RunnerListener列表
     */
    public void clearCronerListener() {
        cronerListenerList.clear();
    }

    /**
     * 获得Runner Listener列表。
     *
     * @return
     */
    public ArrayList<RunnerTaskListener> getRunnerListenerList() {
        return runnerListenerList;
    }

    /**
     * 获得Croner Listener列表。
     *
     * @return
     */
    public ArrayList<CronerTaskListener> getCronerListenerList() {
        return cronerListenerList;
    }

}
