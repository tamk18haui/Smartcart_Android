package com.gr6.smartcart_android.seller.voucher;

public class VoucherActionState {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }

    private final Status status;
    private final String message;

    private VoucherActionState(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static VoucherActionState idle() { return new VoucherActionState(Status.IDLE, null); }
    public static VoucherActionState loading() { return new VoucherActionState(Status.LOADING, null); }
    public static VoucherActionState success(String message) { return new VoucherActionState(Status.SUCCESS, message); }
    public static VoucherActionState error(String message) { return new VoucherActionState(Status.ERROR, message); }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError() { return status == Status.ERROR; }
}
