package com.gr6.smartcart_android.auth.request;

public class ShopRegisterRequest {

    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;
    private String confirmPassword;

    private String shopName;
    private String description;
    private String pickupAddress;

    public ShopRegisterRequest() {
    }

    public ShopRegisterRequest(
            String fullName,
            String email,
            String phoneNumber,
            String password,
            String confirmPassword,
            String shopName,
            String description,
            String pickupAddress
    ) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.shopName = shopName;
        this.description = description;
        this.pickupAddress = pickupAddress;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public String getShopName() {
        return shopName;
    }

    public String getDescription() {
        return description;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }
}