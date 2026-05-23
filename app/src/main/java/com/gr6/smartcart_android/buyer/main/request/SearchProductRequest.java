package com.gr6.smartcart_android.buyer.main.request;

import java.math.BigDecimal;

public class SearchProductRequest {

    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy;

    public SearchProductRequest() {
    }

    public SearchProductRequest(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy
    ) {
        this.keyword = keyword == null ? "" : keyword.trim();
        this.categoryId = categoryId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.sortBy = sortBy == null ? "relevance" : sortBy;
    }

    public static SearchProductRequest empty() {
        return new SearchProductRequest(
                "",
                null,
                null,
                null,
                "newest"
        );
    }

    public static SearchProductRequest ofKeywordAndCategory(
            String keyword,
            Long categoryId
    ) {
        return new SearchProductRequest(
                keyword == null ? "" : keyword.trim(),
                categoryId,
                null,
                null,
                "newest"
        );
    }

    public static SearchProductRequest forSearchScreen(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy
    ) {
        return new SearchProductRequest(
                keyword,
                categoryId,
                minPrice,
                maxPrice,
                sortBy
        );
    }

    public String getKeyword() {
        return keyword;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public String getSortBy() {
        return sortBy;
    }
}