package com.gr6.smartcart_android.seller.model;

import com.google.gson.annotations.SerializedName;

public class SellerShopInfoResponse {

    @SerializedName(value = "shopId", alternate = {"id"})
    private Long shopId;

    @SerializedName(value = "shopName", alternate = {"name"})
    private String shopName;

    @SerializedName("description")
    private String description;

    @SerializedName("pickupAddress")
    private String pickupAddress;

    @SerializedName("status")
    private String status;

    @SerializedName("logoUrl")
    private String logoUrl;

    @SerializedName("coverUrl")
    private String coverUrl;

    @SerializedName("sellerId")
    private Long sellerId;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public SellerShopInfoResponse() {
    }

    public Long getShopId() {
        return shopId;
    }

    public Long getId() {
        return shopId;
    }

    public String getShopName() {
        return shopName == null ? "" : shopName;
    }

    public String getName() {
        return getShopName();
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public String getPickupAddress() {
        return pickupAddress == null ? "" : pickupAddress;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public String getLogoUrl() {
        return logoUrl == null ? "" : logoUrl;
    }

    public String getCoverUrl() {
        return coverUrl == null ? "" : coverUrl;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getCreatedAt() {
        return createdAt == null ? "" : createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt == null ? "" : updatedAt;
    }

    public String getSafeShopName() {
        if (shopName == null || shopName.trim().isEmpty()) {
            return "Cửa hàng SmartCart";
        }

        return shopName.trim();
    }

    public String getSafeStatus() {
        if (status == null || status.trim().isEmpty()) {
            return "PENDING";
        }

        return status.trim();
    }
}