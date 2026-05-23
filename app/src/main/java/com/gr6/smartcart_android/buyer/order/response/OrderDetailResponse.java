package com.gr6.smartcart_android.buyer.order.response;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailResponse {

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

    @SerializedName("shippingFee")
    private BigDecimal shippingFee;

    @SerializedName("discountAmount")
    private BigDecimal discountAmount;

    @SerializedName(value = "paymentMethod", alternate = {
            "payment_method",
            "method",
            "payMethod"
    })
    private String paymentMethod;


    @SerializedName(value = "paymentProvider", alternate = {
            "payment_provider",
            "provider",
            "payProvider"
    })
    private String paymentProvider;

    @SerializedName(value = "paymentStatus", alternate = {
            "payment_status",
            "payStatus",
            "transactionStatus",
            "transaction_status"
    })
    private String paymentStatus;

    @SerializedName(value = "updatedAt", alternate = {
            "statusUpdatedAt",
            "processedAt",
            "completedAt",
            "deliveredAt"
    })
    private String updatedAt;

    @SerializedName("receiverName")
    private String receiverName;

    @SerializedName(value = "receiverPhone", alternate = {
            "receiver_phone",
            "phone",
            "phoneNumber",
            "receiverPhoneNumber",
            "shippingPhone"
    })
    private String receiverPhone;

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
    public Long getShopId() {
        return shopId;
    }

    public String getStatus() {
        return status == null ? "" : status;
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

    public String getReceiverPhone() {
        if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
            return "Chưa có số điện thoại";
        }
        return receiverPhone.trim();
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public boolean canCancel() {
        return Boolean.TRUE.equals(canCancel);
    }

    public List<OrderItemResponse> getItems() {
        return items == null ? new ArrayList<>() : items;
    }

    public int getItemCount() {
        int count = 0;

        for (OrderItemResponse item : getItems()) {
            count += item.getQuantity();
        }

        return count;
    }

    public String getPaymentText() {
        String method = paymentMethod == null ? "" : paymentMethod.trim().toUpperCase();
        String provider = paymentProvider == null ? "" : paymentProvider.trim().toUpperCase();

        if ("NONE".equals(provider)) {
            provider = "";
        }

        boolean online =
                "ONLINE".equals(method)
                        || "MOMO".equals(provider)
                        || "VNPAY".equals(provider)
                        || "VN_PAY".equals(provider);

        if (online && isPaid()) {
            return "Đã thanh toán - " + getDeliveryStatusText();
        }

        if ("COD".equals(method)
                || "CASH".equals(method)
                || "CASH_ON_DELIVERY".equals(method)) {
            return "Thanh toán khi nhận hàng";
        }

        if ("MOMO".equals(provider)) {
            return "Thanh toán online - MoMo";
        }

        if ("VNPAY".equals(provider) || "VN_PAY".equals(provider)) {
            return "Thanh toán online - VNPay";
        }

        if ("ONLINE".equals(method)) {
            return "Thanh toán online";
        }

        if ("WALLET".equals(method)) {
            return "Ví SmartCart";
        }

        return "Thanh toán khi nhận hàng";
    }

    public boolean isPaid() {
        String ps = paymentStatus == null ? "" : paymentStatus.trim().toUpperCase();

        return "COMPLETED".equals(ps)
                || "SUCCESS".equals(ps)
                || "PAID".equals(ps);
    }

    public boolean isDelivered() {
        return "DELIVERED".equalsIgnoreCase(getStatus());
    }

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(getStatus());
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(getStatus())
                || "PAYMENT_FAILED".equalsIgnoreCase(getStatus());
    }

    public String getDeliveryStatusText() {
        String s = getStatus() == null ? "" : getStatus().trim().toUpperCase();

        switch (s) {
            case "PENDING_PAYMENT":
                return "Chờ thanh toán";
            case "PENDING":
                return "Chờ xác nhận";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "PREPARING":
                return "Đang chuẩn bị hàng";
            case "SHIPPING":
                return "Đang giao hàng";
            case "DELIVERED":
                return "Đã giao hàng";
            case "COMPLETED":
                return "Hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            case "PAYMENT_FAILED":
                return "Thanh toán thất bại";
            default:
                return "Đang xử lý";
        }
    }

    public String getJourneyCurrentTitle() {
        String s = getStatus() == null ? "" : getStatus().trim().toUpperCase();

        switch (s) {
            case "PENDING_PAYMENT":
                return "Đơn hàng đang chờ thanh toán";
            case "PENDING":
                return "Đơn hàng đang chờ xác nhận";
            case "CONFIRMED":
                return "Shop đã xác nhận đơn hàng";
            case "PREPARING":
                return "Shop đang chuẩn bị hàng";
            case "SHIPPING":
                return "Đơn hàng đang được giao";
            case "DELIVERED":
                return "Đơn hàng đã được giao";
            case "COMPLETED":
                return "Đơn hàng đã hoàn thành";
            case "CANCELLED":
                return "Đơn hàng đã bị hủy";
            case "PAYMENT_FAILED":
                return "Thanh toán thất bại";
            default:
                return "Đơn hàng đang xử lý";
        }
    }

    public String getJourneyCurrentDescription() {
        String s = getStatus() == null ? "" : getStatus().trim().toUpperCase();

        switch (s) {
            case "DELIVERED":
                return "Bạn có thể xác nhận hoàn thành đơn hàng nếu đã nhận đủ sản phẩm.";
            case "COMPLETED":
                return "Cảm ơn bạn đã mua hàng tại SmartCart.";
            case "CANCELLED":
                return "Đơn hàng này đã bị hủy.";
            case "PAYMENT_FAILED":
                return "Thanh toán không thành công, vui lòng tạo đơn mới nếu cần.";
            default:
                return "SmartCart sẽ cập nhật trạng thái đơn hàng khi có thay đổi.";
        }
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
                return "Sản phẩm";
            }
            return productName.trim();
        }

        public String getVariantSku() {
            return variantSku == null ? "" : variantSku;
        }

        public int getQuantity() {
            return quantity == null ? 0 : quantity;
        }

        public long getPriceAtPurchase() {
            return priceAtPurchase == null ? 0L : priceAtPurchase;
        }
        public long getLineTotal() {
            return getPriceAtPurchase() * getQuantity();
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public boolean canReview() {
            return Boolean.TRUE.equals(canReview);
        }

        public boolean isReviewed() {
            return Boolean.TRUE.equals(reviewed);
        }
    }
}