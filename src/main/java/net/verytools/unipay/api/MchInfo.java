package net.verytools.unipay.api;

import net.verytools.unipay.alipay.AlipayMchInfo;
import net.verytools.unipay.utils.Prop;
import net.verytools.unipay.wxpay.WxSpMchInfo;
import net.verytools.unipay.wxpay.WxpayMchInfo;

public abstract class MchInfo {

    /**
     * 账号标识。
     *
     * @return identifier for an account.
     */
    public abstract String getId();

    /**
     * @param payType  wx or alipay
     * @param filename the properties file name of your pay config which should reside in your classpath
     * @return mch info
     */
    public static MchInfo create(PayType payType, String filename) {
        Prop prop = new Prop(filename);
        switch (payType) {
            case wx:
                WxpayMchInfo wxMchInfo = new WxpayMchInfo();
                wxMchInfo.setAppId(prop.get("app_id"));
                wxMchInfo.setMchKey(prop.get("mch_key")); // api key
                wxMchInfo.setMchId(prop.get("mch_id"));
                wxMchInfo.setKeyPath(prop.get("key_path")); // 退款证书路径
                wxMchInfo.setSignType(prop.get("sign_type", "MD5"));
                wxMchInfo.validate();
                return wxMchInfo;
            case alipay:
                AlipayMchInfo alipayMchInfo = new AlipayMchInfo();
                alipayMchInfo.setOpenApiDomain(prop.get("open_api_domain"));
                alipayMchInfo.setPid(prop.get("pid"));
                alipayMchInfo.setAppid(prop.get("appid"));
                alipayMchInfo.setPrivateKey(prop.get("private_key"));
                alipayMchInfo.setPublicKey(prop.get("public_key"));
                alipayMchInfo.setAlipayPublicKey(prop.get("alipay_public_key"));
                alipayMchInfo.setSignType(prop.get("sign_type", "RAS2"));
                alipayMchInfo.setMaxQueryRetry(prop.getInt("max_query_retry", 3));
                alipayMchInfo.setQueryDuration(prop.getLong("query_duration", 5000L));
                alipayMchInfo.setMaxCancelRetry(prop.getInt("max_cancel_retry", 3));
                alipayMchInfo.setCancelDuration(prop.getLong("cancel_duration", 2000L));
                alipayMchInfo.validate();
                return alipayMchInfo;
            default:
                throw new IllegalArgumentException("invalid payType");
        }
    }

    /**
     * 创建特约商户配置信息。目前只支持微信特约商户，支付宝暂不支持。
     *
     * @param payType  pay type
     * @param filename 配置文件路径，配置文件需要房子
     * @return 特约商户配置信息
     */
    public static MchInfo createSpMchInfo(PayType payType, String filename) {
        Prop prop = new Prop(filename);
        switch (payType) {
            case wx:
                WxSpMchInfo mchInfo = new WxSpMchInfo();
                mchInfo.setAppId(prop.get("app_id"));
                mchInfo.setMchKey(prop.get("mch_key")); // api key
                mchInfo.setMchId(prop.get("mch_id"));
                mchInfo.setSubMchId(prop.get("sub_mch_id"));
                mchInfo.setKeyPath(prop.get("key_path")); // 退款证书路径
                mchInfo.setSignType(prop.get("sign_type", "MD5"));
                mchInfo.validate();
                return mchInfo;
            default:
                throw new IllegalArgumentException("pay type not supported yet: " + payType);
        }
    }

    /**
     * @param payType  wx or alipay
     * @param filename the properties file name of your pay config which should reside in your classpath
     * @return mch info
     */
    public static MchInfo create(String payType, String filename) {
        return create(PayType.valueOf(payType), filename);
    }
}
