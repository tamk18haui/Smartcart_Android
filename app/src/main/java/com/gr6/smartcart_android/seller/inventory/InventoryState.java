package com.gr6.smartcart_android.seller.inventory;

import com.gr6.smartcart_android.seller.inventory.response.InventoryVariantItem;

import java.util.ArrayList;
import java.util.List;

public class InventoryState {
    public enum Status { LOADING, SUCCESS, ERROR }

    private final Status status;
    private final List<InventoryVariantItem> items;
    private final String message;

    private InventoryState(Status status, List<InventoryVariantItem> items, String message) {
        this.status = status;
        this.items = items == null ? new ArrayList<>() : items;
        this.message = message;
    }

    public static InventoryState loading() { return new InventoryState(Status.LOADING, new ArrayList<>(), null); }
    public static InventoryState success(List<InventoryVariantItem> items, String message) { return new InventoryState(Status.SUCCESS, items, message); }
    public static InventoryState error(String message) { return new InventoryState(Status.ERROR, new ArrayList<>(), message); }

    public Status getStatus() { return status; }
    public List<InventoryVariantItem> getItems() { return items; }
    public String getMessage() { return message; }
    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError() { return status == Status.ERROR; }
}
