package com.gr6.smartcart_android.buyer.order;

import com.gr6.smartcart_android.buyer.order.response.OrderDetailResponse;

public class OrderDetailState {

    private final boolean loading;
    private final boolean success;
    private final String message;
    private final OrderDetailResponse data;

    private OrderDetailState(
            boolean loading,
            boolean success,
            String message,
            OrderDetailResponse data
    ) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static OrderDetailState loading() {
        return new OrderDetailState(true, false, null, null);
    }

    public static OrderDetailState success(OrderDetailResponse data, String message) {
        return new OrderDetailState(false, true, message, data);
    }

    public static OrderDetailState error(String message) {
        if (message == null || message.trim().isEmpty()) {
            message = "Không tải được chi tiết đơn hàng";
        }

        return new OrderDetailState(false, false, message, null);
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public OrderDetailResponse getData() {
        return data;
    }
}