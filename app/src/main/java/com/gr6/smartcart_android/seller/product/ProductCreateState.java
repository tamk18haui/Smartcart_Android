package com.gr6.smartcart_android.seller.product;

import com.gr6.smartcart_android.seller.product.response.ProductResponse;

public class ProductCreateState {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }

    private final Status status;
    private final ProductResponse data;
    private final String message;

    private ProductCreateState(Status status, ProductResponse data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static ProductCreateState idle() { return new ProductCreateState(Status.IDLE, null, null); }
    public static ProductCreateState loading() { return new ProductCreateState(Status.LOADING, null, null); }
    public static ProductCreateState success(ProductResponse data, String message) { return new ProductCreateState(Status.SUCCESS, data, message); }
    public static ProductCreateState error(String message) { return new ProductCreateState(Status.ERROR, null, message); }

    public Status getStatus() { return status; }
    public ProductResponse getData() { return data; }
    public String getMessage() { return message; }
    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError() { return status == Status.ERROR; }
}
