package net.verytools.unipay.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import net.verytools.unipay.api.*;
import net.verytools.unipay.core.PushOrderStatus;
import net.verytools.unipay.core.TradeStatusTranslator;
import net.verytools.unipay.utils.MathUtils;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://opendocs.alipay.com/open/02ekfk
 * https://docs.open.alipay.com/api_1/alipay.trade.precreate/
 * https://docs.open.alipay.com/194/105322/ -- a must read
 *
 * @author gaols
 */
public class AlipayUnipayService implements UnipayService {

    private static final Logger logger = LoggerFactory.getLogger(AlipayUnipayService.class);
    private static final AlipayUnipayService orderService = new AlipayUnipayService();

    /**
     * https://opendocs.alipay.com/open/194/106078?ref=api
     *
     * @param context       The servlet context for this order.
     * @param order         订单信息
     * @param alipayMchInfo 下单对应的商户信息
     * @return 下单结果
     * <p>
     * 预下单请求生成的二维码有效时间为2小时。
     */
    @Override
    public PushOrderResult unifyOrder(OrderContext context, Order order, MchInfo alipayMchInfo) {
        logger.info("Unify order START: {}", order.toString());

        AlipayMchInfo mchInfo = (AlipayMchInfo) alipayMchInfo;
        AlipayClient alipayClient = new DefaultAlipayClient(mchInfo.getOpenApiDomain(), mchInfo.getAppid(), mchInfo.getPrivateKey(), "json", "UTF-8", mchInfo.getAlipayPublicKey(), "RSA2");
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(context.getNotifyUrl());

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", order.getOutTradeNo());
        bizContent.put("total_amount", convertTotalAmount(order.getTotalFee()));
        bizContent.put("subject", order.getSubject());
        if (CollectionUtils.isNotEmpty(order.getLineItemList())) {
            bizContent.put("goods_detail", getGoodsDetails(order));
        }

        request.setBizContent(bizContent.toString());

        PushOrderResult ret = new PushOrderResult();
        Map<String, Object> data = new HashMap<>();
        ret.setResponse(data);

        AlipayTradePrecreateResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            logger.error("[alipay] unify order error", e);
            ret.setPushOrderStatus(PushOrderStatus.FAILED);
            data.put("msg", "alipay api exception: " + e.getMessage());
            return ret;
        }

        data.put("code", response.getCode());
        data.put("msg", response.getMsg());
        data.put("sub_code", response.getSubCode());
        data.put("sub_msg", response.getSubMsg());

        if (response.isSuccess()) {
            ret.setPushOrderStatus(PushOrderStatus.SUCCESS);
            data.put("qr_code_url", response.getQrCode());
            data.put("out_trade_no", response.getOutTradeNo());
        } else {
            ret.setPushOrderStatus(PushOrderStatus.FAILED);
        }

        return ret;
    }

    private JSONArray getGoodsDetails(Order order) {
        // 商品明细信息，按需传入
        JSONArray goodsDetails = new JSONArray();
        List<LineItem> lineItemList = order.getLineItemList();
        for (LineItem lineItem : lineItemList) {
            JSONObject goods1 = new JSONObject();
            goods1.put("goods_id", lineItem.getGoodsId());
            goods1.put("goods_name", lineItem.getGoodsName());
            goods1.put("quantity", lineItem.getQuantity());
            goods1.put("price", convertTotalAmount(lineItem.getPrice()));
            goodsDetails.put(goods1);
        }
        return goodsDetails;
    }

    /**
     * amount是单位是分，这里需要将其转化元。
     *
     * @param amount the amount of cents.
     * @return Yuan
     */
    private static String convertTotalAmount(long amount) {
        double m = amount * 1.0d / 100.0d;
        BigDecimal value = BigDecimal.valueOf(m);
        return new DecimalFormat("#.##").format(value);
    }

    /**
     * 交易状态：WAIT_BUYER_PAY（交易创建，等待买家付款）、TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）、TRADE_SUCCESS（交易支付成功）、TRADE_FINISHED（交易结束，不可退款）
     *
     * @param outTradeNo    订单编号
     * @param alipayMchInfo 商户信息
     * @return 支付交易状态
     */
    @Override
    public TradeStatus queryOrderStatus(String outTradeNo, MchInfo alipayMchInfo) {
        AlipayMchInfo mchInfo = (AlipayMchInfo) alipayMchInfo;
        AlipayClient alipayClient = new DefaultAlipayClient(mchInfo.getOpenApiDomain(), mchInfo.getAppid(), mchInfo.getPrivateKey(), "json", "UTF-8", mchInfo.getAlipayPublicKey(), "RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        request.setBizContent(bizContent.toString());
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            String tradeStatus = response.getTradeStatus();
            return TradeStatusTranslator.translateAlipayTradeStatus(tradeStatus);
        } catch (AlipayApiException e) {
            logger.error("query status error", e);
            return TradeStatus.UNKNOWN;
        }
    }

    /**
     * Before cancel this order, we must ensure the order is not paid, if payed success, block it.
     * 每一笔交易一定要闭环，即要么支付成功，要么撤销交易，一定不能有交易一直停留在等待用户付款的状态。
     * 轮询+撤销的流程中，如轮询的结果一直为未付款，撤销一定要紧接着最后一次查询，当中不能有时间间隔。
     * <p>
     * 支付交易返回失败或支付系统超时，调用该接口撤销交易。如果此订单用户支付失败，支付宝系统会将此订单关闭；
     * 如果用户支付成功，支付宝系统会将此订单资金退还给用户。 注意：只有发生支付系统超时或者支付结果未知时可调用撤销，
     * 其他正常支付的单如需实现相同功能请调用申请退款API。提交支付交易后调用【查询订单API】，没有明确的支付结果再调用【撤销订单API】。
     *
     * @param outTradeNo 订单编号
     */
    @Override
    public void cancelOrder(String outTradeNo, MchInfo alipayMchInfo) {
        AlipayMchInfo mchInfo = (AlipayMchInfo) alipayMchInfo;
        AlipayClient alipayClient = new DefaultAlipayClient(mchInfo.getOpenApiDomain(), mchInfo.getAppid(), mchInfo.getPrivateKey(), "json", "UTF-8", mchInfo.getAlipayPublicKey(), "RSA2");
        AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        request.setBizContent(bizContent.toString());
        try {
            AlipayTradeCancelResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                logger.error("[alipay] cancel order failed, code: {}, msg: {}, sub_code: {}, sub_msg: {}", response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            logger.error("[alipay] cancel order failed", e);
        }
    }

    @Override
    public boolean checkSign(Map<String, String> params, String signType, String alipayPubicKey) {
        try {
            return AlipaySignature.rsaCheckV1(params, alipayPubicKey, "UTF-8", signType);
        } catch (AlipayApiException e) {
            logger.error("sign check failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public RefundResult refund(RefundRequest request, MchInfo alipayMchInfo) {
        AlipayRefundResult ret = new AlipayRefundResult();
        AlipayMchInfo mchInfo = (AlipayMchInfo) alipayMchInfo;
        AlipayClient alipayClient = new DefaultAlipayClient(mchInfo.getOpenApiDomain(), mchInfo.getAppid(), mchInfo.getPrivateKey(), "json", "UTF-8", mchInfo.getAlipayPublicKey(), "RSA2");
        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("trade_no", request.getOutTradeNo());
        bizContent.put("refund_amount", MathUtils.centsToYuan(request.getRefundFee()));
        bizContent.put("out_request_no", request.getOutRequestNo());
        try {
            AlipayTradeRefundResponse response = alipayClient.execute(refundRequest);
            if (response.isSuccess()) {
                ret.setTradeStatus(TradeStatus.SUCCESS);
                ret.setBuyerLogonId(response.getBuyerLogonId());
                ret.setBuyerUserId(response.getBuyerUserId());
                ret.setFundChange(response.getFundChange());
                ret.setGmtRefundPay(response.getGmtRefundPay());
                ret.setOpenId(response.getOpenId());
                ret.setOutTradeNo(response.getOutTradeNo());
                ret.setPresentRefundBuyerAmount(response.getPresentRefundBuyerAmount());
                ret.setPresentRefundDiscountAmount(response.getPresentRefundDiscountAmount());
                ret.setPresentRefundMdiscountAmount(response.getPresentRefundMdiscountAmount());
                ret.setRefundCurrency(response.getRefundCurrency());
                ret.setRefundDetailItemList(response.getRefundDetailItemList());
                ret.setRefundFee(response.getRefundFee());
                ret.setSendBackFee(response.getSendBackFee());
                ret.setStoreName(response.getStoreName());
                ret.setTradeNo(response.getTradeNo());
            } else {
                ret.setTradeStatus(TradeStatus.PAYERROR);
                logger.error("[alipay] cancel order failed, code: {}, msg: {}, sub_code: {}, sub_msg: {}", response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            ret.setTradeStatus(TradeStatus.UNKNOWN);
            logger.error("[alipay] cancel order failed", e);
        }
        return ret;
    }

    public static UnipayService create() {
        return orderService;
    }

    private AlipayUnipayService() {
    }

}
