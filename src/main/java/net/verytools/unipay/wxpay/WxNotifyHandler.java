package net.verytools.unipay.wxpay;

import net.verytools.unipay.api.PayNotifyBaseHandler;
import net.verytools.unipay.api.PayNotifyParser;
import net.verytools.unipay.api.WxNotifyResponse;

import javax.servlet.http.HttpServletRequest;

public class WxNotifyHandler extends PayNotifyBaseHandler {

    @Override
    public String generateResult(boolean handleResult) {
        return handleResult ? WxNotifyResponse.success("OK") : WxNotifyResponse.fail("OK");
    }

    @Override
    public PayNotifyParser getPayNotifyParser(HttpServletRequest request) {
        return new WxPayNotifyParser(request);
    }
}
