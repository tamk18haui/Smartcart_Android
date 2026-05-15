package com.gr6.smartcart_android.buyer.checkout.response;

public class CheckoutOrderResponse {

    private Long orderId;
    private Long transactionId;
    private String paymentUrl;
    private String orderStatus;
    private String paymentStatus;
    private String paymentProvider;
    private String checkoutSource;
    private Long totalAmount;

    public Long getOrderId() {
        return orderId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public String getCheckoutSource() {
        return checkoutSource;
    }

    public Long getTotalAmount() {
        return totalAmount == null ? 0L : totalAmount;
    }
}