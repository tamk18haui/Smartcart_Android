package com.gr6.smartcart_android.buyer.order.response;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryResponse {

    @SerializedName("orderId")
    private Long orderId;

    @SerializedName("shopOrderId")
    private Long shopOrderId;

    @SerializedName("shopId")
    private Long shopId;

    @SerializedName("shopName")
    private String shopName;

    @SerializedName("status")
    private String status;

    @SerializedName("totalAmount")
    private BigDecimal totalAmount;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("canCancel")
    private Boolean canCancel;

    @SerializedName("items")
    private List<OrderItemResponse> items;

    public Long getOrderId() {
        return orderId;
    }

    public Long getShopOrderId() {
        return shopOrderId;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopName() {
        if (shopName == null || shopName.trim().isEmpty()) {
            return "SmartCart Shop";
        }

        return shopName.trim();
    }

    public String getStatus() {
        return status == null ? "" : status.trim().toUpperCase();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount == null ? BigDecimal.ZERO : totalAmount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean canCancel() {
        if (canCancel != null) return canCancel;
        return "PENDING".equalsIgnoreCase(getStatus());
    }

    public List<OrderItemResponse> getItems() {
        if (items == null) return new ArrayList<>();
        return items;
    }

    public static class OrderItemResponse {

        @SerializedName("orderItemId")
        private Long orderItemId;

        @SerializedName("productId")
        private Long productId;

        @SerializedName("variantId")
        private Long variantId;

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

        @SerializedName("canReview")
        private Boolean canReview;

        @SerializedName("reviewed")
        private Boolean reviewed;

        public Long getOrderItemId() {
            return orderItemId;
        }

        public Long getProductId() {
            return productId;
        }

        public Long getVariantId() {
            return variantId;
        }

        public String getProductName() {
            if (productName == null || productName.trim().isEmpty()) {
                return "Sản phẩm SmartCart";
            }

            return productName.trim();
        }

        public String getVariantSku() {
            if (variantSku == null || variantSku.trim().isEmpty()) {
                return "";
            }

            return variantSku.trim();
        }

        public int getQuantity() {
            return quantity == null || quantity <= 0 ? 1 : quantity;
        }

        public long getPriceAtPurchase() {
            return priceAtPurchase == null ? 0L : priceAtPurchase;
        }

        public String getImageUrl() {
            return imageUrl == null ? "" : imageUrl;
        }

        public boolean canReview() {
            return Boolean.TRUE.equals(canReview);
        }

        public boolean isReviewed() {
            return Boolean.TRUE.equals(reviewed);
        }

        public boolean reviewed() {
            return isReviewed();
        }
    }
}