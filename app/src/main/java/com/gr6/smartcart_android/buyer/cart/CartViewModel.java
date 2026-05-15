package com.gr6.smartcart_android.buyer.cart;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.buyer.cart.repository.CartRepository;
import com.gr6.smartcart_android.buyer.cart.response.CartDetailResponse;

public class CartViewModel extends AndroidViewModel {

    private final CartRepository repository;

    private final MutableLiveData<CartListState> cartState = new MutableLiveData<>();
    private final MutableLiveData<CartActionState> actionState = new MutableLiveData<>();

    public CartViewModel(@NonNull Application application) {
        super(application);
        repository = new CartRepository(application);
    }

    public LiveData<CartListState> getCartState() {
        return cartState;
    }

    public LiveData<CartActionState> getActionState() {
        return actionState;
    }

    public void loadCart() {
        cartState.setValue(CartListState.loading());

        repository.getCartItems(new CartRepository.CartCallback() {
            @Override
            public void onSuccess(CartDetailResponse data) {
                cartState.postValue(CartListState.success(data));
            }

            @Override
            public void onError(String message) {
                cartState.postValue(CartListState.error(message));
            }
        });
    }

    public void updateQuantity(Long variantId, int quantity) {
        if (variantId == null) {
            actionState.setValue(CartActionState.error("Không tìm thấy sản phẩm"));
            return;
        }

        actionState.setValue(CartActionState.loading());

        repository.updateQuantity(variantId, quantity, new CartRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                actionState.postValue(CartActionState.success(message));
            }

            @Override
            public void onError(String message) {
                actionState.postValue(CartActionState.error(message));
            }
        });
    }
    public void changeVariant(Long cartItemId, Long newVariantId) {
        if (cartItemId == null || cartItemId <= 0) {
            actionState.setValue(CartActionState.error("Không tìm thấy sản phẩm trong giỏ hàng"));
            return;
        }

        if (newVariantId == null || newVariantId <= 0) {
            actionState.setValue(CartActionState.error("Phân loại không hợp lệ"));
            return;
        }

        actionState.setValue(CartActionState.loading());

        repository.changeVariant(cartItemId, newVariantId, new CartRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                actionState.postValue(CartActionState.success(message));
            }

            @Override
            public void onError(String message) {
                actionState.postValue(CartActionState.error(message));
            }
        });
    }
    public void removeItem(Long variantId) {
        if (variantId == null) {
            actionState.setValue(CartActionState.error("Không tìm thấy sản phẩm"));
            return;
        }

        actionState.setValue(CartActionState.loading());

        repository.removeItem(variantId, new CartRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                actionState.postValue(CartActionState.success(message));
            }

            @Override
            public void onError(String message) {
                actionState.postValue(CartActionState.error(message));
            }
        });
    }
}