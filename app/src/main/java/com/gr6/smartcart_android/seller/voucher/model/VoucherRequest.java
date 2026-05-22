package com.gr6.smartcart_android.seller.voucher.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request gửi lên backend.
 * Backend đang dùng DiscountType: PERCENT / FIXED
 * Ngày gửi dạng ISO LocalDateTime: yyyy-MM-dd'T'HH:mm:ss
 */
public class VoucherRequest {

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

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    public VoucherRequest() {
    }

    public VoucherRequest(
            String code,
            String discountType,
            Long discountValue,
            Long minOrderValue,
            Long maxDiscountAmount,
            Integer usageLimit,
            String startDate,
            String endDate
    ) {
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderValue = minOrderValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.usageLimit = usageLimit;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}


