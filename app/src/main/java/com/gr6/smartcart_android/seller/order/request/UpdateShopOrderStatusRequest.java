package com.gr6.smartcart_android.seller.order.request;

public class UpdateShopOrderStatusRequest {

    private String status;
    private String cancelReason;

    public UpdateShopOrderStatusRequest(String status, String cancelReason) {
        this.status = status;
        this.cancelReason = cancelReason;
    }

    public String getStatus() {
        return status;
    }

    public String getCancelReason() {
        return cancelReason;
    }
}