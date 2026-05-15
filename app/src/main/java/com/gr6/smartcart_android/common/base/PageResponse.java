package com.gr6.smartcart_android.common.base;

import java.util.List;

public class PageResponse<T> {

    private int currentPage;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private List<T> data;

    public PageResponse() {
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public List<T> getData() {
        return data;
    }

    public boolean hasNextPage() {
        return currentPage < totalPages;
    }

    public boolean isEmpty() {
        return data == null || data.isEmpty();
    }
}