package com.gr6.smartcart_android.seller.voucher.model;

import com.google.gson.annotations.SerializedName;

public class VoucherResponse {

    @SerializedName("voucherId")
    private Long voucherId;

    @SerializedName("code")
    private String code;

    @SerializedName("discountType")
    private String discountType;

    @SerializedName("discountValue")
    private Long discountValue;

    @SerializedName("minOrderValue")
    private Long minOrderValue;

    @SerializedName("maxDiscountAmount")
    private Long maxDiscountAmount;

    @SerializedName("usageLimit")
    private Integer usageLimit;

    @SerializedName("usedCount")
    private Integer usedCount;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("status")
    private String status;

    public VoucherResponse() {
    }

    public Long getVoucherId() {
        return voucherId;
    }

    public String getCode() {
        return code;
    }

    public String getDiscountType() {
        return discountType;
    }

    public Long getDiscountValue() {
        return discountValue;
    }

    public Long getMinOrderValue() {
        return minOrderValue;
    }

    public Long getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public Integer getUsedCount() {
        return usedCount;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
    }

    public int safeUsedCount() {
        return usedCount == null ? 0 : usedCount;
    }

    public int safeUsageLimit() {
        return usageLimit == null || usageLimit <= 0 ? 1 : usageLimit;
    }

    public long safeDiscountValue() {
        return discountValue == null ? 0L : discountValue;
    }

    public long safeMinOrderValue() {
        return minOrderValue == null ? 0L : minOrderValue;
    }

    public long safeMaxDiscountAmount() {
        return maxDiscountAmount == null ? 0L : maxDiscountAmount;
    }
}
