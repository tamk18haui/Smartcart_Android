package com.gr6.smartcart_android.buyer.order;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.buyer.checkout.response.CheckoutOrderResponse;
import com.gr6.smartcart_android.buyer.order.repository.OrderRepository;
import com.gr6.smartcart_android.buyer.order.response.OrderHistoryResponse;

import java.util.List;

public class OrderHistoryViewModel extends AndroidViewModel {

    private final OrderRepository repository;

    private final MutableLiveData<OrderHistoryState> orderHistoryState = new MutableLiveData<>();
    private final MutableLiveData<OrderActionState> cancelState = new MutableLiveData<>();
    private final MutableLiveData<OrderActionState> completeState = new MutableLiveData<>();
    private final MutableLiveData<CheckoutOrderResponse> retryPaymentState = new MutableLiveData<>();
    private final MutableLiveData<String> retryPaymentErrorState = new MutableLiveData<>();

    public OrderHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);
    }

    public LiveData<OrderHistoryState> getOrderHistoryState() {
        return orderHistoryState;
    }

    public LiveData<OrderActionState> getCancelState() {
        return cancelState;
    }

    public LiveData<OrderActionState> getCompleteState() {
        return completeState;
    }

    public LiveData<CheckoutOrderResponse> getRetryPaymentState() {
        return retryPaymentState;
    }

    public LiveData<String> getRetryPaymentErrorState() {
        return retryPaymentErrorState;
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

    public void completeOrder(Long shopOrderId) {
        completeState.setValue(OrderActionState.loading());

        repository.completeOrder(shopOrderId, new OrderRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                completeState.postValue(OrderActionState.success(message));
            }

            @Override
            public void onError(String message) {
                completeState.postValue(OrderActionState.error(message));
            }
        });
    }

    public void retryPayment(Long shopOrderId) {
        repository.retryPayment(shopOrderId, new OrderRepository.OrderCallback<CheckoutOrderResponse>() {
            @Override
            public void onSuccess(CheckoutOrderResponse data, String message) {
                retryPaymentState.postValue(data);
            }

            @Override
            public void onError(String message) {
                retryPaymentErrorState.postValue(message);
            }
        });
    }
}