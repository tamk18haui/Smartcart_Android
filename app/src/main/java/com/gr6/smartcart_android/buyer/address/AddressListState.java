package com.gr6.smartcart_android.buyer.address;

import com.gr6.smartcart_android.buyer.address.response.AddressResponse;

import java.util.List;

public class AddressListState {

    private final boolean loading;
    private final boolean success;
    private final String message;
    private final List<AddressResponse> data;

    private AddressListState(
            boolean loading,
            boolean success,
            String message,
            List<AddressResponse> data
    ) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static AddressListState loading() {
        return new AddressListState(true, false, null, null);
    }

    public static AddressListState success(List<AddressResponse> data) {
        return new AddressListState(false, true, null, data);
    }

    public static AddressListState error(String message) {
        return new AddressListState(false, false, message, null);
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

    public List<AddressResponse> getData() {
        return data;
    }
}