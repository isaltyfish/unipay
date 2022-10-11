package net.verytools.unipay.api;

public class CancelOrderResult extends BaseApiResult {

    private boolean ok = false;

    public boolean isOk() {
        return ok;
    }

    public void setResult(boolean ok) {
        this.ok = ok;
    }

}
