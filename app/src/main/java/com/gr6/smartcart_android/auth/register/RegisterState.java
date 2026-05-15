package com.gr6.smartcart_android.auth.register;

public class RegisterState {

    private final boolean loading;
    private final boolean success;
    private final String message;

    private RegisterState(boolean loading, boolean success, String message) {
        this.loading = loading;
        this.success = success;
        this.message = message;
    }

    public static RegisterState loading() {
        return new RegisterState(true, false, null);
    }

    public static RegisterState success(String message) {
        return new RegisterState(false, true, message);
    }

    public static RegisterState error(String message) {
        return new RegisterState(false, false, message);
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
}