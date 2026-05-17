package com.gr6.smartcart_android.buyer.order;

import com.gr6.smartcart_android.buyer.order.response.OrderHistoryResponse;

import java.util.List;

public class OrderHistoryState {

    private final boolean loading;
    private final boolean success;
    private final String message;
    private final List<OrderHistoryResponse> data;

    private OrderHistoryState(
            boolean loading,
            boolean success,
            String message,
            List<OrderHistoryResponse> data
    ) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static OrderHistoryState loading() {
        return new OrderHistoryState(true, false, null, null);
    }

    public static OrderHistoryState success(List<OrderHistoryResponse> data, String message) {
        return new OrderHistoryState(false, true, message, data);
    }

    public static OrderHistoryState error(String message) {
        return new OrderHistoryState(false, false, message, null);
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

    public List<OrderHistoryResponse> getData() {
        return data;
    }
}