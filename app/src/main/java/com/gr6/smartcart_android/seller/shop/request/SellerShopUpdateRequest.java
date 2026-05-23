package com.gr6.smartcart_android.seller.shop.request;

public class SellerShopUpdateRequest {
    private String shopName;
    private String pickupAddress;
    private String description;
    private String logoUrl;
    private String coverUrl;

    public SellerShopUpdateRequest(String shopName, String pickupAddress, String description) {
        this(shopName, pickupAddress, description, null, null);
    }

    public SellerShopUpdateRequest(
            String shopName,
            String pickupAddress,
            String description,
            String logoUrl,
            String coverUrl
    ) {
        this.shopName = shopName;
        this.pickupAddress = pickupAddress;
        this.description = description;
        this.logoUrl = logoUrl;
        this.coverUrl = coverUrl;
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

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }
}
