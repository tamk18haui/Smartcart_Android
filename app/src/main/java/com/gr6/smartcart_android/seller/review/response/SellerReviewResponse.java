package com.gr6.smartcart_android.seller.review.response;

import com.google.gson.annotations.SerializedName;

public class SellerReviewResponse {

    @SerializedName("reviewId")
    private Long reviewId;

    @SerializedName("productId")
    private Long productId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("productImageUrl")
    private String productImageUrl;

    @SerializedName("buyerId")
    private Long buyerId;

    @SerializedName("buyerName")
    private String buyerName;

    @SerializedName("rating")
    private Integer rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("sellerReply")
    private String sellerReply;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getReviewId() {
        return reviewId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName == null ? "Sản phẩm" : productName;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public String getBuyerName() {
        if (buyerName == null || buyerName.trim().isEmpty()) {
            return "Người mua SmartCart";
        }
        return buyerName.trim();
    }

    public int getRating() {
        return rating == null ? 0 : rating;
    }

    public String getComment() {
        if (comment == null || comment.trim().isEmpty()) {
            return "Người mua chưa để lại bình luận.";
        }
        return comment.trim();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSellerReply() {
        return sellerReply;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
