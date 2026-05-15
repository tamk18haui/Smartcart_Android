package com.gr6.smartcart_android.buyer.cart;

import com.gr6.smartcart_android.buyer.cart.response.CartDetailResponse;

public class CartListState {

    private final boolean loading;
    private final boolean success;
    private final String message;
    private final CartDetailResponse data;

    private CartListState(boolean loading, boolean success, String message, CartDetailResponse data) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static CartListState loading() {
        return new CartListState(true, false, null, null);
    }

    public static CartListState success(CartDetailResponse data) {
        return new CartListState(false, true, null, data);
    }

    public static CartListState error(String message) {
        return new CartListState(false, false, message, null);
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

    public CartDetailResponse getData() {
        return data;
    }
}