package com.gr6.smartcart_android.auth.login;

import com.gr6.smartcart_android.auth.response.LoginResponse;

public class LoginState {

    private final boolean loading;
    private final boolean success;
    private final String message;
    private final LoginResponse data;

    private LoginState(boolean loading, boolean success, String message, LoginResponse data) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static LoginState loading() {
        return new LoginState(true, false, null, null);
    }

    public static LoginState success(LoginResponse data, String message) {
        return new LoginState(false, true, message, data);
    }

    public static LoginState error(String message) {
        return new LoginState(false, false, message, null);
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

    public LoginResponse getData() {
        return data;
    }
}