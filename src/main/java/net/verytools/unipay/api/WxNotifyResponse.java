package net.verytools.unipay.api;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import me.chanjar.weixin.common.util.xml.XStreamInitializer;

@XStreamAlias("xml")
public class WxNotifyResponse {

    @XStreamAlias("return_code")
    private String returnCode;
    @XStreamAlias("return_msg")
    private String returnMsg;

    public static String success(String msg) {
        WxNotifyResponse resp = new WxNotifyResponse();
        resp.returnCode = Constants.SUCCESS;
        resp.returnMsg = msg;
        return resp.toXml();
    }

    public static String fail(String msg) {
        WxNotifyResponse resp = new WxNotifyResponse();
        resp.returnCode = Constants.FAIL;
        resp.returnMsg = msg;
        return resp.toXml();
    }

    public String toXml() {
        XStream xstream = XStreamInitializer.getInstance();
        xstream.autodetectAnnotations(true);
        return xstream.toXML(this);
    }

    public String getReturnCode() {
        return returnCode;
    }

    public String getReturnMsg() {
        return returnMsg;
    }
}
