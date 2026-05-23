package com.gr6.smartcart_android.buyer.review.api;

import com.gr6.smartcart_android.buyer.review.request.CreateReviewRequest;
import com.gr6.smartcart_android.buyer.review.response.ReviewResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ReviewApiService {

    @POST("api/v1/reviews")
    Call<BaseResponse<ReviewResponse>> createReview(
            @Body CreateReviewRequest request
    );
}