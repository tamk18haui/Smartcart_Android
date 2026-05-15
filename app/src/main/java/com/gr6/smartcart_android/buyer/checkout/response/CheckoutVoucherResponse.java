package com.gr6.smartcart_android.buyer.checkout.response;

public class CheckoutVoucherResponse {

    private Long voucherId;
    private Long shopId;
    private String code;
    private String discountType;
    private Long discountValue;
    private Long minOrderValue;
    private Long maxDiscountAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private String startDate;
    private String endDate;
    private String status;

    public Long getVoucherId() {
        return voucherId;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getCode() {
        return code == null ? "" : code;
    }

    public String getDiscountType() {
        return discountType == null ? "" : discountType;
    }

    public Long getDiscountValue() {
        return discountValue == null ? 0L : discountValue;
    }

    public Long getMinOrderValue() {
        return minOrderValue == null ? 0L : minOrderValue;
    }

    public Long getMaxDiscountAmount() {
        return maxDiscountAmount == null ? 0L : maxDiscountAmount;
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

    public String getDisplayTitle() {
        if ("PERCENT".equalsIgnoreCase(getDiscountType())) {
            return "Giảm " + getDiscountValue() + "%";
        }

        return "Giảm " + formatNumber(getDiscountValue()) + "đ";
    }

    public String getDisplaySubtitle() {
        String text = "Mã " + getCode();

        if (getMinOrderValue() > 0) {
            text += " • Đơn từ " + formatNumber(getMinOrderValue()) + "đ";
        }

        if (getMaxDiscountAmount() > 0 && "PERCENT".equalsIgnoreCase(getDiscountType())) {
            text += " • Tối đa " + formatNumber(getMaxDiscountAmount()) + "đ";
        }

        return text;
    }

    private String formatNumber(long value) {
        return String.format("%,d", value).replace(",", ".");
    }
}