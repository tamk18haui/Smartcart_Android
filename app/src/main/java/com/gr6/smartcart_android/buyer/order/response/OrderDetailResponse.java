package com.gr6.smartcart_android.buyer.order.response;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailResponse {

    @SerializedName("shopOrderId")
    private Long shopOrderId;

    @SerializedName("shopName")
    private String shopName;

    @SerializedName("status")
    private String status;

    @SerializedName("totalAmount")
    private BigDecimal totalAmount;

    @SerializedName("shippingFee")
    private BigDecimal shippingFee;

    @SerializedName("discountAmount")
    private BigDecimal discountAmount;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("paymentProvider")
    private String paymentProvider;

    @SerializedName("receiverName")
    private String receiverName;

    @SerializedName("shippingAddress")
    private String shippingAddress;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("canCancel")
    private Boolean canCancel;

    @SerializedName("items")
    private List<OrderItemResponse> items;

    public Long getShopOrderId() {
        return shopOrderId;
    }

    public String getOrderCode() {
        if (shopOrderId == null) return "SC-ORDER";
        return "SC-" + shopOrderId;
    }

    public String getShopName() {
        if (shopName == null || shopName.trim().isEmpty()) {
            return "SmartCart Shop";
        }
        return shopName.trim();
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentText() {
        String method = paymentMethod == null ? "" : paymentMethod.trim().toUpperCase();
        String provider = paymentProvider == null ? "" : paymentProvider.trim().toUpperCase();

        if ("COD".equals(method)) {
            return "Thanh toán khi nhận hàng";
        }

        if ("ONLINE".equals(method)) {
            if ("MOMO".equals(provider)) {
                return "Thanh toán online - MoMo";
            }

            if ("VNPAY".equals(provider)) {
                return "Thanh toán online - VNPay";
            }

            return "Thanh toán online";
        }

        if ("WALLET".equals(method)) {
            return "Ví SmartCart";
        }

        return "Chưa có thông tin thanh toán";
    }

    public BigDecimal getTotalAmount() {
        return totalAmount == null ? BigDecimal.ZERO : totalAmount;
    }

    public BigDecimal getShippingFee() {
        return shippingFee == null ? BigDecimal.ZERO : shippingFee;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount == null ? BigDecimal.ZERO : discountAmount;
    }

    public String getReceiverName() {
        if (receiverName == null || receiverName.trim().isEmpty()) {
            return "Người nhận";
        }
        return receiverName.trim();
    }

    public String getShippingAddress() {
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            return "Chưa có địa chỉ giao hàng";
        }
        return shippingAddress.trim();
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean canCancel() {
        return Boolean.TRUE.equals(canCancel);
    }

    public List<OrderItemResponse> getItems() {
        if (items == null) return new ArrayList<>();
        return items;
    }

    public int getItemCount() {
        return getItems().size();
    }

    public static class OrderItemResponse {

        @SerializedName("productName")
        private String productName;

        @SerializedName("variantSku")
        private String variantSku;

        @SerializedName("quantity")
        private Integer quantity;

        @SerializedName("priceAtPurchase")
        private Long priceAtPurchase;

        @SerializedName("imageUrl")
        private String imageUrl;

        public String getProductName() {
            if (productName == null || productName.trim().isEmpty()) {
                return "Sản phẩm SmartCart";
            }
            return productName.trim();
        }

        public String getVariantSku() {
            if (variantSku == null || variantSku.trim().isEmpty()) {
                return "Phân loại mặc định";
            }
            return variantSku.trim();
        }

        public Integer getQuantity() {
            return quantity == null ? 0 : quantity;
        }

        public Long getPriceAtPurchase() {
            return priceAtPurchase == null ? 0L : priceAtPurchase;
        }

        public String getImageUrl() {
            if (imageUrl == null) return "";
            return imageUrl.trim();
        }

        public Long getLineTotal() {
            return getPriceAtPurchase() * getQuantity();
        }
    }
}