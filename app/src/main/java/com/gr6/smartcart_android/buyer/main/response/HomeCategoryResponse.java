package com.gr6.smartcart_android.buyer.main.response;

import com.google.gson.annotations.SerializedName;

public class HomeCategoryResponse {

    @SerializedName("categoryId")
    private Long categoryId;

    @SerializedName("id")
    private Long id;

    @SerializedName("categoryName")
    private String categoryName;

    @SerializedName("name")
    private String name;

    @SerializedName("categoryDescription")
    private String categoryDescription;

    @SerializedName("description")
    private String description;

    @SerializedName("categoryImageUrl")
    private String categoryImageUrl;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("categoryStatus")
    private String categoryStatus;

    public Long getCategoryId() {
        if (categoryId != null) return categoryId;
        return id;
    }

    public String getCategoryName() {
        if (categoryName != null && !categoryName.trim().isEmpty()) return categoryName;
        if (name != null && !name.trim().isEmpty()) return name;
        return "Danh mục";
    }

    public String getCategoryDescription() {
        if (categoryDescription != null && !categoryDescription.trim().isEmpty()) {
            return categoryDescription;
        }
        return description;
    }

    public String getCategoryImageUrl() {
        if (categoryImageUrl != null && !categoryImageUrl.trim().isEmpty()) return categoryImageUrl;
        return imageUrl;
    }

    public String getCategoryStatus() {
        return categoryStatus;
    }
}