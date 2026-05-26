package com.gr6.smartcart_android.buyer.main.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RecommendationPageResponse {

    @SerializedName("strategy")
    private String strategy;

    @SerializedName("model")
    private String model;

    @SerializedName("products")
    private List<HomeProductResponse> products;

    @SerializedName("page")
    private Integer page;

    @SerializedName("size")
    private Integer size;

    @SerializedName("totalElements")
    private Long totalElements;

    @SerializedName("totalPages")
    private Integer totalPages;

    @SerializedName("hasMore")
    private Boolean hasMore;

    public String getStrategy() {
        return strategy == null ? "" : strategy;
    }

    public String getModel() {
        return model == null ? "" : model;
    }

    public List<HomeProductResponse> getProducts() {
        return products == null ? new ArrayList<>() : products;
    }

    public int getPageIndexZeroBased() {
        return page == null ? 0 : page;
    }

    public int getSize() {
        return size == null ? getProducts().size() : size;
    }

    public long getTotalElements() {
        return totalElements == null ? 0L : totalElements;
    }

    public int getTotalPages() {
        return totalPages == null ? 0 : totalPages;
    }

    public int getPage() {
        return page == null ? 0 : page;
    }

    public boolean isHasMore() {
        return hasMore != null && hasMore;
    }

    public boolean isLast() {
        if (hasMore == null) return true;
        return !hasMore;
    }
}