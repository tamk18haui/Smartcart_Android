package com.gr6.smartcart_android.buyer.order;

public class OrderActionState {

    private final boolean loading;
    private final boolean success;
    private final String message;

    private OrderActionState(boolean loading, boolean success, String message) {
        this.loading = loading;
        this.success = success;
        this.message = message;
    }

    public static OrderActionState loading() {
        return new OrderActionState(true, false, null);
    }

    public static OrderActionState success(String message) {
        return new OrderActionState(false, true, message);
    }

    public static OrderActionState error(String message) {
        return new OrderActionState(false, false, message);
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
}