package com.gr6.smartcart_android.seller.order.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailResponse {
    @SerializedName(value = "id", alternate = {"shopOrderId", "orderId"})
    private Long id;
    @SerializedName(value = "orderCode", alternate = {"orderNumber", "code"})
    private String orderCode;
    private String status;
    @SerializedName(value = "createdAt", alternate = {"orderDate", "createdDate"})
    private String createdAt;
    @SerializedName(value = "totalAmount", alternate = {"total", "finalAmount"})
    private BigDecimal totalAmount;
    @SerializedName(value = "subtotalAmount", alternate = {"subtotal", "itemsTotal"})
    private BigDecimal subtotalAmount;
    @SerializedName(value = "shippingFee", alternate = {"shippingAmount", "deliveryFee"})
    private BigDecimal shippingFee;

    @SerializedName(value = "buyerId", alternate = {"customerId", "userId"})
    private Long buyerId;
    @SerializedName(value = "buyerName", alternate = {"customerName"})
    private String buyerName;
    @SerializedName(value = "receiverName", alternate = {"recipientName", "customerName"})
    private String receiverName;
    private String receiverPhone;
    @SerializedName(value = "shippingAddress", alternate = {"address", "receiverAddress"})
    private String shippingAddress;

    @SerializedName(value = "items", alternate = {"orderItems"})
    private List<OrderItemResponse> items;

    public Long getId() {
        return id;
    }

    public String getOrderCode() {
        if (orderCode != null && !orderCode.trim().isEmpty()) return orderCode;
        return id == null ? "#ORD" : "#ORD-" + id;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public String getBuyerName() {
        if (buyerName != null && !buyerName.trim().isEmpty()) return buyerName;
        return receiverName == null ? "Người mua" : receiverName;
    }

    public String getReceiverName() {
        return receiverName == null ? "" : receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone == null ? "" : receiverPhone;
    }

    public String getShippingAddress() {
        return shippingAddress == null ? "" : shippingAddress;
    }

    public List<OrderItemResponse> getItems() {
        return items == null ? new ArrayList<>() : items;
    }
}
