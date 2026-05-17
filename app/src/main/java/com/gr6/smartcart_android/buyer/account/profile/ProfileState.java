package com.gr6.smartcart_android.buyer.account.profile;

import com.gr6.smartcart_android.buyer.account.profile.response.ProfileResponse;

public class ProfileState {

    private final boolean loading;
    private final boolean success;
    private final String message;
    private final ProfileResponse data;

    private ProfileState(
            boolean loading,
            boolean success,
            String message,
            ProfileResponse data
    ) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ProfileState loading() {
        return new ProfileState(true, false, null, null);
    }

    public static ProfileState success(
            ProfileResponse data,
            String message
    ) {
        return new ProfileState(false, true, message, data);
    }

    public static ProfileState error(String message) {
        return new ProfileState(false, false, message, null);
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ProfileResponse getData() {
        return data;
    }
}