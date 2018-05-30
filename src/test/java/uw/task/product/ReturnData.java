package uw.task.product;

/**
 * @author liliang
 * @since 2018-05-30
 */
public final class ReturnData {

    public static final String SUCCESS = "success";

    public static final String ERROR = "error";

    public static final String UNKOWN = "unknow";

    /**
     * 状态
     */
    private String state = UNKOWN;

    /**
     * 信息。
     */
    private String msg;

    /**
     * 代码，可能代表错误代码。
     */
    private String code;

    public ReturnData() {
    }

    protected ReturnData(String state, String msg) {
        this.state = state;
        this.msg = msg;
    }

    /**
     * 附带消息的成功返回值。
     *
     * @param msg
     * @return
     */
    public static ReturnData success(String msg) {
        return new ReturnData(SUCCESS, msg);
    }

    /**
     * 成功返回值。
     *
     * @return
     */
    public static ReturnData success() {
        return new ReturnData(SUCCESS, null);
    }

    /**
     * 附带消息的错误返回值。
     *
     * @param msg
     * @return
     */
    public static ReturnData error(String msg) {
        return new ReturnData(ERROR, msg);
    }

    /**
     * 是否成功。
     *
     * @return
     */
    public boolean isSuccess() {
        return SUCCESS.equals(this.state);
    }

    /**
     * 设置代码。
     *
     * @param code
     * @return
     */
    public ReturnData code(String code) {
        this.code = code;
        return this;
    }

    @Override
    public String toString() {
        return "ReturnData{" +
                "state=" + state +
                ", msg='" + msg + '\'' +
                '}';
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

