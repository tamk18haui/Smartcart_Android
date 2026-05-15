package com.gr6.smartcart_android.buyer.main.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ProductPageResponse {

    @SerializedName("content")
    private List<HomeProductResponse> content;

    @SerializedName("number")
    private Integer number;

    @SerializedName("size")
    private Integer size;

    @SerializedName("totalPages")
    private Integer totalPages;

    @SerializedName("totalElements")
    private Long totalElements;

    @SerializedName("last")
    private Boolean last;

    public List<HomeProductResponse> getContent() {
        if (content == null) return new ArrayList<>();
        return content;
    }

    public int getNumber() {
        return number == null ? 0 : number;
    }

    public int getSize() {
        return size == null ? 0 : size;
    }

    public int getTotalPages() {
        return totalPages == null ? 0 : totalPages;
    }

    public long getTotalElements() {
        return totalElements == null ? 0L : totalElements;
    }

    public boolean isLast() {
        return Boolean.TRUE.equals(last);
    }
}