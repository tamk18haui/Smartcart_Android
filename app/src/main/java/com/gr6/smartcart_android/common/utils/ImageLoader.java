package com.gr6.smartcart_android.common.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
                .apply(new RequestOptions()
                        .centerCrop()
                        .override(500, 500)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.bg_image_placeholder)
                        .error(R.drawable.bg_image_placeholder)
                )
                .into(imageView);
    }

    public static void loadProductBanner(Context context, String url, ImageView imageView) {
        if (context == null || imageView == null) return;

        if (url == null || url.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.bg_image_placeholder);
            return;
        }

        Glide.with(context)
                .load(url.trim())
                .apply(new RequestOptions()
                        .centerCrop()
                        .override(900, 900)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.bg_image_placeholder)
                        .error(R.drawable.bg_image_placeholder)
                )
                .into(imageView);
    }

    public static void loadSmall(Context context, String url, ImageView imageView) {
        if (context == null || imageView == null) return;

        if (url == null || url.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.bg_image_placeholder);
            return;
        }

        Glide.with(context)
                .load(url.trim())
                .apply(new RequestOptions()
                        .centerCrop()
                        .override(160, 160)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.bg_image_placeholder)
                        .error(R.drawable.bg_image_placeholder)
                )
                .into(imageView);
    }

    public static void loadCircle(Context context, String url, ImageView imageView) {
        if (context == null || imageView == null) return;

        if (url == null || url.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.ic_category);
            return;
        }

        Glide.with(context)
                .load(url.trim())
                .apply(new RequestOptions()
                        .circleCrop()
                        .override(220, 220)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.bg_image_placeholder)
                        .error(R.drawable.bg_image_placeholder)
                )
                .into(imageView);
    }

    public static void loadCircleShop(Context context, String url, ImageView imageView) {
        if (context == null || imageView == null) return;

        if (url == null || url.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.ic_shop);
            return;
        }

        Glide.with(context)
                .load(url.trim())
                .apply(new RequestOptions()
                        .circleCrop()
                        .override(220, 220)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.ic_shop)
                        .error(R.drawable.ic_shop)
                )
                .into(imageView);
    }

    public static void clear(Context context, ImageView imageView) {
        if (context == null || imageView == null) return;
        Glide.with(context).clear(imageView);
    }
}