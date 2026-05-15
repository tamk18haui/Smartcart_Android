package com.gr6.smartcart_android.buyer.address.response;

import com.google.gson.annotations.SerializedName;

public class AddressResponse {

    @SerializedName("addressId")
    private Long addressId;

    @SerializedName("id")
    private Long id;

    @SerializedName("receiverName")
    private String receiverName;

    @SerializedName("receiverPhone")
    private String receiverPhone;

    @SerializedName("phone")
    private String phone;

    @SerializedName("fullAddress")
    private String fullAddress;

    @SerializedName("address")
    private String address;

    @SerializedName("isDefault")
    private Boolean isDefault;

    @SerializedName("default")
    private Boolean defaultAddress;

    public Long getAddressId() {
        if (addressId != null) return addressId;
        return id;
    }

    public String getReceiverName() {
        if (receiverName == null || receiverName.trim().isEmpty()) {
            return "Người nhận";
        }
        return receiverName;
    }

    public String getReceiverPhone() {
        if (receiverPhone != null && !receiverPhone.trim().isEmpty()) return receiverPhone;
        if (phone != null && !phone.trim().isEmpty()) return phone;
        return "";
    }

    public String getFullAddress() {
        if (fullAddress != null && !fullAddress.trim().isEmpty()) return fullAddress;
        if (address != null && !address.trim().isEmpty()) return address;
        return "";
    }

    public Boolean getIsDefault() {
        if (isDefault != null) return isDefault;
        if (defaultAddress != null) return defaultAddress;
        return false;
    }

    public boolean isDefaultAddress() {
        return Boolean.TRUE.equals(getIsDefault());
    }
}