package com.gr6.smartcart_android.seller.order;

public class OrderActionState {

    public enum Status {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final String message;

    private OrderActionState(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static OrderActionState idle() {
        return new OrderActionState(Status.IDLE, null);
    }

    public static OrderActionState loading() {
        return new OrderActionState(Status.LOADING, null);
    }

    public static OrderActionState success(String message) {
        return new OrderActionState(Status.SUCCESS, message);
    }

    public static OrderActionState error(String message) {
        return new OrderActionState(Status.ERROR, message);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        if (message == null || message.trim().isEmpty()) {
            if (isSuccess()) return "Thao tác thành công";
            if (isError()) return "Có lỗi xảy ra";
            return "";
        }
        return message;
    }

    public boolean isIdle() {
        return status == Status.IDLE;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}