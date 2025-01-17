package net.verytools.unipay.api;

import net.verytools.unipay.core.Locker;
import net.verytools.unipay.core.SimpleLocker;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public abstract class PayNotifyBaseHandler implements PayNotifyHandler {

    private static final Locker locker = new SimpleLocker();

    @Override
    public String handle(HttpServletRequest request, PayNotifyCallback callback, Locker handlerLock) {
        String lockName = String.valueOf(System.currentTimeMillis());
        PayNotifyParser parser = getPayNotifyParser(request);
        Map<String, String> parasMap = parser.getNotifyParasMap();
        String outTradeNo = parasMap.get(Constants.OUT_TRADE_NO);
        if (StringUtils.isNotBlank(outTradeNo)) {
            lockName = outTradeNo;
        }
        Locker lock = locker;
        if (handlerLock != null) {
            lock = handlerLock;
        }
        try {
            lock.lock(lockName);
            if (callback.isNotifyHandled(outTradeNo)) {
                return generateResult(true);
            }

            MchInfo mchInfo = callback.resolveMchInfo(parasMap);
            if (parser.isSignValid(mchInfo) && parser.isSuccess()) {
                callback.onPaySuccess(outTradeNo, parasMap);
                return generateResult(true);
            }

            return generateResult(false);
        } finally {
            lock.release(lockName);
        }
    }

}
