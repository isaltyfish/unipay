package net.verytools.unipay.alipay;

import net.verytools.unipay.api.Constants;
import net.verytools.unipay.api.PayNotifyBaseHandler;
import net.verytools.unipay.api.PayNotifyParser;

import javax.servlet.http.HttpServletRequest;

/**
 * @author gaols
 */
public class AlipayNotifyHandler extends PayNotifyBaseHandler {
    @Override
    public String generateResult(boolean handleResult) {
        return handleResult ? Constants.SUCCESS : Constants.FAIL;
    }

    @Override
    public PayNotifyParser getPayNotifyParser(HttpServletRequest request) {
        return new AlipayPayNotifyParser(request);
    }
}
