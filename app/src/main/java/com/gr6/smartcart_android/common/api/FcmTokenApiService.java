package com.gr6.smartcart_android.common.api;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.request.FcmTokenRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FcmTokenApiService {

    @POST("api/v1/fcm/token")
    Call<BaseResponse<Object>> saveToken(@Body FcmTokenRequest request);
}
