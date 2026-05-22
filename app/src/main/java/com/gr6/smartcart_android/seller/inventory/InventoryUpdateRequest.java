package com.gr6.smartcart_android.seller.inventory;

import com.google.gson.annotations.SerializedName;

public class InventoryUpdateRequest {
    @SerializedName("variantId")
    private Long variantId;

    @SerializedName("quantity")
    private Integer quantity;

    public InventoryUpdateRequest(Long variantId, Integer quantity) {
        this.variantId = variantId;
        this.quantity = quantity;
    }
}
