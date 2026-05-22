package com.gr6.smartcart_android.seller.order;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class SellerOrderListResponse {
    @SerializedName("id")
    private Long id;

    @SerializedName("orderCode")
    private String orderCode;

    @SerializedName("totalAmount")
    private BigDecimal totalAmount;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("buyerId")
    private Long buyerId;

    @SerializedName("receiverName")
    private String receiverName;

    @SerializedName("productName")
    private String productName;

    @SerializedName("variantName")
    private String variantName;

    @SerializedName("productImageUrl")
    private String productImageUrl;

    public Long getId() { return id; }
    public String getOrderCode() { return orderCode == null ? "ORD-" + id : orderCode; }
    public BigDecimal getTotalAmount() { return totalAmount == null ? BigDecimal.ZERO : totalAmount; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public Long getBuyerId() { return buyerId; }
    public String getReceiverName() { return receiverName; }
    public String getProductName() { return productName; }
    public String getVariantName() { return variantName; }
    public String getProductImageUrl() { return productImageUrl; }
}
