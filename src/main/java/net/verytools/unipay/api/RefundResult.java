package net.verytools.unipay.api;

public abstract class RefundResult {

    private TradeStatus tradeStatus;

    public TradeStatus getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(TradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
    }
}
