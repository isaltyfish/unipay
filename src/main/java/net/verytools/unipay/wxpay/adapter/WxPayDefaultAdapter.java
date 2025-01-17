package net.verytools.unipay.wxpay.adapter;

import net.verytools.unipay.api.*;

import java.util.Map;

public class WxPayDefaultAdapter implements UnipayService {

    @Override
    public PushOrderResult unifyOrder(OrderContext context, Order order, MchInfo mchInfo) {
        throw new UnsupportedOperationException(msg());
    }

    @Override
    public TradeStatus queryOrderStatus(String outTradeNo, MchInfo mchInfo) {
        throw new UnsupportedOperationException(msg());
    }

    @Override
    public CancelOrderResult cancelOrder(String outTradeNo, MchInfo mchInfo) {
        throw new UnsupportedOperationException(msg());
    }

    @Override
    public boolean checkSign(Map<String, String> params, String signType, String mchKey) {
        throw new UnsupportedOperationException(msg());
    }

    @Override
    public RefundResult refund(RefundRequest request, MchInfo mchInfo) {
        throw new UnsupportedOperationException(msg());
    }

    private String msg() {
        return "To support weixin pay, you can use either weixin-popular or WxJava";
    }

}
