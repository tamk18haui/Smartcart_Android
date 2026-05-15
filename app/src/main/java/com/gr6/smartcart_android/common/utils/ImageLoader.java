package com.gr6.smartcart_android.common.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gr6.smartcart_android.R;

public class ImageLoader {

    private ImageLoader() {
    }

    public static void load(Context context, String url, ImageView imageView) {
        if (context == null || imageView == null) return;

        if (url == null || url.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.bg_image_placeholder);
            return;
        }

        Glide.with(context)
                .load(url.trim())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .into(imageView);
    }

    public static void loadCircle(Context context, String url, ImageView imageView) {
        if (context == null || imageView == null) return;

        if (url == null || url.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.bg_image_placeholder);
            return;
        }

        Glide.with(context)
                .load(url.trim())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .circleCrop()
                .into(imageView);
    }

    public static void clear(Context context, ImageView imageView) {
        if (context == null || imageView == null) return;

        Glide.with(context).clear(imageView);
    }
}