package com.gr6.smartcart_android.seller.order.model;

public class UpdateShopOrderStatusRequest {
    private String status;
    private String cancelReason;

    public UpdateShopOrderStatusRequest(String status, String reason) {
        this.status = status;
        this.cancelReason = reason;
    }

    public String getStatus() {
        return status;
    }

    public String getCancelReason() {
        return cancelReason;
    }
}
