package com.gr6.smartcart_android.seller.order;

import com.gr6.smartcart_android.seller.order.response.OrderListResponse;

import java.util.ArrayList;
import java.util.List;

public class OrderListState {
    public enum Status { LOADING, SUCCESS, ERROR }

    private final Status status;
    private final List<OrderListResponse> orders;
    private final String message;

    private OrderListState(Status status, List<OrderListResponse> orders, String message) {
        this.status = status;
        this.orders = orders == null ? new ArrayList<>() : orders;
        this.message = message;
    }

    public static OrderListState loading() { return new OrderListState(Status.LOADING, new ArrayList<>(), null); }
    public static OrderListState success(List<OrderListResponse> orders, String message) { return new OrderListState(Status.SUCCESS, orders, message); }
    public static OrderListState error(String message) { return new OrderListState(Status.ERROR, new ArrayList<>(), message); }

    public Status getStatus() { return status; }
    public List<OrderListResponse> getOrders() { return orders; }
    public String getMessage() { return message; }
    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError() { return status == Status.ERROR; }
}
