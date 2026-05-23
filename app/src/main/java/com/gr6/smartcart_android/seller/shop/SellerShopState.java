package com.gr6.smartcart_android.seller.shop;

import com.gr6.smartcart_android.seller.shop.response.SellerShopInfoResponse;

public class SellerShopState {
    public enum Status { LOADING, SUCCESS, ERROR }

    private final Status status;
    private final SellerShopInfoResponse shop;
    private final String message;

    private SellerShopState(Status status, SellerShopInfoResponse shop, String message) {
        this.status = status;
        this.shop = shop;
        this.message = message;
    }

    public static SellerShopState loading() { return new SellerShopState(Status.LOADING, null, null); }
    public static SellerShopState success(SellerShopInfoResponse shop, String message) { return new SellerShopState(Status.SUCCESS, shop, message); }
    public static SellerShopState error(String message) { return new SellerShopState(Status.ERROR, null, message); }

    public Status getStatus() { return status; }
    public SellerShopInfoResponse getShop() { return shop; }
    public String getMessage() { return message; }
    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError() { return status == Status.ERROR; }
}
