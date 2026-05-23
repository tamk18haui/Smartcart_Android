package com.gr6.smartcart_android.buyer.review.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.buyer.review.api.ReviewApiService;
import com.gr6.smartcart_android.buyer.review.request.CreateReviewRequest;
import com.gr6.smartcart_android.buyer.review.response.ReviewResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository gọi API review thật từ backend.
 */
public class ReviewRepository {

    private final ReviewApiService apiService;

    public ReviewRepository(Context context) {
        apiService = ApiClient.createService(context, ReviewApiService.class);
    }

    public void createReview(
            Long orderItemId,
            int rating,
            String comment,
            List<String> imageUrls,
            String videoUrl,
            ReviewCallback callback
    ) {
        if (orderItemId == null || orderItemId <= 0) {
            callback.onError("Không tìm thấy sản phẩm trong đơn hàng");
            return;
        }

        if (rating < 1 || rating > 5) {
            callback.onError("Vui lòng chọn từ 1 đến 5 sao");
            return;
        }

        CreateReviewRequest request = new CreateReviewRequest(
                orderItemId,
                rating,
                comment,
                imageUrls,
                videoUrl
        );

        apiService.createReview(request).enqueue(new Callback<BaseResponse<ReviewResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<ReviewResponse>> call,
                    @NonNull Response<BaseResponse<ReviewResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Gửi đánh giá thất bại. Mã lỗi: " + response.code());
                    return;
                }

                BaseResponse<ReviewResponse> body = response.body();

                if (body == null) {
                    callback.onError("Server không trả dữ liệu");
                    return;
                }

                if (!body.isSuccess()) {
                    callback.onError(body.getSafeMessage());
                    return;
                }

                callback.onSuccess(body.getData(), body.getSafeMessage());
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<ReviewResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public interface ReviewCallback {
        void onSuccess(ReviewResponse data, String message);

        void onError(String message);
    }
}