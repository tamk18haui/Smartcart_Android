package com.gr6.smartcart_android.seller.voucher;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.seller.voucher.repository.SellerVoucherRepository;
import com.gr6.smartcart_android.seller.voucher.request.VoucherRequest;
import com.gr6.smartcart_android.seller.voucher.response.VoucherResponse;

import java.util.ArrayList;
import java.util.List;

public class SellerVoucherViewModel extends AndroidViewModel {

    private final SellerVoucherRepository repository;
    private final MutableLiveData<VoucherListState> voucherListState = new MutableLiveData<>();
    private final MutableLiveData<VoucherActionState> actionState = new MutableLiveData<>();

    public SellerVoucherViewModel(@NonNull Application application) {
        super(application);
        repository = new SellerVoucherRepository(application);
        actionState.setValue(VoucherActionState.idle());
    }

    public LiveData<VoucherListState> getVoucherListState() { return voucherListState; }
    public LiveData<VoucherActionState> getActionState() { return actionState; }

    public void loadMyVouchers() {
        voucherListState.setValue(VoucherListState.loading());
        repository.loadMyVouchers(new SellerVoucherRepository.VoucherCallback<List<VoucherResponse>>() {
            @Override public void onSuccess(List<VoucherResponse> data, String message) {
                voucherListState.postValue(VoucherListState.success(data == null ? new ArrayList<>() : data, message));
            }
            @Override public void onError(String message) {
                voucherListState.postValue(VoucherListState.error(message));
            }
        });
    }

    public void createVoucher(VoucherRequest request) {
        actionState.setValue(VoucherActionState.loading());
        repository.createVoucher(request, new SellerVoucherRepository.VoucherCallback<VoucherResponse>() {
            @Override public void onSuccess(VoucherResponse data, String message) {
                actionState.postValue(VoucherActionState.success(message));
            }
            @Override public void onError(String message) {
                actionState.postValue(VoucherActionState.error(message));
            }
        });
    }

    public void updateVoucher(Long voucherId, VoucherRequest request) {
        actionState.setValue(VoucherActionState.loading());
        repository.updateVoucher(voucherId, request, new SellerVoucherRepository.VoucherCallback<VoucherResponse>() {
            @Override public void onSuccess(VoucherResponse data, String message) {
                actionState.postValue(VoucherActionState.success(message));
            }
            @Override public void onError(String message) {
                actionState.postValue(VoucherActionState.error(message));
            }
        });
    }

    public void deactivateVoucher(Long voucherId) {
        actionState.setValue(VoucherActionState.loading());
        repository.deactivateVoucher(voucherId, new SellerVoucherRepository.SimpleCallback() {
            @Override public void onSuccess(String message) {
                actionState.postValue(VoucherActionState.success(message));
                loadMyVouchers();
            }
            @Override public void onError(String message) {
                actionState.postValue(VoucherActionState.error(message));
            }
        });
    }
}
