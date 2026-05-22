package com.gr6.smartcart_android.seller.model;

import java.math.BigDecimal;
import java.util.List;

public class ProductRequest {
    private Long categoryId;
    private String name;
    private String description;
    private String brand;
    private String condition;
    private BigDecimal basePrice;
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Integer stockQuantity;
    private List<String> uploadImages;
    private List<ProductVariantRequest> variants;

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setUploadImages(List<String> uploadImages) {
        this.uploadImages = uploadImages;
    }

    public void setVariants(List<ProductVariantRequest> variants) {
        this.variants = variants;
    }
}


