package com.gr6.smartcart_android.buyer.main.response;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class HomeProductResponse {

    @SerializedName("productId")
    private Long productId;

    @SerializedName("id")
    private Long id;

    @SerializedName("categoryId")
    private Long categoryId;

    @SerializedName("categoryName")
    private String categoryName;

    @SerializedName("shopId")
    private Long shopId;

    @SerializedName("shopName")
    private String shopName;

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

    @SerializedName("maxPrice")
    private BigDecimal maxPrice;

    @SerializedName("salePrice")
    private BigDecimal salePrice;

    @SerializedName("originalPrice")
    private BigDecimal originalPrice;

    @SerializedName("soldQuantity")
    private Integer soldQuantity;

    @SerializedName("totalSold")
    private Integer totalSold;

    @SerializedName("averageRating")
    private Double averageRating;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("reviewCount")
    private Integer reviewCount;

    @SerializedName("location")
    private String location;

    public Long getProductId() {
        if (productId != null) return productId;
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        if (categoryName == null || categoryName.trim().isEmpty()) return "Danh mục";
        return categoryName;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopName() {
        if (shopName == null || shopName.trim().isEmpty()) return "SmartCart Shop";
        return shopName;
    }

    public String getProductName() {
        if (productName != null && !productName.trim().isEmpty()) return productName;
        if (name != null && !name.trim().isEmpty()) return name;
        return "Sản phẩm SmartCart";
    }

    public String getDescription() {
        if (productDescription != null && !productDescription.trim().isEmpty()) {
            return productDescription;
        }

        if (description != null && !description.trim().isEmpty()) {
            return description;
        }

        return "";
    }

    public String getImageUrl() {
        if (thumbnailUrl != null && !thumbnailUrl.trim().isEmpty()) return thumbnailUrl.trim();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) return firstImage(imageUrl);
        if (productImageUrl != null && !productImageUrl.trim().isEmpty()) return firstImage(productImageUrl);
        return "";
    }

    public BigDecimal getDisplayPrice() {
        if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0) return salePrice;
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) > 0) return minPrice;
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) return price;
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) return originalPrice;
        return BigDecimal.ZERO;
    }

    public BigDecimal getMinPrice() {
        if (minPrice != null) return minPrice;
        return getDisplayPrice();
    }

    public BigDecimal getMaxPrice() {
        if (maxPrice != null) return maxPrice;
        return getDisplayPrice();
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public Integer getSoldQuantity() {
        if (soldQuantity != null) return Math.max(soldQuantity, 0);
        if (totalSold != null) return Math.max(totalSold, 0);
        return 0;
    }

    public Double getAverageRating() {
        if (averageRating != null) return averageRating;
        if (rating != null) return rating;
        return 0.0;
    }

    public Integer getReviewCount() {
        return reviewCount == null ? 0 : reviewCount;
    }

    public String getLocation() {
        if (location == null || location.trim().isEmpty()) return "";
        return location.trim();
    }

    private String firstImage(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";

        String[] parts = raw.split(",");

        for (String part : parts) {
            if (part != null && !part.trim().isEmpty()) {
                return part.trim();
            }
        }

        return "";
    }
}