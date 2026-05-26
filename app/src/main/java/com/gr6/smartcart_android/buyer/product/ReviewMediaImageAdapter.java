package com.gr6.smartcart_android.buyer.product;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ReviewMediaImageAdapter extends RecyclerView.Adapter<ReviewMediaImageAdapter.ImageVH> {

    private final Context context;
    private final List<String> images = new ArrayList<>();

    public ReviewMediaImageAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<String> data) {
        images.clear();

        if (data != null) {
            for (String url : data) {
                if (url == null || url.trim().isEmpty()) continue;
                images.add(url.trim());
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageVH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review_media_image, parent, false);

        return new ImageVH(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ImageVH holder,
            int position
    ) {
        String imageUrl = images.get(position);

        ImageLoader.load(context, imageUrl, holder.imgReviewMedia);

        holder.imgReviewMedia.setOnClickListener(v -> showImagePreview(imageUrl));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public void onViewRecycled(@NonNull ImageVH holder) {
        super.onViewRecycled(holder);
        ImageLoader.clear(context, holder.imgReviewMedia);
    }

    private void showImagePreview(String imageUrl) {
        if (context == null || imageUrl == null || imageUrl.trim().isEmpty()) return;

        AlertDialog dialog = new AlertDialog.Builder(context).create();

        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        imageView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.black));
        imageView.setPadding(8, 8, 8, 8);

        ImageLoader.loadProductBanner(context, imageUrl.trim(), imageView);

        imageView.setOnClickListener(v -> dialog.dismiss());

        dialog.setView(imageView);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.black);
            dialog.getWindow().setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
        }
    }

    static class ImageVH extends RecyclerView.ViewHolder {

        private final ImageView imgReviewMedia;

        ImageVH(@NonNull View itemView) {
            super(itemView);

            imgReviewMedia = itemView.findViewById(R.id.imgReviewMedia);
        }
    }
}