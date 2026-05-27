package com.gr6.smartcart_android.buyer.order;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryUiModel {

    private final Long orderId;
    private final Long shopOrderId;
    private final Long shopId;
    private final String shopName;
    private final String status;
    private final long totalAmount;
    private final String createdAt;
    private final boolean canCancel;

    private final String paymentStatus;
    private final List<OrderItemUiModel> items;

    public OrderHistoryUiModel(
            Long orderId,
            Long shopOrderId,
            Long shopId,
            String shopName,
            String status,
            String paymentStatus,
            long totalAmount,
            String createdAt,
            boolean canCancel,
            List<OrderItemUiModel> items
    ) {
        this.orderId = orderId;
        this.shopOrderId = shopOrderId;
        this.shopId = shopId;
        this.shopName = shopName == null ? "SmartCart Shop" : shopName;
        this.status = status == null ? "" : status.trim().toUpperCase();
        this.paymentStatus = paymentStatus == null ? "" : paymentStatus.trim().toUpperCase();
        this.totalAmount = totalAmount;
        this.createdAt = createdAt == null ? "--" : createdAt;
        this.canCancel = canCancel;
        this.items = items == null ? new ArrayList<>() : items;
    }
    public String getPaymentStatus() {
        return paymentStatus;
    }

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
        return shopName;
    }

    public String getStatus() {
        return status;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean canCancel() {
        return canCancel;
    }

    public List<OrderItemUiModel> getItems() {
        return items;
    }

    public int getTotalQuantity() {
        int total = 0;

        for (OrderItemUiModel item : items) {
            if (item != null) {
                total += item.getQuantity();
            }
        }

        return total;
    }

    public boolean isAllReviewed() {
        if (items == null || items.isEmpty()) {
            return false;
        }

        for (OrderItemUiModel item : items) {
            if (item == null) {
                return false;
            }

            if (item.getOrderItemId() == null || item.getOrderItemId() <= 0) {
                return false;
            }

            if (!item.isReviewed()) {
                return false;
            }
        }

        return true;
    }

    public static class OrderItemUiModel {

        private final Long orderItemId;
        private final Long productId;
        private final Long variantId;
        private final String productName;
        private final String variantSku;
        private final int quantity;
        private final long priceAtPurchase;
        private final String imageUrl;
        private final boolean canReview;
        private final boolean reviewed;

        public OrderItemUiModel(
                Long orderItemId,
                Long productId,
                Long variantId,
                String productName,
                String variantSku,
                int quantity,
                long priceAtPurchase,
                String imageUrl,
                boolean canReview,
                boolean reviewed
        ) {
            this.orderItemId = orderItemId;
            this.productId = productId;
            this.variantId = variantId;
            this.productName = productName == null ? "Sản phẩm" : productName;
            this.variantSku = variantSku == null ? "" : variantSku;
            this.quantity = quantity <= 0 ? 1 : quantity;
            this.priceAtPurchase = priceAtPurchase;
            this.imageUrl = imageUrl == null ? "" : imageUrl;
            this.canReview = canReview;
            this.reviewed = reviewed;
        }

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
            return productName;
        }

        public String getVariantSku() {
            return variantSku;
        }

        public int getQuantity() {
            return quantity;
        }

        public long getPriceAtPurchase() {
            return priceAtPurchase;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public boolean canReview() {
            return canReview;
        }

        public boolean isReviewed() {
            return reviewed;
        }

        public boolean reviewed() {
            return reviewed;
        }
    }
}