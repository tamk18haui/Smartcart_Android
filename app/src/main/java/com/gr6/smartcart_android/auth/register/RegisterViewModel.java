package com.gr6.smartcart_android.auth.register;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.auth.repository.AuthRepository;
import com.gr6.smartcart_android.auth.request.RegisterRequest;
import com.gr6.smartcart_android.auth.request.ShopRegisterRequest;

public class RegisterViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<RegisterState> registerState = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public LiveData<RegisterState> getRegisterState() {
        return registerState;
    }

    public void registerBuyer(RegisterRequest request) {
        registerState.setValue(RegisterState.loading());

        authRepository.registerBuyer(request, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                registerState.postValue(RegisterState.success(message));
            }

            @Override
            public void onError(String message) {
                registerState.postValue(RegisterState.error(message));
            }
        });
    }

    public void registerSeller(ShopRegisterRequest request) {
        registerState.setValue(RegisterState.loading());

        authRepository.registerSeller(request, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                registerState.postValue(RegisterState.success(message));
            }

            @Override
            public void onError(String message) {
                registerState.postValue(RegisterState.error(message));
            }
        });
    }
}