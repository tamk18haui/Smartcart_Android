package com.gr6.smartcart_android.seller.wallet.request;

import com.google.gson.annotations.SerializedName;

public class AnalyticsDateFilterRequest {

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    public AnalyticsDateFilterRequest(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
