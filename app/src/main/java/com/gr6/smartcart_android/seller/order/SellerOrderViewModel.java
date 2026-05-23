package com.gr6.smartcart_android.seller.order;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.seller.order.repository.SellerOrderRepository;
import com.gr6.smartcart_android.seller.order.response.OrderDetailResponse;
import com.gr6.smartcart_android.seller.order.response.OrderListResponse;

import java.util.ArrayList;
import java.util.List;

public class SellerOrderViewModel extends AndroidViewModel {

    private final SellerOrderRepository repository;

    private final MutableLiveData<OrderListState> orderListState = new MutableLiveData<>();
    private final MutableLiveData<OrderDetailState> orderDetailState = new MutableLiveData<>();
    private final MutableLiveData<OrderActionState> actionState = new MutableLiveData<>();

    public SellerOrderViewModel(@NonNull Application application) {
        super(application);
        repository = new SellerOrderRepository(application);
        actionState.setValue(OrderActionState.idle());
    }

    public LiveData<OrderListState> getOrderListState() {
        return orderListState;
    }

    public LiveData<OrderDetailState> getOrderDetailState() {
        return orderDetailState;
    }

    public LiveData<OrderActionState> getActionState() {
        return actionState;
    }

    public void loadOrders(String keyword) {
        orderListState.setValue(OrderListState.loading());

        repository.loadOrders(keyword, new SellerOrderRepository.OrderCallback<List<OrderListResponse>>() {
            @Override
            public void onSuccess(List<OrderListResponse> data, String message) {
                List<OrderListResponse> safeData = data == null ? new ArrayList<>() : data;
                orderListState.postValue(OrderListState.success(safeData, message));
            }

            @Override
            public void onError(String message) {
                orderListState.postValue(OrderListState.error(message));
            }
        });
    }

    public void loadOrderDetail(Long orderId) {
        if (orderId == null || orderId <= 0) {
            orderDetailState.setValue(OrderDetailState.error("Mã đơn hàng không hợp lệ"));
            return;
        }

        orderDetailState.setValue(OrderDetailState.loading());

        repository.loadOrderDetail(orderId, new SellerOrderRepository.OrderCallback<OrderDetailResponse>() {
            @Override
            public void onSuccess(OrderDetailResponse data, String message) {
                if (data == null) {
                    orderDetailState.postValue(OrderDetailState.error("Không có dữ liệu chi tiết đơn hàng"));
                    return;
                }

                orderDetailState.postValue(OrderDetailState.success(data, message));
            }

            @Override
            public void onError(String message) {
                orderDetailState.postValue(OrderDetailState.error(message));
            }
        });
    }

    public void updateStatus(Long orderId, String status, String reason) {
        if (orderId == null || orderId <= 0) {
            actionState.setValue(OrderActionState.error("Mã đơn hàng không hợp lệ"));
            return;
        }

        if (status == null || status.trim().isEmpty()) {
            actionState.setValue(OrderActionState.error("Trạng thái không hợp lệ"));
            return;
        }

        if ("CANCELLED".equalsIgnoreCase(status)
                && (reason == null || reason.trim().isEmpty())) {
            actionState.setValue(OrderActionState.error("Vui lòng nhập lý do hủy đơn"));
            return;
        }

        actionState.setValue(OrderActionState.loading());

        repository.updateOrderStatus(orderId, status, reason, new SellerOrderRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                actionState.postValue(OrderActionState.success(message));
                loadOrderDetail(orderId);
            }

            @Override
            public void onError(String message) {
                actionState.postValue(OrderActionState.error(message));
            }
        });
    }

    public void confirmOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            actionState.setValue(OrderActionState.error("Mã đơn hàng không hợp lệ"));
            return;
        }

        actionState.setValue(OrderActionState.loading());

        repository.confirmOrderStatus(orderId, new SellerOrderRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                actionState.postValue(OrderActionState.success(message));
                loadOrderDetail(orderId);
            }

            @Override
            public void onError(String message) {
                actionState.postValue(OrderActionState.error(message));
            }
        });
    }

    public void resetActionState() {
        actionState.setValue(OrderActionState.idle());
    }
}