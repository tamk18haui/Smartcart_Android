package com.gr6.smartcart_android.seller.wallet.response;

import com.google.gson.annotations.SerializedName;

public class WalletTransactionResponse {

    @SerializedName("walletTxId")
    private Long walletTxId;

    @SerializedName("type")
    private String type;

    @SerializedName("amount")
    private Long amount;

    @SerializedName("description")
    private String description;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getWalletTxId() {
        return walletTxId;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public Long getAmount() {
        return amount == null ? 0L : amount;
    }

    public String getDescription() {
        return description == null || description.trim().isEmpty()
                ? "Giao dịch ví"
                : description.trim();
    }

    public String getCreatedAt() {
        return createdAt == null ? "" : createdAt;
    }

    public boolean isIncome() {
        String normalized = getType().trim().toUpperCase();
        return normalized.contains("IN")
                || normalized.contains("CREDIT")
                || normalized.contains("SETTLEMENT")
                || normalized.contains("REFUND")
                || getAmount() > 0;
    }
}
