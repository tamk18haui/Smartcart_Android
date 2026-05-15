package com.gr6.smartcart_android.buyer.main.response;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class HomeProductResponse {

    @SerializedName("productId")
    private Long productId;

    @SerializedName("id")
    private Long id;

    @SerializedName("productName")
    private String productName;

    @SerializedName("name")
    private String name;

    @SerializedName("productDescription")
    private String productDescription;

    @SerializedName("description")
    private String description;

    @SerializedName("thumbnailUrl")
    private String thumbnailUrl;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("productImageUrl")
    private String productImageUrl;

    @SerializedName("price")
    private BigDecimal price;

    @SerializedName("minPrice")
    private BigDecimal minPrice;

    @SerializedName("salePrice")
    private BigDecimal salePrice;

    @SerializedName("originalPrice")
    private BigDecimal originalPrice;

    @SerializedName("soldQuantity")
    private Integer soldQuantity;

    @SerializedName("sold")
    private Integer sold;

    @SerializedName("averageRating")
    private Double averageRating;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("shopId")
    private Long shopId;

    @SerializedName("shopName")
    private String shopName;

    @SerializedName("categoryId")
    private Long categoryId;

    public Long getProductId() {
        if (productId != null) return productId;
        return id;
    }

    public String getProductName() {
        if (productName != null && !productName.trim().isEmpty()) return productName;
        if (name != null && !name.trim().isEmpty()) return name;
        return "Sản phẩm";
    }

    public String getProductDescription() {
        if (productDescription != null && !productDescription.trim().isEmpty()) {
            return productDescription;
        }
        return description;
    }

    public String getImageUrl() {
        if (thumbnailUrl != null && !thumbnailUrl.trim().isEmpty()) return thumbnailUrl;
        if (imageUrl != null && !imageUrl.trim().isEmpty()) return imageUrl;
        return productImageUrl;
    }

    public BigDecimal getDisplayPrice() {
        if (salePrice != null) return salePrice;
        if (minPrice != null) return minPrice;
        if (price != null) return price;
        return BigDecimal.ZERO;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public Integer getSoldQuantity() {
        if (soldQuantity != null) return soldQuantity;
        if (sold != null) return sold;
        return 0;
    }

    public Double getAverageRating() {
        if (averageRating != null) return averageRating;
        if (rating != null) return rating;
        return 0.0;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopName() {
        if (shopName == null || shopName.trim().isEmpty()) return "SmartCart Shop";
        return shopName;
    }

    public Long getCategoryId() {
        return categoryId;
    }
}