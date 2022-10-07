package net.verytools.unipay.wxpay;

import net.verytools.unipay.api.MchInfo;
import net.verytools.unipay.api.PayNotifyParser;
import net.verytools.unipay.core.WxVendor;
import net.verytools.unipay.utils.IOUtils;
import net.verytools.unipay.utils.ParaUtils;
import net.verytools.unipay.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

/**
 * @author gaols
 */
public class WxPayNotifyParser implements PayNotifyParser {

    private final Map<String, String> parasMap;
    private static final Logger logger = LoggerFactory.getLogger(WxPayNotifyParser.class);

    @Override
    public boolean isSuccess() {
        String resultCode = parasMap.get("result_code");
        return "SUCCESS".equals(resultCode);
    }

    @Override
    public boolean isSignValid(MchInfo mchInfo) {
        WxpayMchInfo info = (WxpayMchInfo) mchInfo;
        return WxVendor.getProxy().checkSign(this.parasMap, info.getSignType(), info.getMchKey());
    }

    @Override
    public Map<String, String> getNotifyParasMap() {
        return parasMap;
    }

    public WxPayNotifyParser(HttpServletRequest request) {
        try {
            String xmlData = IOUtils.copyToString(request.getInputStream(), Charset.forName("UTF-8"));
            this.parasMap = parseXml(xmlData);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public WxPayNotifyParser(String xmlData) {
        this.parasMap = parseXml(xmlData);
    }

    private Map<String, String> parseXml(String xmlData) {
        logger.info("wx pay notify xml:\n" + xmlData);
        Map<String, String> parasMap = Collections.unmodifiableMap(XmlUtils.parseXml(xmlData));
        logParas(parasMap);
        return parasMap;
    }

    private void logParas(Map<String, String> parasMap) {
        logger.info(ParaUtils.formatParas(parasMap, "\nWeixin Notify:"));
    }
}
