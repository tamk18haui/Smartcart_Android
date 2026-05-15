package com.gr6.smartcart_android.buyer.main.request;

import java.math.BigDecimal;

public class SearchProductRequest {

    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    public SearchProductRequest() {
    }

    public SearchProductRequest(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        this.keyword = keyword;
        this.categoryId = categoryId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public static SearchProductRequest empty() {
        return new SearchProductRequest("", null, null, null);
    }
}