package com.gr6.smartcart_android.seller.inventory;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.seller.inventory.repository.SellerInventoryRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerInventoryViewModel extends AndroidViewModel {

    private final SellerInventoryRepository repository;
    private final MutableLiveData<InventoryActionState> actionState = new MutableLiveData<>();

    public SellerInventoryViewModel(@NonNull Application application) {
        super(application);
        repository = new SellerInventoryRepository(application);
        actionState.setValue(InventoryActionState.idle());
    }

    public LiveData<InventoryActionState> getActionState() { return actionState; }

    public void increaseStock(Long variantId, int quantity) {
        actionState.setValue(InventoryActionState.loading());
        repository.increaseStock(variantId, quantity).enqueue(wrap());
    }

    public void decreaseStock(Long variantId, int quantity) {
        actionState.setValue(InventoryActionState.loading());
        repository.decreaseStock(variantId, quantity).enqueue(wrap());
    }

    private Callback<BaseResponse<String>> wrap() {
        return new Callback<BaseResponse<String>>() {
            @Override public void onResponse(@NonNull Call<BaseResponse<String>> call,
                                             @NonNull Response<BaseResponse<String>> response) {
                BaseResponse<String> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    String message = body == null ? "Cập nhật tồn kho thất bại" : body.getSafeMessage();
                    actionState.postValue(InventoryActionState.error(message));
                    return;
                }
                actionState.postValue(InventoryActionState.success(body.getSafeMessage()));
            }

            @Override public void onFailure(@NonNull Call<BaseResponse<String>> call, @NonNull Throwable t) {
                actionState.postValue(InventoryActionState.error("Không kết nối được server: " + t.getMessage()));
            }
        };
    }
}
