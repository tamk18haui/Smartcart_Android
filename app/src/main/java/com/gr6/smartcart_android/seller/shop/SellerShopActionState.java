package com.gr6.smartcart_android.seller.shop;

public class SellerShopActionState {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }

    private final Status status;
    private final String message;

    private SellerShopActionState(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static SellerShopActionState idle() { return new SellerShopActionState(Status.IDLE, null); }
    public static SellerShopActionState loading() { return new SellerShopActionState(Status.LOADING, null); }
    public static SellerShopActionState success(String message) { return new SellerShopActionState(Status.SUCCESS, message); }
    public static SellerShopActionState error(String message) { return new SellerShopActionState(Status.ERROR, message); }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError() { return status == Status.ERROR; }
}
