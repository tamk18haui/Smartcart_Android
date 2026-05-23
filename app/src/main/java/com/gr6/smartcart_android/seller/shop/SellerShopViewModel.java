package com.gr6.smartcart_android.seller.shop;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.seller.shop.repository.SellerShopRepository;
import com.gr6.smartcart_android.seller.shop.request.SellerShopUpdateRequest;
import com.gr6.smartcart_android.seller.shop.response.SellerShopInfoResponse;

public class SellerShopViewModel extends AndroidViewModel {

    private final SellerShopRepository repository;
    private final MutableLiveData<SellerShopState> shopState = new MutableLiveData<>();
    private final MutableLiveData<SellerShopActionState> actionState = new MutableLiveData<>();

    public SellerShopViewModel(@NonNull Application application) {
        super(application);
        repository = new SellerShopRepository(application);
        actionState.setValue(SellerShopActionState.idle());
    }

    public LiveData<SellerShopState> getShopState() { return shopState; }
    public LiveData<SellerShopActionState> getActionState() { return actionState; }

    public void loadMyShopInfo() {
        shopState.setValue(SellerShopState.loading());
        repository.loadMyShopInfo(new SellerShopRepository.ShopCallback<SellerShopInfoResponse>() {
            @Override public void onSuccess(SellerShopInfoResponse data, String message) {
                shopState.postValue(SellerShopState.success(data, message));
            }
            @Override public void onError(String message) {
                shopState.postValue(SellerShopState.error(message));
            }
        });
    }

    public void updateMyShop(SellerShopUpdateRequest request) {
        actionState.setValue(SellerShopActionState.loading());
        repository.updateMyShop(request, new SellerShopRepository.ShopCallback<Object>() {
            @Override public void onSuccess(Object data, String message) {
                actionState.postValue(SellerShopActionState.success(message));
                loadMyShopInfo();
            }
            @Override public void onError(String message) {
                actionState.postValue(SellerShopActionState.error(message));
            }
        });
    }
}
