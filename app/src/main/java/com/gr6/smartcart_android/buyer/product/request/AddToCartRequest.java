package com.gr6.smartcart_android.buyer.product.request;

public class AddToCartRequest {

    private Long variantId;
    private Integer quantity;

    public AddToCartRequest() {
    }

    public AddToCartRequest(Long variantId, Integer quantity) {
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