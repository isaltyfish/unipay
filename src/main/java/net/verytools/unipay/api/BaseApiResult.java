package net.verytools.unipay.api;

public class BaseApiResult {
    /**
     * 对应腾讯接口的err_code_des
     * 对应阿里接口的msg
     */
    private String msg;
    /**
     * 对应腾讯接口的err_code
     * 对应阿里接口的code
     */
    private String code;
    /**
     * 腾讯接口没有subCode，腾讯接口该字段为空
     * 对应阿里接口的sub_code
     */
    private String subCode;
    /**
     * 腾讯接口没有subMsg，腾讯接口该字段为空
     * 对应阿里接口的sub_msg
     */
    private String subMsg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSubCode() {
        return subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }

    public String getSubMsg() {
        return subMsg;
    }

    public void setSubMsg(String subMsg) {
        this.subMsg = subMsg;
    }
}
