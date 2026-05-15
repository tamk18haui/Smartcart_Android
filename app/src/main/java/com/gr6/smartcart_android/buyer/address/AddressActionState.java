package com.gr6.smartcart_android.buyer.address;

public class AddressActionState {

    private final boolean loading;
    private final boolean success;
    private final String message;

    private AddressActionState(
            boolean loading,
            boolean success,
            String message
    ) {
        this.loading = loading;
        this.success = success;
        this.message = message;
    }

    public static AddressActionState loading() {
        return new AddressActionState(true, false, null);
    }

    public static AddressActionState success(String message) {
        if (message == null || message.trim().isEmpty()) {
            message = "Thao tác thành công";
        }

        return new AddressActionState(false, true, message);
    }

    public static AddressActionState error(String message) {
        if (message == null || message.trim().isEmpty()) {
            message = "Thao tác thất bại";
        }

        return new AddressActionState(false, false, message);
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