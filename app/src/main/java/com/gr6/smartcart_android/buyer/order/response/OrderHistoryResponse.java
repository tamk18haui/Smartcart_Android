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
        return shopName;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount == null ? BigDecimal.ZERO : totalAmount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean canCancel() {
        if (canCancel != null) return canCancel;
        return "PENDING".equals(status);
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
            return productName;
        }

        public String getVariantSku() {
            if (variantSku == null || variantSku.trim().isEmpty()) {
                return "Phân loại";
            }
            return variantSku;
        }

        public Integer getQuantity() {
            return quantity == null ? 1 : quantity;
        }

        public Long getPriceAtPurchase() {
            return priceAtPurchase == null ? 0L : priceAtPurchase;
        }

        public String getImageUrl() {
            return imageUrl == null ? "" : imageUrl;
        }
    }
}