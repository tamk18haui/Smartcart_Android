package com.gr6.smartcart_android.seller.inventory.model;

import com.gr6.smartcart_android.seller.model.ProductResponse;
import com.gr6.smartcart_android.seller.model.VariantResponse;

public class InventoryVariantItem {
    private final ProductResponse product;
    private final VariantResponse variant;

    public InventoryVariantItem(ProductResponse product, VariantResponse variant) {
        this.product = product;
        this.variant = variant;
    }

    public ProductResponse getProduct() {
        return product;
    }

    public VariantResponse getVariant() {
        return variant;
    }

    public String getDisplayImage() {
        if (variant != null && variant.getImageUrl() != null && !variant.getImageUrl().trim().isEmpty()) {
            return variant.getImageUrl();
        }
        return product == null ? null : product.getFirstImage();
    }

    public String getProductName() {
        return product == null ? "" : product.getName();
    }

    public int getStock() {
        return variant == null ? 0 : variant.getStockQuantity();
    }
}
