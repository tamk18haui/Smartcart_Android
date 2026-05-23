package com.gr6.smartcart_android.buyer.review;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.buyer.review.repository.ReviewRepository;
import com.gr6.smartcart_android.buyer.review.response.ReviewResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel xử lý logic gửi review.
 *
 * Activity không gọi API trực tiếp.
 */
public class ReviewViewModel extends AndroidViewModel {

    private final ReviewRepository repository;
    private final MutableLiveData<ReviewState> reviewState = new MutableLiveData<>();

    public ReviewViewModel(@NonNull Application application) {
        super(application);
        repository = new ReviewRepository(application);
    }

    public LiveData<ReviewState> getReviewState() {
        return reviewState;
    }

    public void submitReview(
            Long orderItemId,
            int rating,
            String comment,
            List<String> imageUrls,
            String videoUrl
    ) {
        if (orderItemId == null || orderItemId <= 0) {
            reviewState.setValue(ReviewState.error("Không tìm thấy sản phẩm trong đơn hàng"));
            return;
        }

        if (rating < 1 || rating > 5) {
            reviewState.setValue(ReviewState.error("Vui lòng chọn số sao hợp lệ"));
            return;
        }

        List<String> safeImages = new ArrayList<>();

        if (imageUrls != null) {
            for (String url : imageUrls) {
                if (url == null || url.trim().isEmpty()) continue;

                safeImages.add(url.trim());

                if (safeImages.size() == 4) {
                    break;
                }
            }
        }

        String safeVideo = videoUrl == null || videoUrl.trim().isEmpty()
                ? null
                : videoUrl.trim();

        reviewState.setValue(ReviewState.loading());

        repository.createReview(
                orderItemId,
                rating,
                comment == null ? "" : comment.trim(),
                safeImages,
                safeVideo,
                new ReviewRepository.ReviewCallback() {
                    @Override
                    public void onSuccess(ReviewResponse data, String message) {
                        reviewState.postValue(ReviewState.success(message));
                    }

                    @Override
                    public void onError(String message) {
                        reviewState.postValue(ReviewState.error(message));
                    }
                }
        );
    }
}