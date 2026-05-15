package com.gr6.smartcart_android.buyer.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder> {

    private final List<String> images = new ArrayList<>();

    public void setImages(List<String> data) {
        images.clear();

        if (data != null) {
            for (String url : data) {
                if (url != null && !url.trim().isEmpty()) {
                    images.add(url.trim());
                }
            }
        }

        notifyDataSetChanged();
    }

    public int getImageCount() {
        return images.size();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ImageViewHolder holder,
            int position
    ) {
        String imageUrl = images.get(position);
        ImageLoader.load(holder.itemView.getContext(), imageUrl, holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProductImage);
        }
    }
}