package com.gr6.smartcart_android.seller.model;

public class SellerShopUpdateRequest {
    private String shopName;
    private String pickupAddress;
    private String description;

    public SellerShopUpdateRequest(String shopName, String pickupAddress, String description) {
        this.shopName = shopName;
        this.pickupAddress = pickupAddress;
        this.description = description;
    }

    public String getShopName() {
        return shopName;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public String getDescription() {
        return description;
    }
}
