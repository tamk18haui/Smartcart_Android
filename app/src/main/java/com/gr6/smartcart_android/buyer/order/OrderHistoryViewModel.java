package com.gr6.smartcart_android.buyer.order;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.buyer.order.repository.OrderRepository;
import com.gr6.smartcart_android.buyer.order.response.OrderHistoryResponse;

import java.util.List;

public class OrderHistoryViewModel extends AndroidViewModel {

    private final MutableLiveData<OrderActionState> cancelState = new MutableLiveData<>();

    private final OrderRepository repository;
    private final MutableLiveData<OrderHistoryState> orderHistoryState = new MutableLiveData<>();

    public LiveData<OrderActionState> getCancelState() {
        return cancelState;
    }

    public OrderHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);
    }

    public LiveData<OrderHistoryState> getOrderHistoryState() {
        return orderHistoryState;
    }

    public void cancelOrder(Long shopOrderId, String reason) {
        cancelState.setValue(OrderActionState.loading());

        repository.cancelOrder(shopOrderId, reason, new OrderRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                cancelState.postValue(OrderActionState.success(message));
            }

            @Override
            public void onError(String message) {
                cancelState.postValue(OrderActionState.error(message));
            }
        });
    }

    public void loadOrderHistory() {
        orderHistoryState.setValue(OrderHistoryState.loading());

        repository.getOrderHistory(new OrderRepository.OrderHistoryCallback() {
            @Override
            public void onSuccess(List<OrderHistoryResponse> data, String message) {
                orderHistoryState.postValue(OrderHistoryState.success(data, message));
            }

            @Override
            public void onError(String message) {
                orderHistoryState.postValue(OrderHistoryState.error(message));
            }
        });
    }
}