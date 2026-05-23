package com.gr6.smartcart_android.seller.product;

import com.gr6.smartcart_android.seller.product.response.ProductResponse;

public class ProductDetailState {

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final ProductResponse product;
    private final String message;

    private ProductDetailState(Status status, ProductResponse product, String message) {
        this.status = status;
        this.product = product;
        this.message = message;
    }

    public static ProductDetailState loading() {
        return new ProductDetailState(Status.LOADING, null, null);
    }

    public static ProductDetailState success(ProductResponse product, String message) {
        return new ProductDetailState(Status.SUCCESS, product, message);
    }

    public static ProductDetailState error(String message) {
        return new ProductDetailState(Status.ERROR, null, message);
    }

    public Status getStatus() {
        return status;
    }

    public ProductResponse getProduct() {
        return product;
    }

    public String getMessage() {
        return message;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}