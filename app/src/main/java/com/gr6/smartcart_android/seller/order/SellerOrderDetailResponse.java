package com.gr6.smartcart_android.seller.order;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class SellerOrderDetailResponse {
    @SerializedName("id")
    private Long id;

    @SerializedName("orderCode")
    private String orderCode;

    @SerializedName("buyerId")
    private Long buyerId;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("totalAmount")
    private BigDecimal totalAmount;

    @SerializedName("shippingFee")
    private BigDecimal shippingFee;

    @SerializedName("discountAmount")
    private BigDecimal discountAmount;

    @SerializedName("cancelReason")
    private String cancelReason;

    @SerializedName("receiverName")
    private String receiverName;

    @SerializedName("receiverPhone")
    private String receiverPhone;

    @SerializedName("shippingAddress")
    private String shippingAddress;

    @SerializedName("items")
    private List<SellerOrderItemResponse> items;

    public Long getId() { return id; }
    public String getOrderCode() { return orderCode == null ? "ORD-" + id : orderCode; }
    public Long getBuyerId() { return buyerId; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public BigDecimal getTotalAmount() { return totalAmount == null ? BigDecimal.ZERO : totalAmount; }
    public BigDecimal getShippingFee() { return shippingFee == null ? BigDecimal.ZERO : shippingFee; }
    public BigDecimal getDiscountAmount() { return discountAmount == null ? BigDecimal.ZERO : discountAmount; }
    public String getCancelReason() { return cancelReason; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public String getShippingAddress() { return shippingAddress; }
    public List<SellerOrderItemResponse> getItems() { return items; }
}
