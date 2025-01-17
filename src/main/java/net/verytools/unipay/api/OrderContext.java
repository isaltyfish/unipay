package net.verytools.unipay.api;

import net.verytools.unipay.utils.IpUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author gaols
 */
public class OrderContext {
    /**
     * Order related request.
     */
    private HttpServletRequest request;
    /**
     * 支付超时时间。
     */
    private String payTimeout;
    /**
     * 支付宝服务器/微信主动通知商户服务器里指定的页面http/https路径。
     */
    private String notifyUrl;


    public OrderContext() {
    }

    public OrderContext(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Get the ip of the payer.
     *
     * @return the real ip of the payer.
     */
    public String getPayerIp() {
        return request == null ? "120.210.205.42" : IpUtils.getRealIp(request);
    }

    /**
     * 默认支付超时时间：10分钟(to alipay)。
     */
    public String getPayTimeout() {
        return StringUtils.isBlank(payTimeout) ? "10m" : payTimeout;
    }

    public void setPayTimeout(String payTimeout) {
        this.payTimeout = payTimeout;
    }

    public String getNotifyUrl() {
        if (StringUtils.isBlank(notifyUrl)) {
            throw new IllegalStateException("You should call `orderContext.setNotifyUrl()` method first before call this method");
        }

        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
}
