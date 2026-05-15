package com.gr6.smartcart_android.buyer.cart.request;

public class CartItemRequest {

    private Long variantId;
    private Integer quantity;

    public CartItemRequest(Long variantId, Integer quantity) {
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