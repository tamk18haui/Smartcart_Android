package com.gr6.smartcart_android.auth.api;

import com.gr6.smartcart_android.auth.request.LoginRequest;
import com.gr6.smartcart_android.auth.request.RegisterRequest;
import com.gr6.smartcart_android.auth.request.ShopRegisterRequest;
import com.gr6.smartcart_android.auth.response.LoginResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("api/v1/auth/login")
    Call<BaseResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("api/v1/auth/register")
    Call<BaseResponse<Object>> registerBuyer(@Body RegisterRequest request);

    @POST("api/v1/shops/register")
    Call<BaseResponse<Object>> registerSeller(@Body ShopRegisterRequest request);
}