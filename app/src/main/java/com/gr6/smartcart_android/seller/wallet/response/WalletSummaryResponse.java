package com.gr6.smartcart_android.seller.wallet.response;

import com.google.gson.annotations.SerializedName;

public class WalletSummaryResponse {

    @SerializedName("walletId")
    private Long walletId;

    @SerializedName("sellerId")
    private Long sellerId;

    @SerializedName("shopId")
    private Long shopId;

    @SerializedName("shopName")
    private String shopName;

    @SerializedName("balance")
    private Long balance;

    @SerializedName("status")
    private String status;

    @SerializedName("updatedAt")
    private String updatedAt;

    public Long getWalletId() {
        return walletId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopName() {
        return shopName == null ? "" : shopName;
    }

    public Long getBalance() {
        return balance == null ? 0L : balance;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public String getUpdatedAt() {
        return updatedAt == null ? "" : updatedAt;
    }
}
