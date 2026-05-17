package com.gr6.smartcart_android.buyer.checkout.response;

import com.google.gson.annotations.SerializedName;

public class CheckoutVoucherResponse {

    @SerializedName("voucherId")
    private Long voucherId;

    @SerializedName("shopId")
    private Long shopId;

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

    @SerializedName("usable")
    private Boolean usable;

    @SerializedName("usedByCurrentUser")
    private Boolean usedByCurrentUser;

    @SerializedName("unavailableReason")
    private String unavailableReason;

    @SerializedName("displayTitle")
    private String displayTitle;

    @SerializedName("displaySubtitle")
    private String displaySubtitle;

    public Long getVoucherId() {
        return voucherId;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getCode() {
        return code == null ? "" : code.trim();
    }

    public String getDiscountType() {
        return discountType;
    }

    public Long getDiscountValue() {
        return discountValue == null ? 0L : discountValue;
    }

    public Long getMinOrderValue() {
        return minOrderValue == null ? 0L : minOrderValue;
    }

    public Long getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public Integer getUsageLimit() {
        return usageLimit == null ? 0 : usageLimit;
    }

    public Integer getUsedCount() {
        return usedCount == null ? 0 : usedCount;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public boolean isUsable() {
        return usable == null || usable;
    }

    public boolean isUsedByCurrentUser() {
        return usedByCurrentUser != null && usedByCurrentUser;
    }

    public String getUnavailableReason() {
        return unavailableReason == null ? "" : unavailableReason;
    }

    public String getDisplayTitle() {
        if (displayTitle != null && !displayTitle.trim().isEmpty()) {
            return displayTitle;
        }

        if ("PERCENT".equalsIgnoreCase(discountType)) {
            return "Giảm " + getDiscountValue() + "%";
        }

        return "Giảm " + formatVnd(getDiscountValue());
    }

    public String getDisplaySubtitle() {
        if (displaySubtitle != null && !displaySubtitle.trim().isEmpty()) {
            return displaySubtitle;
        }

        if (getMinOrderValue() > 0) {
            return "Đơn tối thiểu " + formatVnd(getMinOrderValue());
        }

        return "Không yêu cầu đơn tối thiểu";
    }

    private String formatVnd(Long value) {
        long amount = value == null ? 0L : value;
        return String.format("%,d", amount).replace(",", ".") + "đ";
    }
}