package com.gr6.smartcart_android.buyer.order.request;

public class CancelOrderRequest {

    private String reason;

    public CancelOrderRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}