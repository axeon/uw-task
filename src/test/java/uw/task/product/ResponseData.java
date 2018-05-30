package uw.task.product;

import static uw.task.product.ReturnData.ERROR;
import static uw.task.product.ReturnData.SUCCESS;
import static uw.task.product.ReturnData.UNKOWN;

/**
 * @author liliang
 * @since 2018-05-30
 */
public final class ResponseData<T> {

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

    /**
     * 返回数据
     */
    private T data;

    private ResponseData(String state, String code, String msg, T data) {
        this.state = state;
        this.msg = msg;
        this.code = code;
        this.data = data;
    }

    public ResponseData() {
        super();
    }

    /**
     * 成功返回值。
     *
     * @return
     */
    public static <T> ResponseData<T> success() {
        return new ResponseData<T>(SUCCESS, null, null, null);
    }

    /**
     * 成功返回值。
     *
     * @return
     */
    public static <T> ResponseData<T> success(T t) {
        return new ResponseData<T>(SUCCESS, null, null, t);
    }

    /**
     * 成功返回值。
     *
     * @return
     */
    public static <T> ResponseData<T> success(T t, String msg) {
        return new ResponseData<T>(SUCCESS, null, msg, t);
    }


    /**
     * 附带消息的失败返回值。
     *
     * @param msg
     * @return
     */
    public static <T> ResponseData<T> error(String msg) {
        return new ResponseData<T>(ERROR, null, msg, null);
    }

    /**
     * 附带代码，消息的失败返回值。
     * 此方法是为了防止没有T参数，而调用code()时候出错误提示。
     *
     * @param msg
     * @return
     */
    public static <T> ResponseData<T> errorCode(String code, String msg) {
        return new ResponseData<T>(ERROR, code, msg, null);
    }

    /**
     * 附带消息的失败返回值。
     *
     * @param msg
     * @return
     */
    public static <T> ResponseData<T> error(T t, String msg) {
        return new ResponseData<T>(ERROR, null, msg, t);
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
    public ResponseData<T> code(String code) {
        this.code = code;
        return this;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "state=" + state +
                ", msg='" + msg + '\'' +
                '}';
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
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

