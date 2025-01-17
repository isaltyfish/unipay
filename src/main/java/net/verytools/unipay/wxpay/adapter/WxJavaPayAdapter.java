package net.verytools.unipay.wxpay.adapter;

import com.github.binarywang.wxpay.bean.order.WxPayNativeOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayOrderCloseRequest;
import com.github.binarywang.wxpay.bean.request.WxPayOrderQueryRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.github.binarywang.wxpay.util.SignUtils;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import net.verytools.unipay.api.*;
import net.verytools.unipay.core.PushOrderStatus;
import net.verytools.unipay.core.TradeStatusTranslator;
import net.verytools.unipay.wxpay.WxSpMchInfo;
import net.verytools.unipay.wxpay.WxpayMchInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WxJavaPayAdapter implements UnipayService {

    private static final Logger logger = LoggerFactory.getLogger(WxJavaPayAdapter.class);

    @Override
    public PushOrderResult unifyOrder(OrderContext context, Order order, MchInfo mchInfo) {
        WxPayService wxPayService = payService(mchInfo);
        wxPayService.getConfig().setNotifyUrl(context.getNotifyUrl());
        PushOrderResult ret = new PushOrderResult();
        try {
            WxPayNativeOrderResult pushResult = wxPayService.createOrder(createWxPayUnifiedOrderRequest(context, order));// 内部会自动签名
            ret.setPushOrderStatus(PushOrderStatus.SUCCESS);
            Map<String, Object> resp = new HashMap<>();
            resp.put(Constants.QRCODE_URL, pushResult.getCodeUrl());
            resp.put(Constants.OUT_TRADE_NO, order.getOutTradeNo());
            ret.setResponse(resp);
        } catch (WxPayException e) {
            ret.setPushOrderStatus(PushOrderStatus.FAILED);
            ret.setCode(e.getErrCode());
            ret.setMsg(e.getErrCodeDes());
            logger.error("create order failed", e);
        }

        return ret;
    }

    @Override
    public TradeStatus queryOrderStatus(String outTradeNo, MchInfo mchInfo) {
        WxPayService wxPayService = payService(mchInfo);
        WxPayOrderQueryRequest queryRequest = new WxPayOrderQueryRequest();
        queryRequest.setOutTradeNo(outTradeNo);
        try {
            WxPayOrderQueryResult result = wxPayService.queryOrder(queryRequest);
            String tradeState = result.getTradeState();
            logger.info(String.format("Wx trade[%s] state is: %s", outTradeNo, tradeState));
            return TradeStatusTranslator.translateWxTradeStatus(tradeState);
        } catch (WxPayException e) {
            logger.error("query order status error,outTradeNo={}", outTradeNo, e);
            return TradeStatus.UNKNOWN;
        }
    }

    @Override
    public CancelOrderResult cancelOrder(String outTradeNo, MchInfo mchInfo) {
        WxPayService wxPayService = payService(mchInfo);
        WxPayOrderCloseRequest queryRequest = new WxPayOrderCloseRequest();
        queryRequest.setOutTradeNo(outTradeNo);
        CancelOrderResult ret = new CancelOrderResult();
        try {
            wxPayService.closeOrder(queryRequest);
            ret.setResult(true);
        } catch (WxPayException e) {
            ret.setCode(e.getErrCode());
            ret.setMsg(e.getErrCodeDes());
            logger.error("query order status error,outTradeNo={}", outTradeNo, e);
        }
        return ret;
    }

    @Override
    public boolean checkSign(Map<String, String> params, String signType, String mchKey) {
        return SignUtils.checkSign(params, signType, mchKey);
    }

    @Override
    public RefundResult refund(RefundRequest request, MchInfo mchInfo) {
        WxRefundResult ret = new WxRefundResult();
        try {
            WxPayRefundResult result = payService(mchInfo).refund(createRefundRequest(request));
            if (Constants.SUCCESS.equals(result.getResultCode())) {
                ret.setTradeStatus(TradeStatus.SUCCESS);
            } else if (Constants.FAIL.equals(result.getResultCode())) {
                ret.setTradeStatus(TradeStatus.PAYERROR);
                ret.setCode(result.getErrCode());
                ret.setMsg(result.getErrCodeDes());
            } else {
                ret.setTradeStatus(TradeStatus.UNKNOWN);
            }

            ret.setResultCode(result.getResultCode());
            ret.setErrCode(result.getErrCode());
            ret.setErrCodeDes(result.getErrCodeDes());
            ret.setTransactionId(result.getTransactionId());
            ret.setOutTradeNo(result.getOutTradeNo());
            ret.setOutRefundNo(result.getOutRefundNo());
            ret.setRefundId(result.getRefundId());
            ret.setRefundFee(result.getRefundFee());
            ret.setSettlementRefundFee(result.getSettlementRefundFee());
            ret.setTotalFee(result.getTotalFee());
            ret.setSettlementTotalFee(result.getSettlementTotalFee());
            ret.setFeeType(result.getFeeType());
            ret.setCashFee(result.getCashFee());
            ret.setCashFeeType(result.getCashFeeType());
            ret.setCashRefundFee(result.getCashRefundFee().toString());
        } catch (WxPayException e) {
            ret.setTradeStatus(TradeStatus.UNKNOWN);
        }
        return ret;
    }

    private WxPayUnifiedOrderRequest createWxPayUnifiedOrderRequest(OrderContext context, Order order) {
        WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
        orderRequest.setBody(order.getSubject()); // 商品描述，例如：腾讯充值中心-QQ会员充值，即最好是应用名+商品名
        orderRequest.setOutTradeNo(order.getOutTradeNo());// 商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号
        orderRequest.setTotalFee((int) order.getTotalFee());//分
        orderRequest.setSpbillCreateIp(context.getPayerIp());
        orderRequest.setAttach(order.getAttach());

        String orderDetail = buildOrderDetail(order);
        if (StringUtils.isNotBlank(orderDetail)) {
            orderRequest.setDetail(orderDetail);
        }
        return orderRequest;
    }

    private WxPayService payService(MchInfo mchInfo) {
        WxpayMchInfo info = (WxpayMchInfo) mchInfo;
        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setAppId(info.getAppId());
        payConfig.setMchId(info.getMchId());
        payConfig.setMchKey(info.getMchKey());
        payConfig.setSignType(info.getSignType());
        payConfig.setTradeType("NATIVE");
        payConfig.setKeyPath(info.getKeyPath());

        if (mchInfo instanceof WxSpMchInfo) {
            payConfig.setSubMchId(((WxSpMchInfo) mchInfo).getSubMchId());
        }

        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);
        return wxPayService;
    }

    private String buildOrderDetail(Order order) {
        List<LineItem> items = order.getLineItemList();
        if (items == null || items.isEmpty()) {
            return null;
        }

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setCostPrice((int) order.getTotalFee());
        orderDetail.setGoodDetails(getGoodDetails(items));

        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
                .toJson(orderDetail);
    }

    private List<GoodDetail> getGoodDetails(List<LineItem> items) {
        List<GoodDetail> goodDetailList = new ArrayList<>();
        for (LineItem item : items) {
            GoodDetail goodDetail = new GoodDetail();
            goodDetail.setGoodsId(item.getGoodsId());
            goodDetail.setGoodsName(item.getGoodsName());
            goodDetail.setPrice(item.getPrice());
            goodDetail.setQuantity(item.getQuantity());
            goodDetail.setWxpayGoodsId(item.getVendorGoodsId());
            goodDetailList.add(goodDetail);
        }

        return goodDetailList;
    }

    private WxPayRefundRequest createRefundRequest(RefundRequest request) {
        WxPayRefundRequest.WxPayRefundRequestBuilder refundRequest = WxPayRefundRequest.newBuilder();
        refundRequest.transactionId(request.getTransactionId());
        refundRequest.refundFee(request.getRefundFee());
        refundRequest.totalFee(request.getTotalFee());
        refundRequest.outRefundNo(request.getOutRequestNo()); // 退款单号以R开头
        refundRequest.outTradeNo(request.getOutTradeNo()); // 退款单号以R开头
        refundRequest.notifyUrl(request.getNotifyUrl());
        return refundRequest.build();
    }

}
