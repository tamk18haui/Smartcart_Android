package com.gr6.smartcart_android.seller.wallet.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RevenueReportResponse {

    @SerializedName("totalRevenue")
    private Long totalRevenue;

    @SerializedName("totalOrders")
    private Long totalOrders;

    @SerializedName("dailyDetails")
    private List<DailyRevenueResponse> dailyDetails;

    public Long getTotalRevenue() {
        return totalRevenue == null ? 0L : totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders == null ? 0L : totalOrders;
    }

    public List<DailyRevenueResponse> getDailyDetails() {
        return dailyDetails == null ? new ArrayList<>() : dailyDetails;
    }
}
