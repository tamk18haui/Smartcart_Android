package com.gr6.smartcart_android.auth.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.auth.repository.AuthRepository;
import com.gr6.smartcart_android.auth.request.LoginRequest;
import com.gr6.smartcart_android.auth.response.LoginResponse;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    public void login(String email, String password) {
        loginState.setValue(LoginState.loading());

        LoginRequest request = new LoginRequest(email, password);

        authRepository.login(request, new AuthRepository.AuthCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse data, String message) {
                loginState.postValue(LoginState.success(data, message));
            }

            @Override
            public void onError(String message) {
                loginState.postValue(LoginState.error(message));
            }
        });
    }
}