package com.gr6.smartcart_android.buyer.product;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.buyer.product.repository.ProductRepository;
import com.gr6.smartcart_android.buyer.product.response.ProductDetailResponse;

import java.util.List;

public class ProductDetailViewModel extends AndroidViewModel {

    private final ProductRepository repository;

    private final MutableLiveData<ProductDetailState> detailState = new MutableLiveData<>();
    private final MutableLiveData<ProductActionState> actionState = new MutableLiveData<>();
    private final MutableLiveData<List<ProductDetailResponse.ShopVoucherDTO>> voucherState =
            new MutableLiveData<>();

    public ProductDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
    }

    public LiveData<ProductDetailState> getDetailState() {
        return detailState;
    }

    public LiveData<ProductActionState> getActionState() {
        return actionState;
    }

    public LiveData<List<ProductDetailResponse.ShopVoucherDTO>> getVoucherState() {
        return voucherState;
    }

    public void loadProductDetail(Long productId) {
        detailState.setValue(ProductDetailState.loading());

        repository.getProductDetail(productId, new ProductRepository.ProductDetailCallback() {
            @Override
            public void onSuccess(ProductDetailResponse data) {
                detailState.postValue(ProductDetailState.success(data));
            }

            @Override
            public void onError(String message) {
                detailState.postValue(ProductDetailState.error(message));
            }
        });
    }

    public void addToCart(Long variantId, Integer quantity) {
        actionState.setValue(ProductActionState.loading());

        repository.addToCart(variantId, quantity, new ProductRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                actionState.postValue(ProductActionState.success(message));
            }

            @Override
            public void onError(String message) {
                actionState.postValue(ProductActionState.error(message));
            }
        });
    }

    public void loadShopVouchers(Long shopId) {
        repository.getShopVouchers(shopId, vouchers -> voucherState.postValue(vouchers));
    }
}