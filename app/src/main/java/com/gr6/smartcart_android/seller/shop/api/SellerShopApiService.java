package com.gr6.smartcart_android.seller.shop.api;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.seller.shop.request.SellerShopUpdateRequest;
import com.gr6.smartcart_android.seller.shop.response.SellerShopInfoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface SellerShopApiService {

    @GET("api/v1/shops/info")
    Call<BaseResponse<SellerShopInfoResponse>> getMyShopInfo();

    @PUT("api/v1/shops/update")
    Call<BaseResponse<Object>> updateMyShop(@Body SellerShopUpdateRequest request);
}
