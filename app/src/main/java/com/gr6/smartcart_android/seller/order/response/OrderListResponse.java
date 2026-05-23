package com.gr6.smartcart_android.seller.order.response;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class OrderListResponse {

    @SerializedName(value = "id", alternate = {"shopOrderId", "orderId"})
    private Long id;

    @SerializedName(value = "orderCode", alternate = {"orderNumber", "code"})
    private String orderCode;

    @SerializedName(value = "totalAmount", alternate = {"total", "finalAmount"})
    private BigDecimal totalAmount;

    private String status;

    @SerializedName(value = "createdAt", alternate = {"orderDate", "createdDate"})
    private String createdAt;

    @SerializedName(value = "customerName", alternate = {"buyerName", "receiverName"})
    private String customerName;

    private String receiverPhone;

    @SerializedName(value = "firstProductName", alternate = {"productName"})
    private String firstProductName;

    @SerializedName(value = "firstVariantName", alternate = {"variantName", "sku", "variantSku"})
    private String firstVariantName;

    @SerializedName(
            value = "firstProductImage",
            alternate = {
                    "imageUrl",
                    "productImage",
                    "productImageUrl",
                    "variantImageUrl",
                    "thumbnailUrl"
            }
    )
    private String firstProductImage;

    public Long getId() {
        return id;
    }

    public String getOrderCode() {
        if (orderCode != null && !orderCode.trim().isEmpty()) {
            return orderCode;
        }
        return id == null ? "#ORD" : "#ORD-" + id;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCustomerName() {
        return customerName == null ? "" : customerName;
    }

    public String getReceiverPhone() {
        return receiverPhone == null ? "" : receiverPhone;
    }

    public String getFirstProductName() {
        return firstProductName == null || firstProductName.trim().isEmpty()
                ? "Sản phẩm"
                : firstProductName;
    }

    public String getFirstVariantName() {
        return firstVariantName == null ? "" : firstVariantName;
    }

    public String getFirstProductImage() {
        return firstProductImage;
    }
}