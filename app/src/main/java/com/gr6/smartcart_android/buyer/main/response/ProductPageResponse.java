package com.gr6.smartcart_android.buyer.main.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ProductPageResponse {

    @SerializedName("content")
    private List<HomeProductResponse> content;

    @SerializedName("data")
    private List<HomeProductResponse> data;

    @SerializedName("number")
    private Integer number;

    @SerializedName("currentPage")
    private Integer currentPage;

    @SerializedName("size")
    private Integer size;

    @SerializedName("pageSize")
    private Integer pageSize;

    @SerializedName("totalPages")
    private Integer totalPages;

    @SerializedName("totalElements")
    private Long totalElements;

    @SerializedName("last")
    private Boolean last;

    public List<HomeProductResponse> getProducts() {
        if (content != null) return content;
        if (data != null) return data;
        return new ArrayList<>();
    }

    public int getPageIndexZeroBased() {
        if (number != null) return number;

        if (currentPage != null && currentPage > 0) {
            return currentPage - 1;
        }

        return 0;
    }

    public int getSize() {
        if (size != null) return size;
        if (pageSize != null) return pageSize;
        return getProducts().size();
    }

    public int getTotalPages() {
        return totalPages == null ? 0 : totalPages;
    }

    public long getTotalElements() {
        return totalElements == null ? 0L : totalElements;
    }

    public boolean isLast() {
        if (last != null) return last;

        if (totalPages <= 0) return true;

        return getPageIndexZeroBased() >= totalPages - 1;
    }
}