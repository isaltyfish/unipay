package net.verytools.unipay.api;


import net.verytools.unipay.core.PushOrderStatus;

import java.util.HashMap;
import java.util.Map;

public class PushOrderResult extends BaseApiResult {
    private PushOrderStatus pushOrderStatus;
    private Map<String, Object> response = new HashMap<>();

    public PushOrderStatus getPushOrderStatus() {
        return pushOrderStatus;
    }

    public void setPushOrderStatus(PushOrderStatus pushOrderStatus) {
        this.pushOrderStatus = pushOrderStatus;
    }

    public Map<String, Object> getResponse() {
        return response;
    }

    public boolean isOk() {
        return PushOrderStatus.SUCCESS == this.pushOrderStatus;
    }

    /**
     * 获取生成二维码所需信息。
     */
    public String getQrCodeContent() {
        return (String) response.get(Constants.QRCODE_URL);
    }

    /**
     * 获取商户交易编号。
     */
    public String getOutTradeNo() {
        return (String) response.get(Constants.OUT_TRADE_NO);
    }

    public void setResponse(Map<String, Object> response) {
        this.response = response;
    }
}
