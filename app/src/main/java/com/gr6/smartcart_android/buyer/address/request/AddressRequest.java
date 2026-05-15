package com.gr6.smartcart_android.buyer.address.request;

public class AddressRequest {

    private String receiverName;
    private String receiverPhone;
    private String fullAddress;
    private Boolean isDefault;

    public AddressRequest() {
    }

    public AddressRequest(
            String receiverName,
            String receiverPhone,
            String fullAddress,
            Boolean isDefault
    ) {
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.fullAddress = fullAddress;
        this.isDefault = isDefault;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }
}