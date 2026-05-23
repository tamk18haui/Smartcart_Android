package com.gr6.smartcart_android.seller.wallet.response;

import com.google.gson.annotations.SerializedName;

public class WithdrawResponse {

    @SerializedName("withdrawId")
    private Long withdrawId;

    @SerializedName("walletId")
    private Long walletId;

    @SerializedName("sellerId")
    private Long sellerId;

    @SerializedName("sellerEmail")
    private String sellerEmail;

    @SerializedName("sellerName")
    private String sellerName;

    @SerializedName("shopId")
    private Long shopId;

    @SerializedName("shopName")
    private String shopName;

    @SerializedName("amount")
    private Long amount;

    @SerializedName("bankName")
    private String bankName;

    @SerializedName("bankAccountNumber")
    private String bankAccountNumber;

    @SerializedName("bankAccountHolder")
    private String bankAccountHolder;

    @SerializedName("sellerNote")
    private String sellerNote;

    @SerializedName("status")
    private String status;

    @SerializedName("adminNote")
    private String adminNote;

    @SerializedName("transferCode")
    private String transferCode;

    @SerializedName("processedBy")
    private String processedBy;

    @SerializedName("processedAt")
    private String processedAt;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public Long getWithdrawId() {
        return withdrawId;
    }

    public Long getAmount() {
        return amount == null ? 0L : amount;
    }

    public String getBankName() {
        return bankName == null ? "" : bankName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber == null ? "" : bankAccountNumber;
    }

    public String getBankAccountHolder() {
        return bankAccountHolder == null ? "" : bankAccountHolder;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public String getAdminNote() {
        return adminNote == null ? "" : adminNote;
    }

    public String getCreatedAt() {
        return createdAt == null ? "" : createdAt;
    }
}
