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
    private final List<OrderItemUiModel> items;

    public OrderHistoryUiModel(
            Long orderId,
            Long shopOrderId,
            Long shopId,
            String shopName,
            String status,
            long totalAmount,
            String createdAt,
            boolean canCancel,
            List<OrderItemUiModel> items
    ) {
        this.orderId = orderId;
        this.shopOrderId = shopOrderId;
        this.shopId = shopId;
        this.shopName = shopName;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.canCancel = canCancel;
        this.items = items == null ? new ArrayList<>() : items;
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
            total += item.getQuantity();
        }
        return total;
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

        public OrderItemUiModel(
                Long orderItemId,
                Long productId,
                Long variantId,
                String productName,
                String variantSku,
                int quantity,
                long priceAtPurchase,
                String imageUrl
        ) {
            this.orderItemId = orderItemId;
            this.productId = productId;
            this.variantId = variantId;
            this.productName = productName;
            this.variantSku = variantSku;
            this.quantity = quantity;
            this.priceAtPurchase = priceAtPurchase;
            this.imageUrl = imageUrl;
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
    }
}