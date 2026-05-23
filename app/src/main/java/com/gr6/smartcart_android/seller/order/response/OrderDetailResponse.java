package com.gr6.smartcart_android.seller.order.response;

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

    /*
     * Không để customerName trùng với receiverName.
     * Backend hiện tại trả buyerName riêng và receiverName riêng.
     */
    @SerializedName(value = "buyerName", alternate = {"customerName"})
    private String buyerName;

    /*
     * Không thêm customerName vào đây vì Gson sẽ báo duplicate field.
     */
    @SerializedName(value = "receiverName", alternate = {"recipientName"})
    private String receiverName;

    @SerializedName(value = "receiverPhone", alternate = {"recipientPhone", "phone"})
    private String receiverPhone;

    @SerializedName(value = "shippingAddress", alternate = {"address", "receiverAddress"})
    private String shippingAddress;

    @SerializedName(value = "items", alternate = {"orderItems"})
    private List<OrderItemResponse> items;

    public Long getId() {
        return id;
    }

    public String getOrderCode() {
        if (orderCode != null && !orderCode.trim().isEmpty()) {
            return orderCode;
        }

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
        if (buyerName != null && !buyerName.trim().isEmpty()) {
            return buyerName;
        }

        if (receiverName != null && !receiverName.trim().isEmpty()) {
            return receiverName;
        }

        return "Người mua";
    }

    public String getReceiverName() {
        if (receiverName != null && !receiverName.trim().isEmpty()) {
            return receiverName;
        }

        return buyerName == null ? "" : buyerName;
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