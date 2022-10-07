package net.verytools.unipay.alipay;

public abstract class TradeResult implements Result {

    protected AlipayTradeStatus tradeStatus;

    public void setTradeStatus(AlipayTradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public AlipayTradeStatus getTradeStatus() {
        return tradeStatus;
    }

}
