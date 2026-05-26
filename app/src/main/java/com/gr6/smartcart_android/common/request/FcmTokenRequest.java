package com.gr6.smartcart_android.common.request;

import com.google.gson.annotations.SerializedName;

public class FcmTokenRequest {

    @SerializedName("fcmToken")
    private String fcmToken;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("platform")
    private String platform;

    public FcmTokenRequest(String fcmToken, String deviceId, String platform) {
        this.fcmToken = fcmToken;
        this.deviceId = deviceId;
        this.platform = platform;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getPlatform() {
        return platform;
    }
}
