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

    public Long getWalletId() {
        return walletId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getSellerEmail() {
        return sellerEmail == null ? "" : sellerEmail;
    }

    public String getSellerName() {
        return sellerName == null ? "" : sellerName;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopName() {
        return shopName == null ? "" : shopName;
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

    public String getSellerNote() {
        return sellerNote == null ? "" : sellerNote;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public String getAdminNote() {
        return adminNote == null ? "" : adminNote;
    }

    public String getTransferCode() {
        return transferCode == null ? "" : transferCode;
    }

    public String getProcessedBy() {
        return processedBy == null ? "" : processedBy;
    }

    public String getProcessedAt() {
        return processedAt == null ? "" : processedAt;
    }

    public String getCreatedAt() {
        return createdAt == null ? "" : createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt == null ? "" : updatedAt;
    }
}
