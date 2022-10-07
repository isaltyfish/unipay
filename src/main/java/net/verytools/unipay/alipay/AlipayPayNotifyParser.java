package net.verytools.unipay.alipay;

import net.verytools.unipay.api.MchInfo;
import net.verytools.unipay.api.PayNotifyParser;
import net.verytools.unipay.utils.ParaUtils;
import net.verytools.unipay.utils.sign.AlipaySignCheckUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

/**
 * https://docs.open.alipay.com/194/103296/
 *
 * @author gaols
 */
public class AlipayPayNotifyParser implements PayNotifyParser {

    private final Map<String, String> parasMap;
    private final Map<String, String> originalParasMap;
    private static final Logger logger = LoggerFactory.getLogger(AlipayPayNotifyParser.class);

    @Override
    public boolean isSuccess() {
        String status = parasMap.get("trade_status");
        return "TRADE_SUCCESS".equals(status) || "TRADE_FINISHED".equals(status);
    }

    @Override
    public boolean isSignValid(MchInfo mchInfo) {
        return AlipaySignCheckUtils.rsaSignCheck(originalParasMap, mchInfo);
    }

    @Override
    public Map<String, String> getNotifyParasMap() {
        return Collections.unmodifiableMap(this.parasMap);
    }

    public AlipayPayNotifyParser(HttpServletRequest request) {
        Map map = request.getParameterMap();
        this.originalParasMap = ParaUtils.getParasMap(map);
        this.parasMap = Collections.unmodifiableMap(originalParasMap);
        logParas(parasMap);
    }

    private void logParas(Map<String, String> parasMap) {
        logger.error(ParaUtils.formatParas(parasMap, "\nAlipay Notify:")); // error here means important.
    }
}
