package com.gr6.smartcart_android.buyer.account.profile.api;

import com.gr6.smartcart_android.buyer.account.profile.request.ProfileUpdateRequest;
import com.gr6.smartcart_android.buyer.account.profile.response.ProfileResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface ProfileApiService {

    @GET("api/v2/user/profile")
    Call<BaseResponse<ProfileResponse>> getProfile();

    @PUT("api/v2/user/profile")
    Call<BaseResponse<ProfileResponse>> updateProfile(
            @Body ProfileUpdateRequest request
    );
}