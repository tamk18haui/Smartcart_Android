package com.gr6.smartcart_android.seller.product;

import com.gr6.smartcart_android.seller.product.response.ProductResponse;

import java.util.ArrayList;
import java.util.List;

public class ProductListState {
    public enum Status { LOADING, SUCCESS, ERROR }

    private final Status status;
    private final List<ProductResponse> products;
    private final String message;

    private ProductListState(Status status, List<ProductResponse> products, String message) {
        this.status = status;
        this.products = products == null ? new ArrayList<>() : products;
        this.message = message;
    }

    public static ProductListState loading() { return new ProductListState(Status.LOADING, new ArrayList<>(), null); }
    public static ProductListState success(List<ProductResponse> products, String message) { return new ProductListState(Status.SUCCESS, products, message); }
    public static ProductListState error(String message) { return new ProductListState(Status.ERROR, new ArrayList<>(), message); }

    public Status getStatus() { return status; }
    public List<ProductResponse> getProducts() { return products; }
    public String getMessage() { return message; }
    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError() { return status == Status.ERROR; }
}
