package com.gr6.smartcart_android.seller.product.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductResponse {
    private Long productId;
    private String name;
    private String description;
    private String brand;
    private String condition;
    private BigDecimal basePrice;
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Long categoryId;
    private Long shopId;
    private String status;
    private List<String> images;
    private List<VariantResponse> variants;
    private Double averageRating;
    private Integer reviewCount;
    private Integer soldQuantity;
    private Long totalRevenue;

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return safe(name);
    }

    public String getDescription() {
        return description;
    }

    public String getBrand() {
        return brand;
    }

    public String getCondition() {
        return condition;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public BigDecimal getLength() {
        return length;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getImages() {
        return images == null ? new ArrayList<>() : images;
    }

    public List<VariantResponse> getVariants() {
        return variants == null ? new ArrayList<>() : variants;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount == null ? 0 : reviewCount;
    }

    public Integer getSoldQuantity() {
        return soldQuantity == null ? 0 : soldQuantity;
    }

    public Long getTotalRevenue() {
        return totalRevenue == null ? 0L : totalRevenue;
    }

    public String getFirstImage() {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.get(0);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}




