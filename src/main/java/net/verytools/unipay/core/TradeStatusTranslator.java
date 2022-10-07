package net.verytools.unipay.core;

import net.verytools.unipay.alipay.AlipayTradeStatus;
import net.verytools.unipay.api.TradeStatus;

public class TradeStatusTranslator {
    public static PushOrderStatus translateAlipayPushOrderStatus(AlipayTradeStatus status) {
        PushOrderStatus retStatus;
        switch (status) {
            case SUCCESS:
                retStatus = PushOrderStatus.SUCCESS;
                break;
            case FAILED:
                retStatus = PushOrderStatus.FAILED;
                break;
            default:
                retStatus = PushOrderStatus.UNKNOWN;
                break;
        }
        return retStatus;
    }

    /**
     * 交易状态：WAIT_BUYER_PAY（交易创建，等待买家付款）、TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）、TRADE_SUCCESS（交易支付成功）、TRADE_FINISHED（交易结束，不可退款）
     *
     * @param status alipay trade status
     */
    public static net.verytools.unipay.api.TradeStatus translateAlipayTradeStatus(String status) {
        net.verytools.unipay.api.TradeStatus retStatus = net.verytools.unipay.api.TradeStatus.UNKNOWN;
        if ("TRADE_SUCCESS".equals(status)) {
            retStatus = net.verytools.unipay.api.TradeStatus.SUCCESS;
        } else if ("WAIT_BUYER_PAY".equals(status)) {
            retStatus = net.verytools.unipay.api.TradeStatus.WAIT_BUYER_PAY;
        } else if ("TRADE_CLOSED".equals(status)) {
            retStatus = net.verytools.unipay.api.TradeStatus.CLOSED;
        } else if ("TRADE_FINISHED".equals(status)) {
            retStatus = TradeStatus.FINISHED;
        }
        return retStatus;
    }

    public static net.verytools.unipay.api.TradeStatus translateWxTradeStatus(String status) {
        net.verytools.unipay.api.TradeStatus retStatus = net.verytools.unipay.api.TradeStatus.UNKNOWN;
        if ("SUCCESS".equals(status)) {
            retStatus = net.verytools.unipay.api.TradeStatus.SUCCESS;
        } else if ("REFUND".equals(status)) {
            retStatus = net.verytools.unipay.api.TradeStatus.REFUND;
        } else if ("NOTPAY".equals(status)) {
            retStatus = net.verytools.unipay.api.TradeStatus.WAIT_BUYER_PAY;
        } else if ("CLOSED".equals(status)) {
            retStatus = net.verytools.unipay.api.TradeStatus.CLOSED;
        } else if ("REVOKED".equals(status)) {
            retStatus = net.verytools.unipay.api.TradeStatus.REVOKED;
        } else if ("USERPAYING".equals(status)) {
            retStatus = net.verytools.unipay.api.TradeStatus.USERPAYING;
        } else if ("PAYERROR".equals(status)) {
            retStatus = net.verytools.unipay.api.TradeStatus.PAYERROR;
        }
        return retStatus;
    }
}
