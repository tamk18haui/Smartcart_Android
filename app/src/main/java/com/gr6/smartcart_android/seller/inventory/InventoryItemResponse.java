package com.gr6.smartcart_android.seller.inventory;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class InventoryItemResponse {
    @SerializedName("productId")
    private Long productId;

    @SerializedName("variantId")
    private Long variantId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("sku")
    private String sku;

    @SerializedName("variantName")
    private String variantName;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("price")
    private BigDecimal price;

    @SerializedName("stockQuantity")
    private Integer stockQuantity;

    @SerializedName("lowStock")
    private Boolean lowStock;

    public Long getProductId() {
        return productId;
    }

    public Long getVariantId() {
        return variantId;
    }

    public String getProductName() {
        return productName;
    }

    public String getSku() {
        return sku;
    }

    public String getVariantName() {
        return variantName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStockQuantity() {
        return stockQuantity == null ? 0 : stockQuantity;
    }

    public boolean isLowStock() {
        return Boolean.TRUE.equals(lowStock);
    }
}
