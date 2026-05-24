package com.gr6.smartcart_android.buyer.product;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.product.response.ProductDetailResponse;

import java.util.ArrayList;
import java.util.List;

public class ProductReviewAdapter extends RecyclerView.Adapter<ProductReviewAdapter.ReviewVH> {

    private final Context context;
    private final List<ProductDetailResponse.ReviewDTO> reviews = new ArrayList<>();

    public ProductReviewAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<ProductDetailResponse.ReviewDTO> data) {
        reviews.clear();

        if (data != null) {
            reviews.addAll(data);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewVH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_review, parent, false);

        return new ReviewVH(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ReviewVH holder,
            int position
    ) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class ReviewVH extends RecyclerView.ViewHolder {

        private final TextView txtAvatarLetter;
        private final TextView txtUserName;
        private final TextView txtRating;
        private final TextView txtCreatedAt;
        private final TextView txtComment;
        private final RecyclerView rcvReviewImages;
        private final VideoView videoReview;
        private final View layoutSellerReply;
        private final TextView txtSellerReply;

        ReviewVH(@NonNull View itemView) {
            super(itemView);

            txtAvatarLetter = itemView.findViewById(R.id.txtAvatarLetter);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtCreatedAt = itemView.findViewById(R.id.txtCreatedAt);
            txtComment = itemView.findViewById(R.id.txtComment);
            rcvReviewImages = itemView.findViewById(R.id.rcvReviewImages);
            videoReview = itemView.findViewById(R.id.videoReview);
            layoutSellerReply = itemView.findViewById(R.id.layoutSellerReply);
            txtSellerReply = itemView.findViewById(R.id.txtSellerReply);
        }

        void bind(ProductDetailResponse.ReviewDTO review) {
            if (review == null) return;

            String userName = review.getUserName();
            txtUserName.setText(userName);
            txtAvatarLetter.setText(getAvatarLetter(userName));

            txtRating.setText(buildStars(review.getRating()) + "  " + review.getRating() + "/5");

            String createdAt = formatDate(review.getCreatedAt());
            if (createdAt.isEmpty()) {
                txtCreatedAt.setVisibility(View.GONE);
            } else {
                txtCreatedAt.setVisibility(View.VISIBLE);
                txtCreatedAt.setText(createdAt);
            }

            txtComment.setText(review.getComment());

            bindImages(review.getImageUrls());
            bindVideo(review.getVideoUrl());
            bindSellerReply(review.getSellerReply());
        }

        private void bindImages(List<String> imageUrls) {
            if (imageUrls == null || imageUrls.isEmpty()) {
                rcvReviewImages.setVisibility(View.GONE);
                rcvReviewImages.setAdapter(null);
                return;
            }

            rcvReviewImages.setVisibility(View.VISIBLE);

            ReviewMediaImageAdapter imageAdapter = new ReviewMediaImageAdapter(context);
            imageAdapter.setData(imageUrls);

            rcvReviewImages.setLayoutManager(
                    new LinearLayoutManager(
                            context,
                            LinearLayoutManager.HORIZONTAL,
                            false
                    )
            );

            rcvReviewImages.setAdapter(imageAdapter);
        }

        private void bindVideo(String videoUrl) {
            if (videoUrl == null || videoUrl.trim().isEmpty()) {
                videoReview.stopPlayback();
                videoReview.setVisibility(View.GONE);
                return;
            }

            videoReview.setVisibility(View.VISIBLE);
            videoReview.setVideoURI(Uri.parse(videoUrl.trim()));

            MediaController mediaController = new MediaController(context);
            mediaController.setAnchorView(videoReview);
            videoReview.setMediaController(mediaController);

            videoReview.setOnPreparedListener(mp -> {
                mp.setLooping(false);
                videoReview.seekTo(100);
            });

            videoReview.setOnClickListener(v -> videoReview.start());
        }

        private void bindSellerReply(String sellerReply) {
            if (sellerReply == null || sellerReply.trim().isEmpty()) {
                layoutSellerReply.setVisibility(View.GONE);
                return;
            }

            layoutSellerReply.setVisibility(View.VISIBLE);
            txtSellerReply.setText(sellerReply.trim());
        }

        private String getAvatarLetter(String name) {
            if (name == null || name.trim().isEmpty()) {
                return "S";
            }

            return name.trim().substring(0, 1).toUpperCase();
        }

        private String buildStars(int rating) {
            int safeRating = Math.max(0, Math.min(5, rating));

            StringBuilder builder = new StringBuilder();

            for (int i = 1; i <= 5; i++) {
                builder.append(i <= safeRating ? "★" : "☆");
            }

            return builder.toString();
        }

        private String formatDate(String raw) {
            if (raw == null || raw.trim().isEmpty()) {
                return "";
            }

            String value = raw.trim().replace("T", " ");

            if (value.length() >= 16) {
                return value.substring(0, 16);
            }

            return value;
        }
    }
}