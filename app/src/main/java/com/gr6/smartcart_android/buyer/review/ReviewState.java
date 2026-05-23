package com.gr6.smartcart_android.buyer.review;

/**
 * State dùng để Activity observe trạng thái gửi review.
 */
public class ReviewState {

    private final boolean loading;
    private final boolean success;
    private final String message;

    private ReviewState(
            boolean loading,
            boolean success,
            String message
    ) {
        this.loading = loading;
        this.success = success;
        this.message = message;
    }

    public static ReviewState loading() {
        return new ReviewState(true, false, null);
    }

    public static ReviewState success(String message) {
        return new ReviewState(false, true, message);
    }

    public static ReviewState error(String message) {
        return new ReviewState(false, false, message);
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message == null ? "Có lỗi xảy ra" : message;
    }
}