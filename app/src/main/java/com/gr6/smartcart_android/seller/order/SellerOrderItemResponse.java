package com.gr6.smartcart_android.seller.order;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class SellerOrderItemResponse {
    @SerializedName("variantId")
    private Long variantId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("variantName")
    private String variantName;

    @SerializedName("productImageUrl")
    private String productImageUrl;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("price")
    private BigDecimal price;

    public Long getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public String getVariantName() { return variantName; }
    public String getProductImageUrl() { return productImageUrl; }
    public Integer getQuantity() { return quantity == null ? 0 : quantity; }
    public BigDecimal getPrice() { return price == null ? BigDecimal.ZERO : price; }
}
