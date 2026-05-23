package com.gr6.smartcart_android.seller.inventory.request;

public class InventoryUpdateRequest {
    private Long variantId;
    private Integer quantity;

    public InventoryUpdateRequest(Long variantId, Integer quantity) {
        this.variantId = variantId;
        this.quantity = quantity;
    }

    public Long getVariantId() {
        return variantId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}


