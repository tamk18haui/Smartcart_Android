package com.gr6.smartcart_android.seller.wallet.response;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

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
        String normalized = getType().trim().toUpperCase(Locale.US);

        if (normalized.contains("WITHDRAW")
                || normalized.contains("OUT")
                || normalized.contains("DEBIT")
                || normalized.contains("PAYOUT")) {
            return false;
        }

        if (normalized.contains("IN")
                || normalized.contains("CREDIT")
                || normalized.contains("SETTLEMENT")
                || normalized.contains("REFUND")) {
            return true;
        }

        return getAmount() > 0;
    }
}
