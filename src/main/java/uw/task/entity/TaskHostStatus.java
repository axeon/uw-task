package uw.task.entity;

import java.io.Serializable;

/**
 * TaskHostStatus 实体类
 * Created by Acris on 2017/6/8.
 */
public class TaskHostStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主机ID
     */
    private String hostId;
    
    /**
     * 任务项目
     */
    private String taskProject;

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getTaskProject() {
        return taskProject;
    }

    public void setTaskProject(String taskProject) {
        this.taskProject = taskProject;
    }
}
