package com.gr6.smartcart_android.seller.wallet.response;

import com.google.gson.annotations.SerializedName;

public class SellerSettlementResponse {

    @SerializedName("settlementId")
    private Long settlementId;

    @SerializedName("shopOrderId")
    private Long shopOrderId;

    @SerializedName("orderId")
    private Long orderId;

    @SerializedName("grossAmount")
    private Long grossAmount;

    @SerializedName("commissionAmount")
    private Long commissionAmount;

    @SerializedName("netAmount")
    private Long netAmount;

    @SerializedName("status")
    private String status;

    @SerializedName("note")
    private String note;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getSettlementId() {
        return settlementId;
    }

    public Long getShopOrderId() {
        return shopOrderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getGrossAmount() {
        return grossAmount == null ? 0L : grossAmount;
    }

    public Long getCommissionAmount() {
        return commissionAmount == null ? 0L : commissionAmount;
    }

    public Long getNetAmount() {
        return netAmount == null ? 0L : netAmount;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public String getNote() {
        return note == null ? "" : note;
    }

    public String getCreatedAt() {
        return createdAt == null ? "" : createdAt;
    }
}
