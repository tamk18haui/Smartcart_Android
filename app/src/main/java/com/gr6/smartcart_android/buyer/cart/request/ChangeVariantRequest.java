package com.gr6.smartcart_android.buyer.cart.request;

public class ChangeVariantRequest {

    private Long cartItemId;
    private Long newVariantId;

    public ChangeVariantRequest(Long cartItemId, Long newVariantId) {
        this.cartItemId = cartItemId;
        this.newVariantId = newVariantId;
    }

    public Long getCartItemId() {
        return cartItemId;
    }

    public Long getNewVariantId() {
        return newVariantId;
    }
}