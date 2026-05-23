package com.gr6.smartcart_android.seller.order;

import com.gr6.smartcart_android.seller.order.response.OrderDetailResponse;

public class OrderDetailState {
    public enum Status { LOADING, SUCCESS, ERROR }

    private final Status status;
    private final OrderDetailResponse order;
    private final String message;

    private OrderDetailState(Status status, OrderDetailResponse order, String message) {
        this.status = status;
        this.order = order;
        this.message = message;
    }

    public static OrderDetailState loading() { return new OrderDetailState(Status.LOADING, null, null); }
    public static OrderDetailState success(OrderDetailResponse order, String message) { return new OrderDetailState(Status.SUCCESS, order, message); }
    public static OrderDetailState error(String message) { return new OrderDetailState(Status.ERROR, null, message); }

    public Status getStatus() { return status; }
    public OrderDetailResponse getOrder() { return order; }
    public String getMessage() { return message; }
    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError() { return status == Status.ERROR; }
}
