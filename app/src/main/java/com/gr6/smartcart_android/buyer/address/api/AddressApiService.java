package com.gr6.smartcart_android.buyer.address.api;

import com.gr6.smartcart_android.buyer.address.request.AddressRequest;
import com.gr6.smartcart_android.buyer.address.response.AddressResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AddressApiService {

    @GET("api/v2/customer/addresses")
    Call<BaseResponse<List<AddressResponse>>> getMyAddresses();

    @POST("api/v2/customer/addresses")
    Call<BaseResponse<AddressResponse>> createAddress(@Body AddressRequest request);

    @PUT("api/v2/customer/addresses/{id}")
    Call<BaseResponse<AddressResponse>> updateAddress(
            @Path("id") Long addressId,
            @Body AddressRequest request
    );

    @DELETE("api/v2/customer/addresses/{id}")
    Call<BaseResponse<String>> deleteAddress(@Path("id") Long addressId);

    @PUT("api/v2/customer/addresses/{id}/set-default")
    Call<BaseResponse<String>> setDefaultAddress(@Path("id") Long addressId);
}