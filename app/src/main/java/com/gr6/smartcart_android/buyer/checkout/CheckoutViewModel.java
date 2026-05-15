package com.gr6.smartcart_android.buyer.checkout;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.buyer.checkout.repository.CheckoutRepository;
import com.gr6.smartcart_android.buyer.checkout.request.CheckoutPreviewRequest;
import com.gr6.smartcart_android.buyer.checkout.request.CreateOrderRequest;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutOrderResponse;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutPreviewResponse;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutVoucherResponse;

import java.util.List;

public class CheckoutViewModel extends AndroidViewModel {

    private final CheckoutRepository repository;

    private final MutableLiveData<CheckoutState<CheckoutPreviewResponse>> previewState = new MutableLiveData<>();
    private final MutableLiveData<CheckoutState<CheckoutOrderResponse>> orderState = new MutableLiveData<>();
    private final MutableLiveData<CheckoutState<List<CheckoutVoucherResponse>>> voucherState = new MutableLiveData<>();

    public CheckoutViewModel(@NonNull Application application) {
        super(application);
        repository = new CheckoutRepository(application);
    }

    public LiveData<CheckoutState<CheckoutPreviewResponse>> getPreviewState() {
        return previewState;
    }

    public LiveData<CheckoutState<CheckoutOrderResponse>> getOrderState() {
        return orderState;
    }

    public LiveData<CheckoutState<List<CheckoutVoucherResponse>>> getVoucherState() {
        return voucherState;
    }

    public void loadPreview(CheckoutPreviewRequest request) {
        previewState.setValue(CheckoutState.loading());

        repository.getPreview(request, new CheckoutRepository.CheckoutCallback<CheckoutPreviewResponse>() {
            @Override
            public void onSuccess(CheckoutPreviewResponse data, String message) {
                previewState.postValue(CheckoutState.success(data, message));
            }

            @Override
            public void onError(String message) {
                previewState.postValue(CheckoutState.error(message));
            }
        });
    }

    public void createOrder(CreateOrderRequest request) {
        orderState.setValue(CheckoutState.loading());

        repository.createOrder(request, new CheckoutRepository.CheckoutCallback<CheckoutOrderResponse>() {
            @Override
            public void onSuccess(CheckoutOrderResponse data, String message) {
                orderState.postValue(CheckoutState.success(data, message));
            }

            @Override
            public void onError(String message) {
                orderState.postValue(CheckoutState.error(message));
            }
        });
    }

    public void loadShopVouchers(Long shopId) {
        voucherState.setValue(CheckoutState.loading());

        repository.getShopVouchers(shopId, new CheckoutRepository.CheckoutCallback<List<CheckoutVoucherResponse>>() {
            @Override
            public void onSuccess(List<CheckoutVoucherResponse> data, String message) {
                voucherState.postValue(CheckoutState.success(data, message));
            }

            @Override
            public void onError(String message) {
                voucherState.postValue(CheckoutState.error(message));
            }
        });
    }
}