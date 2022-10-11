package net.verytools.unipay.wxpay.adapter;

import net.verytools.unipay.api.*;
import net.verytools.unipay.core.PushOrderStatus;
import net.verytools.unipay.core.TradeStatusTranslator;
import net.verytools.unipay.wxpay.NonceStr;
import net.verytools.unipay.wxpay.WxpayMchInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weixin.popular.api.PayMchAPI;
import weixin.popular.bean.paymch.*;
import weixin.popular.client.LocalHttpClient;
import weixin.popular.util.SignatureUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class WeixinPopularAdapter implements UnipayService {

    private static final Logger logger = LoggerFactory.getLogger(WeixinPopularAdapter.class);
    private static boolean keyInit;
    private static final ReentrantLock lock = new ReentrantLock();

    @Override
    public PushOrderResult unifyOrder(OrderContext context, Order order, MchInfo mchInfo) {
        WxpayMchInfo info = (WxpayMchInfo) mchInfo;
        Unifiedorder unifiedorder = new Unifiedorder();
        unifiedorder.setAppid(info.getAppId());
        unifiedorder.setMch_id(info.getMchId());
        unifiedorder.setNonce_str(NonceStr.gen());
        unifiedorder.setBody(order.getSubject());
        unifiedorder.setOut_trade_no(order.getOutTradeNo());
        unifiedorder.setTotal_fee(String.valueOf(order.getTotalFee()));
        unifiedorder.setAttach(order.getAttach());
        String ip = context.getPayerIp();
        logger.error(String.format("Unify order::payer ip is: %s", ip));
        unifiedorder.setSpbill_create_ip(ip);
        unifiedorder.setNotify_url(context.getNotifyUrl());
        unifiedorder.setTrade_type("NATIVE"); // optimize trade type to support other type of trade.
        unifiedorder.setDetail(createDetail(order));

        UnifiedorderResult result = PayMchAPI.payUnifiedorder(unifiedorder, info.getMchKey());
        logger.error(String.format("Wx result: return_msg=%s,return_code=%s,sign_status=%s,desc=%s", result.getReturn_msg(), result.getReturn_code(), result.getSign_status(), result.getReturn_msg()));
        logUnifyOrderResult(result, order);

        PushOrderResult ret = new PushOrderResult();
        ret.setPushOrderStatus(parsePushOrderStatus(result));
        Map<String, Object> resp = new HashMap<>();
        if (PushOrderStatus.SUCCESS == ret.getPushOrderStatus()) {
            resp.put(Constants.QRCODE_URL, result.getCode_url());
            resp.put("prepay_id", result.getPrepay_id());
            resp.put(Constants.OUT_TRADE_NO, order.getOutTradeNo());
            ret.setResponse(resp);
        } else {
            ret.setCode(result.getErr_code());
            ret.setMsg(result.getErr_code_des());
        }
        return ret;
    }

    private void logUnifyOrderResult(UnifiedorderResult result, Order order) {
        String rtc = result.getReturn_code();
        String rsc = result.getResult_code();
        String ec = result.getErr_code();
        String ecd = result.getErr_code_des();
        String tid = order.getOutTradeNo();
        logger.error(String.format("Unify order[%s] DONE:return_code=%s,result_code=%s,err_code=%s,err_code_desc=%s", tid, rtc, rsc, ec, ecd));
    }

    private Detail createDetail(Order order) {
        List<LineItem> lineItems = order.getLineItemList();
        if (lineItems == null || lineItems.isEmpty()) {
            return null;
        }

        Detail detail = new Detail();
        detail.setCost_price((int) order.getTotalFee());
        List<GoodsDetail> details = new ArrayList<>();
        for (LineItem item : lineItems) {
            GoodsDetail detail1 = new GoodsDetail();
            detail1.setGoods_name(item.getGoodsName());
            detail1.setGoods_id(item.getGoodsId());
            detail1.setBody(item.getBody());
            detail1.setPrice((int) item.getPrice());
            detail1.setQuantity(item.getQuantity());
            details.add(detail1);
        }
        detail.setGoods_detail(details);
        return detail;
    }

    private PushOrderStatus parsePushOrderStatus(UnifiedorderResult result) {
        String returnCode = result.getReturn_code();
        String resultCode = result.getResult_code();
        return isAllSuccess(returnCode, resultCode) ? PushOrderStatus.SUCCESS : PushOrderStatus.FAILED;
    }

    @Override
    public TradeStatus queryOrderStatus(String outTradeNo, MchInfo mchInfo) {
        WxpayMchInfo info = (WxpayMchInfo) mchInfo;
        MchOrderquery query = new MchOrderquery();
        query.setOut_trade_no(outTradeNo);
        query.setAppid(info.getAppId());
        query.setMch_id(info.getMchId());
        query.setNonce_str(NonceStr.gen());
        MchOrderInfoResult result = PayMchAPI.payOrderquery(query, info.getMchKey());
        logger.info(String.format("Wx trade[%s] state is: %s", outTradeNo, result.getTrade_state()));
        return TradeStatusTranslator.translateWxTradeStatus(result.getTrade_state());
    }

    /**
     * 微信在下单五分钟之内是不允许撤销的。
     *
     * @param outTradeNo 订单编号
     */
    @Override
    public CancelOrderResult cancelOrder(String outTradeNo, MchInfo mchInfo) {
        CancelOrderResult ret = new CancelOrderResult();
        WxpayMchInfo info = (WxpayMchInfo) mchInfo;
        Closeorder closeorder = new Closeorder();
        closeorder.setOut_trade_no(outTradeNo);
        closeorder.setAppid(info.getAppId());
        closeorder.setMch_id(info.getMchId());
        closeorder.setNonce_str(NonceStr.gen());
        MchBaseResult mchBaseResult = PayMchAPI.payCloseorder(closeorder, info.getMchKey());
        logger.error(String.format("cancel order[%s] with resp.status[%s]", outTradeNo, mchBaseResult.getResult_code()));
        ret.setCode(mchBaseResult.getErr_code());
        ret.setMsg(mchBaseResult.getErr_code_des());
        ret.setResult(Objects.equals(mchBaseResult.getResult_code(), Constants.SUCCESS));
        return ret;
    }

    @Override
    public boolean checkSign(Map<String, String> params, String signType, String mchKey) {
        return SignatureUtil.validateSign(params, signType, mchKey);
    }

    @Override
    public RefundResult refund(RefundRequest request, MchInfo mchInfo) {
        WxpayMchInfo info = (WxpayMchInfo) mchInfo;
        initKeyStore(info);
        SecapiPayRefund refund = new SecapiPayRefund();
        refund.setOut_refund_no(request.getOutRequestNo());
        refund.setOut_trade_no(request.getOutTradeNo());
        refund.setTransaction_id(request.getTransactionId());
        refund.setRefund_fee(request.getRefundFee());
        refund.setTotal_fee(request.getTotalFee());
        refund.setNotify_url(request.getNotifyUrl());
        SecapiPayRefundResult result = PayMchAPI.secapiPayRefund(refund, info.getMchKey());
        WxRefundResult ret = new WxRefundResult();

        if (Constants.SUCCESS.equals(result.getResult_code())) {
            ret.setTradeStatus(TradeStatus.SUCCESS);
        } else if (Constants.FAIL.equals(result.getResult_code())) {
            ret.setTradeStatus(TradeStatus.PAYERROR);
            ret.setCode(result.getErr_code());
            ret.setMsg(result.getErr_code_des());
        } else {
            ret.setTradeStatus(TradeStatus.UNKNOWN);
        }

        ret.setResultCode(result.getResult_code());
        ret.setErrCode(result.getErr_code());
        ret.setErrCodeDes(result.getErr_code_des());
        ret.setTransactionId(result.getTransaction_id());
        ret.setOutTradeNo(result.getOut_trade_no());
        ret.setOutRefundNo(result.getOut_refund_no());
        ret.setRefundId(result.getRefund_id());
        ret.setRefundFee(result.getRefund_fee());
        ret.setSettlementRefundFee(result.getSettlement_refund_fee());
        ret.setTotalFee(result.getTotal_fee());
        ret.setSettlementTotalFee(result.getSettlement_total_fee());
        ret.setFeeType(result.getFee_type());
        ret.setCashFee(result.getCash_fee());
        ret.setCashRefundFee(String.valueOf(result.getCash_refund_fee()));

        return ret;
    }

    private void initKeyStore(WxpayMchInfo info) {
        InputStream stream = null;
        lock.lock();
        try {
            if (!keyInit) {
                stream = getClass().getClassLoader().getResourceAsStream(info.getKeyPath());
                LocalHttpClient.initMchKeyStore(info.getMchId(), stream);
                keyInit = true;
            }
        } finally {
            lock.unlock();
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

    private static boolean isAllSuccess(String... values) {
        for (String v : values) {
            if (!Constants.SUCCESS.equals(v)) {
                return false;
            }
        }
        return true;
    }
}
