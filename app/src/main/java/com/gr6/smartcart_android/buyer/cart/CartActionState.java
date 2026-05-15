package com.gr6.smartcart_android.buyer.cart;

public class CartActionState {

    private final boolean loading;
    private final boolean success;
    private final String message;

    private CartActionState(boolean loading, boolean success, String message) {
        this.loading = loading;
        this.success = success;
        this.message = message;
    }

    public static CartActionState loading() {
        return new CartActionState(true, false, null);
    }

    public static CartActionState success(String message) {
        if (message == null || message.trim().isEmpty()) {
            message = "Thao tác thành công";
        }

        return new CartActionState(false, true, message);
    }

    public static CartActionState error(String message) {
        if (message == null || message.trim().isEmpty()) {
            message = "Thao tác thất bại";
        }

        return new CartActionState(false, false, message);
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