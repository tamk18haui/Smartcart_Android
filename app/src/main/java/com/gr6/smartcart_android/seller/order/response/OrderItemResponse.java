package com.gr6.smartcart_android.seller.order.response;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class OrderItemResponse {

    @SerializedName(value = "productName", alternate = {"name"})
    private String productName;

    @SerializedName(value = "variantName", alternate = {"sku", "variantSku"})
    private String variantName;

    private int quantity;

    @SerializedName(value = "price", alternate = {"unitPrice", "priceAtPurchase"})
    private BigDecimal price;

    @SerializedName(
            value = "imageUrl",
            alternate = {
                    "variantImageUrl",
                    "productImageUrl",
                    "productImage",
                    "firstProductImage",
                    "thumbnailUrl",
                    "image"
            }
    )
    private String imageUrl;

    public String getProductName() {
        return productName == null || productName.trim().isEmpty()
                ? "Sản phẩm"
                : productName;
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