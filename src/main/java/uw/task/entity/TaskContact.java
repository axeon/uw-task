package uw.task.entity;

import java.io.Serializable;

/**
 * taskAlertContact实体类。
 *
 * @author axeon
 * @version $Revision: 1.00 $ $Date: 2017-05-06 13:38:17
 */
public class TaskContact implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 执行类信息
     */
    private String taskClass;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String mobile;

    /**
     * 联系email
     */
    private String email;

    /**
     * 联系微信
     */
    private String weixin;

    /**
     * 联系qq
     */
    private String qq;

    /**
     * 联系人备注
     */
    private String remark;

    public TaskContact() {
        super();
    }

    /**
     * 默认构造器
     *
     * @param contactName
     * @param mobile
     * @param email
     * @param weixin
     * @param qq
     */
    public TaskContact(String contactName, String mobile, String email, String weixin, String qq, String remark) {
        super();
        this.contactName = contactName;
        this.mobile = mobile;
        this.email = email;
        this.weixin = weixin;
        this.qq = qq;
        this.remark = remark;
    }

    /**
     * @return the taskClass
     */
    public String getTaskClass() {
        return taskClass;
    }

    /**
     * @param taskClass the taskClass to set
     */
    public void setTaskClass(String taskClass) {
        this.taskClass = taskClass;
    }

    /**
     * @return the contactName
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * @param contactName the contactName to set
     */
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    /**
     * @return the mobile
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * @param mobile the mobile to set
     */
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the weixin
     */
    public String getWeixin() {
        return weixin;
    }

    /**
     * @param weixin the weixin to set
     */
    public void setWeixin(String weixin) {
        this.weixin = weixin;
    }

    /**
     * @return the qq
     */
    public String getQq() {
        return qq;
    }

    /**
     * @param qq the qq to set
     */
    public void setQq(String qq) {
        this.qq = qq;
    }

    /**
     * @return the remark
     */
    public String getRemark() {
        return remark;
    }

    /**
     * @param remark the remark to set
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

}
