package com.gr6.smartcart_android.common.base;

public class BaseResponse<T> {

    private int status;
    private String message;
    private T data;

    public BaseResponse() {
    }

    public BaseResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }

    public String getSafeMessage() {
        if (message == null || message.trim().isEmpty()) {
            return isSuccess() ? "Thành công" : "Có lỗi xảy ra";
        }
        return message;
    }
}