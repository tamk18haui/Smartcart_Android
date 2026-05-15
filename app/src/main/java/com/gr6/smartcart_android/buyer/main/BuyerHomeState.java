package com.gr6.smartcart_android.buyer.main;

import com.gr6.smartcart_android.buyer.main.response.HomeCategoryResponse;
import com.gr6.smartcart_android.buyer.main.response.HomeProductResponse;

import java.util.List;

public class BuyerHomeState {

    private final boolean loading;
    private final boolean success;
    private final String message;
    private final List<HomeCategoryResponse> categories;
    private final List<HomeProductResponse> products;

    private BuyerHomeState(
            boolean loading,
            boolean success,
            String message,
            List<HomeCategoryResponse> categories,
            List<HomeProductResponse> products
    ) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.categories = categories;
        this.products = products;
    }

    public static BuyerHomeState loading() {
        return new BuyerHomeState(true, false, null, null, null);
    }

    public static BuyerHomeState success(
            List<HomeCategoryResponse> categories,
            List<HomeProductResponse> products
    ) {
        return new BuyerHomeState(false, true, null, categories, products);
    }

    public static BuyerHomeState error(String message) {
        return new BuyerHomeState(false, false, message, null, null);
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

    public List<HomeCategoryResponse> getCategories() {
        return categories;
    }

    public List<HomeProductResponse> getProducts() {
        return products;
    }
}