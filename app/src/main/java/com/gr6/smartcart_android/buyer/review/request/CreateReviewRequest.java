package com.gr6.smartcart_android.buyer.review.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request gửi review lên backend.
 *
 * Backend nhận:
 * - orderItemId: sản phẩm cụ thể trong đơn hàng
 * - rating: 1 đến 5 sao
 * - comment: nội dung đánh giá
 * - imageUrls: tối đa 4 ảnh
 * - videoUrl: tối đa 1 video
 */
public class CreateReviewRequest {

    @SerializedName("orderItemId")
    private Long orderItemId;

    @SerializedName("rating")
    private Integer rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    @SerializedName("videoUrl")
    private String videoUrl;

    public CreateReviewRequest(
            Long orderItemId,
            Integer rating,
            String comment,
            List<String> imageUrls,
            String videoUrl
    ) {
        this.orderItemId = orderItemId;
        this.rating = rating;
        this.comment = comment;
        this.imageUrls = imageUrls;
        this.videoUrl = videoUrl;
    }
}