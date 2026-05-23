package com.gr6.smartcart_android.seller.product.response;

public class CategoryResponse {
    private Long categoryId;
    private String categoryName;
    private String categoryDescription;
    private String categoryImageUrl;
    private String categoryStatus;

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public String getCategoryImageUrl() {
        return categoryImageUrl;
    }

    public String getCategoryStatus() {
        return categoryStatus;
    }

    public boolean isActive() {
        return categoryStatus == null || "ACTIVE".equalsIgnoreCase(categoryStatus);
    }
}


