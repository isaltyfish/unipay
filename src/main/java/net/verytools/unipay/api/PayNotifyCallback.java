package net.verytools.unipay.api;

import java.util.Map;

public interface PayNotifyCallback {

    /**
     * do business logic in this callback method.
     *
     * @param outTradeNo  out trade no
     * @param notifyParas all notification params
     */
    void onPaySuccess(String outTradeNo, Map<String, String> notifyParas);

    /**
     * @return true if the notification has already handled, false otherwise.
     */
    boolean isNotifyHandled(String outTradeNo);

    /**
     * @param notifyParas all notification params
     * @return mch info
     */
    MchInfo resolveMchInfo(Map<String, String> notifyParas);
}
