package com.gr6.smartcart_android.buyer.product;

public class ProductActionState {

    private final boolean loading;
    private final boolean success;
    private final String message;

    private ProductActionState(boolean loading, boolean success, String message) {
        this.loading = loading;
        this.success = success;
        this.message = message;
    }

    public static ProductActionState loading() {
        return new ProductActionState(true, false, null);
    }

    public static ProductActionState success(String message) {
        return new ProductActionState(false, true, message);
    }

    public static ProductActionState error(String message) {
        return new ProductActionState(false, false, message);
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