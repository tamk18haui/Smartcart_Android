package com.gr6.smartcart_android.seller.inventory;

public class InventoryActionState {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }

    private final Status status;
    private final String message;

    private InventoryActionState(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static InventoryActionState idle() { return new InventoryActionState(Status.IDLE, null); }
    public static InventoryActionState loading() { return new InventoryActionState(Status.LOADING, null); }
    public static InventoryActionState success(String message) { return new InventoryActionState(Status.SUCCESS, message); }
    public static InventoryActionState error(String message) { return new InventoryActionState(Status.ERROR, message); }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError() { return status == Status.ERROR; }
}
