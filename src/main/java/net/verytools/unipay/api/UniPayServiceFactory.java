package net.verytools.unipay.api;

import net.verytools.unipay.alipay.AlipayUnipayService;
import net.verytools.unipay.wxpay.WxUnipayService;

/**
 * @author gaols
 */
public class UniPayServiceFactory {

    public static UnipayService getUnipayService(PayType type) {
        switch (type) {
            case wx:
                return WxUnipayService.create();
            case alipay:
                return AlipayUnipayService.create();
        }

        throw new IllegalArgumentException("Unknown pay type:" + type);
    }

    public static UnipayService getUnipayService(String type) {
        return getUnipayService(PayType.valueOf(type));
    }
}
