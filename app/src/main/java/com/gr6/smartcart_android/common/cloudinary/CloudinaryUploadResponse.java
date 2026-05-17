package com.gr6.smartcart_android.common.cloudinary;

import com.google.gson.annotations.SerializedName;

public class CloudinaryUploadResponse {

    @SerializedName("secure_url")
    private String secureUrl;

    @SerializedName("url")
    private String url;

    @SerializedName("public_id")
    private String publicId;

    public String getSecureUrl() {
        if (secureUrl != null && !secureUrl.trim().isEmpty()) {
            return secureUrl;
        }

        return url;
    }

    public String getPublicId() {
        return publicId;
    }
}