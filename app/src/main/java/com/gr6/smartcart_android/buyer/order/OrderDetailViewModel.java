package com.gr6.smartcart_android.buyer.order;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.buyer.order.repository.OrderRepository;
import com.gr6.smartcart_android.buyer.order.response.OrderDetailResponse;

public class OrderDetailViewModel extends AndroidViewModel {

    private final OrderRepository repository;
    private final MutableLiveData<OrderDetailState> detailState = new MutableLiveData<>();
    private final MutableLiveData<OrderActionState> completeState = new MutableLiveData<>();

    public OrderDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);
    }

    public LiveData<OrderDetailState> getDetailState() {
        return detailState;
    }

    public LiveData<OrderActionState> getCompleteState() {
        return completeState;
    }

    public void loadOrderDetail(Long shopOrderId) {
        if (shopOrderId == null || shopOrderId <= 0) {
            detailState.setValue(OrderDetailState.error("Không tìm thấy mã đơn hàng"));
            return;
        }

        detailState.setValue(OrderDetailState.loading());

        repository.getOrderDetail(shopOrderId, new OrderRepository.OrderCallback<OrderDetailResponse>() {
            @Override
            public void onSuccess(OrderDetailResponse data, String message) {
                detailState.postValue(OrderDetailState.success(data, message));
            }

            @Override
            public void onError(String message) {
                detailState.postValue(OrderDetailState.error(message));
            }
        });
    }

    public void completeOrder(Long shopOrderId) {
        if (shopOrderId == null || shopOrderId <= 0) {
            completeState.setValue(OrderActionState.error("Không tìm thấy mã đơn hàng"));
            return;
        }

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
}