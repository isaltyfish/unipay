package net.verytools.unipay.api;

import net.verytools.unipay.core.Locker;

import javax.servlet.http.HttpServletRequest;

public interface PayNotifyHandler {
    /**
     * 返回给微信或者支付宝的处理结果。
     *
     * @param handleResult true if successfully handled, false otherwise.
     */
    String generateResult(boolean handleResult);

    PayNotifyParser getPayNotifyParser(HttpServletRequest request);

    /**
     * Auto parse the notification and check the signature and invoke callback when necessary.
     *
     * @param request  the notify request
     * @param callback bossiness logic callback when pay is notified.
     * @return appropriate response to alipay pay or weixin pay.
     */
    String handle(HttpServletRequest request, PayNotifyCallback callback, Locker locker);
}
