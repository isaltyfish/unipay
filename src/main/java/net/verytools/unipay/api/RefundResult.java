package net.verytools.unipay.api;

public abstract class RefundResult extends BaseApiResult {

    private TradeStatus tradeStatus;

    public TradeStatus getTradeStatus() {
        return tradeStatus;
    }

    public boolean isOk() {
        return getTradeStatus() == TradeStatus.SUCCESS;
    }

    public void setTradeStatus(TradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
    }
}
