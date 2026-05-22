package com.gr6.smartcart_android.buyer.account.profile.response;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {

    @SerializedName("userId")
    private Long userId;

    @SerializedName("id")
    private Long id;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("role")
    private String role;

    public Long getUserId() {
        if (userId != null) return userId;
        return id;
    }

    public String getFullName() {
        return fullName == null ? "" : fullName;
    }

    public String getEmail() {
        return email == null ? "" : email;
    }

    public String getPhoneNumber() {
        return phoneNumber == null ? "" : phoneNumber;
    }

    public String getAvatarUrl() {
        return avatarUrl == null ? "" : avatarUrl;
    }

    public String getRole() {
        return role == null ? "" : role;
    }
}