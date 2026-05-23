package com.gr6.smartcart_android.buyer.review.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Response sau khi tạo review thành công.
 */
public class ReviewResponse {

    @SerializedName("reviewId")
    private Long reviewId;

    @SerializedName("orderItemId")
    private Long orderItemId;

    @SerializedName("productId")
    private Long productId;

    @SerializedName("rating")
    private Integer rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    @SerializedName("videoUrl")
    private String videoUrl;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getReviewId() {
        return reviewId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getRating() {
        return rating == null ? 0 : rating;
    }

    public String getComment() {
        return comment == null ? "" : comment;
    }

    public List<String> getImageUrls() {
        return imageUrls == null ? new ArrayList<>() : imageUrls;
    }

    public String getVideoUrl() {
        return videoUrl == null ? "" : videoUrl;
    }

    public String getCreatedAt() {
        return createdAt == null ? "" : createdAt;
    }
}