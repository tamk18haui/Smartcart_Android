package com.gr6.smartcart_android.seller.review.api;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.seller.review.response.SellerReviewResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SellerReviewApiService {

    @GET("api/v1/reviews/shop")
    Call<BaseResponse<List<SellerReviewResponse>>> getShopReviews();
}
