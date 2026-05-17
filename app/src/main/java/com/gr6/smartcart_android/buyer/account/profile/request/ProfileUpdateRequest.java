package com.gr6.smartcart_android.buyer.account.profile.request;

public class ProfileUpdateRequest {

    private String fullName;
    private String phoneNumber;
    private String avatarUrl;

    public ProfileUpdateRequest() {
    }

    public ProfileUpdateRequest(
            String fullName,
            String phoneNumber,
            String avatarUrl
    ) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}