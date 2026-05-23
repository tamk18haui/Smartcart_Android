package com.gr6.smartcart_android.seller.wallet.response;

import com.google.gson.annotations.SerializedName;

public class DailyRevenueResponse {

    @SerializedName("date")
    private String date;

    @SerializedName("revenue")
    private Long revenue;

    @SerializedName("orderCount")
    private Long orderCount;

    public DailyRevenueResponse() {
    }

    public DailyRevenueResponse(String date, Long revenue, Long orderCount) {
        this.date = date;
        this.revenue = revenue;
        this.orderCount = orderCount;
    }

    public String getDate() {
        return date == null ? "" : date;
    }

    public Long getRevenue() {
        return revenue == null ? 0L : revenue;
    }

    public Long getOrderCount() {
        return orderCount == null ? 0L : orderCount;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setRevenue(Long revenue) {
        this.revenue = revenue;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }
}
