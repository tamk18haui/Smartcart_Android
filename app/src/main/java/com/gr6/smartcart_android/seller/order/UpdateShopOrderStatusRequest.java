package com.gr6.smartcart_android.seller.order;

import com.google.gson.annotations.SerializedName;

public class UpdateShopOrderStatusRequest {
    @SerializedName("status")
    private String status;

    @SerializedName("cancelReason")
    private String cancelReason;

    public UpdateShopOrderStatusRequest(String status, String cancelReason) {
        this.status = status;
        this.cancelReason = cancelReason;
    }
}
