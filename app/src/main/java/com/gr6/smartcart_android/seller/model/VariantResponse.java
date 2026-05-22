package com.gr6.smartcart_android.seller.model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class VariantResponse {
    private Long variantId;
    private Long productId;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Map<String, String> attributes;

    public Long getVariantId() {
        return variantId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStockQuantity() {
        return stockQuantity == null ? 0 : stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity == null ? 0 : stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Map<String, String> getAttributes() {
        return attributes == null ? new LinkedHashMap<>() : attributes;
    }

    public String getAttributeText() {
        Map<String, String> attrs = getAttributes();
        if (!attrs.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                if (builder.length() > 0) {
                    builder.append("   ");
                }
                builder.append(entry.getKey()).append(": ").append(entry.getValue());
            }
            return builder.toString();
        }

        if (sku != null && !sku.trim().isEmpty()) {
            return "SKU: " + sku;
        }

        return "Phân loại mặc định";
    }
}
