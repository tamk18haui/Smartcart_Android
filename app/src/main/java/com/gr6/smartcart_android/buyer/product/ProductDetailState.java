package com.gr6.smartcart_android.buyer.product;

import com.gr6.smartcart_android.buyer.product.response.ProductDetailResponse;

public class ProductDetailState {

    private final boolean loading;
    private final boolean success;
    private final String message;
    private final ProductDetailResponse data;

    private ProductDetailState(
            boolean loading,
            boolean success,
            String message,
            ProductDetailResponse data
    ) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ProductDetailState loading() {
        return new ProductDetailState(true, false, null, null);
    }

    public static ProductDetailState success(ProductDetailResponse data) {
        return new ProductDetailState(false, true, null, data);
    }

    public static ProductDetailState error(String message) {
        return new ProductDetailState(false, false, message, null);
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

    public ProductDetailResponse getData() {
        return data;
    }
}