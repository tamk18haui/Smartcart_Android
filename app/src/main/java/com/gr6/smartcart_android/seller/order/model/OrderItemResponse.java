package com.gr6.smartcart_android.seller.order.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class OrderItemResponse {
    @SerializedName(value = "productName", alternate = {"name"})
    private String productName;
    @SerializedName(value = "variantName", alternate = {"variantAttributes", "attributes"})
    private String variantName;
    private int quantity;
    @SerializedName(value = "price", alternate = {"unitPrice"})
    private BigDecimal price;
    @SerializedName(value = "imageUrl", alternate = {"productImage", "firstProductImage"})
    private String imageUrl;

    public String getProductName() {
        return productName == null ? "Sản phẩm" : productName;
    }

    public String getVariantName() {
        return variantName == null ? "" : variantName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
