package com.gr6.smartcart_android.seller.product.request;

import java.math.BigDecimal;
import java.util.Map;

public class ProductVariantRequest {
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Map<String, String> attributes;

    public ProductVariantRequest() {
    }

    public ProductVariantRequest(
            String sku,
            BigDecimal price,
            Integer stockQuantity,
            String imageUrl,
            Map<String, String> attributes
    ) {
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.attributes = attributes;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}




