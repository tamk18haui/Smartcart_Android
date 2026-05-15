package com.gr6.smartcart_android.buyer.checkout;

public class CheckoutState<T> {

    private final boolean loading;
    private final boolean success;
    private final String message;
    private final T data;

    private CheckoutState(boolean loading, boolean success, String message, T data) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> CheckoutState<T> loading() {
        return new CheckoutState<>(true, false, null, null);
    }

    public static <T> CheckoutState<T> success(T data, String message) {
        return new CheckoutState<>(false, true, message, data);
    }

    public static <T> CheckoutState<T> error(String message) {
        return new CheckoutState<>(false, false, message, null);
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

    public T getData() {
        return data;
    }
}