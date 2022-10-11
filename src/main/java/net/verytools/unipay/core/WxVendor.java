package net.verytools.unipay.core;

import net.verytools.unipay.api.UnipayService;
import net.verytools.unipay.wxpay.adapter.WeixinPopularAdapter;
import net.verytools.unipay.wxpay.adapter.WxJavaPayAdapter;
import net.verytools.unipay.wxpay.adapter.WxPayDefaultAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WxVendor {

    private static final Logger logger = LoggerFactory.getLogger(WxVendor.class);

    private UnipayService proxy;
    private static final WxVendor vendor = new WxVendor();

    public static UnipayService getProxy() {
        return vendor.proxy;
    }

    private void tryProxy(String proxyClass, UnipayService adapter) {
        if (proxy == null) {
            try {
                Class.forName(proxyClass);
                proxy = adapter;
                logger.info("{} detected", proxyClass);
            } catch (ClassNotFoundException e) {
                logger.info("{} not in classpath", proxyClass);
            }
        }
    }

    private WxVendor() {
        tryProxy("weixin.popular.bean.paymch.Unifiedorder", new WeixinPopularAdapter());
        tryProxy("com.github.binarywang.wxpay.service.WxPayService", new WxJavaPayAdapter());
        if (proxy == null) {
            proxy = new WxPayDefaultAdapter();
        }
    }

}
