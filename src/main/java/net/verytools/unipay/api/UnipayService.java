package net.verytools.unipay.api;

import java.util.Map;

/**
 * @author gaols
 */
public interface UnipayService {
    /**
     * 统一下单接口。
     *
     * @param order   订单信息
     * @param context The servlet context for this order.
     * @param mchInfo 下单对应的商户信息
     * @return 下单结果
     */
    PushOrderResult unifyOrder(OrderContext context, Order order, MchInfo mchInfo);

    /**
     * 查询订单支付状态。
     *
     * @param outTradeNo 订单编号
     * @param mchInfo    商户信息
     * @return 订单支付状态。
     */
    TradeStatus queryOrderStatus(String outTradeNo, MchInfo mchInfo);

    /**
     * 取消订单。
     *
     * @param outTradeNo 订单编号
     * @param mchInfo    商户信息
     */
    CancelOrderResult cancelOrder(String outTradeNo, MchInfo mchInfo);

    /**
     * 验签。
     */
    boolean checkSign(Map<String, String> params, String signType, String mchKey);

    /**
     * 退款。
     */
    RefundResult refund(RefundRequest request, MchInfo mchInfo);
}
