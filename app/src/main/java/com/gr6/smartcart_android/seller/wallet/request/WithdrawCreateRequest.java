package com.gr6.smartcart_android.seller.wallet.request;

import com.google.gson.annotations.SerializedName;

public class WithdrawCreateRequest {

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

    public WithdrawCreateRequest(
            Long amount,
            String bankName,
            String bankAccountNumber,
            String bankAccountHolder,
            String sellerNote
    ) {
        this.amount = amount;
        this.bankName = bankName;
        this.bankAccountNumber = bankAccountNumber;
        this.bankAccountHolder = bankAccountHolder;
        this.sellerNote = sellerNote;
    }
}
