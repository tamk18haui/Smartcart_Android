package com.gr6.smartcart_android.seller.product;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final List<Uri> images;
    private final Runnable onDataChanged;

    public ImageAdapter(List<Uri> images, Runnable onDataChanged) {
        this.images = images;
        this.onDataChanged = onDataChanged;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_image, parent, false);

        ViewGroup.LayoutParams params = view.getLayoutParams();

        if (params != null) {
            params.width = dp(parent, 82);
            params.height = dp(parent, 82);
            view.setLayoutParams(params);
        }

        RecyclerView.LayoutParams rvParams = new RecyclerView.LayoutParams(
                dp(parent, 82),
                dp(parent, 82)
        );
        rvParams.setMargins(0, 0, dp(parent, 10), 0);
        view.setLayoutParams(rvParams);

        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ImageViewHolder holder,
            int position
    ) {
        Uri uri = images.get(position);

        holder.imgProductImage.setImageURI(uri);

        holder.btnRemoveImage.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            images.remove(adapterPosition);
            notifyItemRemoved(adapterPosition);
            notifyItemRangeChanged(adapterPosition, images.size());

            if (onDataChanged != null) {
                onDataChanged.run();
            }
        });
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }

    private int dp(View view, int value) {
        return (int) (value * view.getResources().getDisplayMetrics().density + 0.5f);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProductImage;
        private final TextView btnRemoveImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProductImage = itemView.findViewById(R.id.imgProductImage);
            btnRemoveImage = itemView.findViewById(R.id.btnRemoveImage);
        }
    }
}


