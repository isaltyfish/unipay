package net.verytools.unipay.wxpay;

import org.apache.commons.lang3.StringUtils;

/**
 * 微信特约商户配置信息
 *
 * @author isaltyfish
 */
public class WxSpMchInfo extends WxpayMchInfo {

    private String subMchId;

    public String getSubMchId() {
        return subMchId;
    }

    public void setSubMchId(String subMchId) {
        this.subMchId = subMchId;
    }

    @Override
    public void validate() {
        super.validate();
        if (StringUtils.isBlank(subMchId)) {
            throw new IllegalArgumentException("subMchId required");
        }
    }
}
