package com.gr6.smartcart_android.common.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.gr6.smartcart_android.R;

public class ThemeColor {

    private ThemeColor() {
    }

    @ColorInt
    public static int primary(android.content.Context context) {
        return ContextCompat.getColor(context, R.color.brand_primary);
    }

    @ColorInt
    public static int primaryDark(android.content.Context context) {
        return ContextCompat.getColor(context, R.color.brand_primary_dark);
    }

    @ColorInt
    public static int primaryLight(android.content.Context context) {
        return ContextCompat.getColor(context, R.color.brand_primary_light);
    }

    @ColorInt
    public static int secondary(android.content.Context context) {
        return ContextCompat.getColor(context, R.color.brand_secondary);
    }

    @ColorInt
    public static int background(android.content.Context context) {
        return ContextCompat.getColor(context, R.color.background);
    }

    @ColorInt
    public static int surface(android.content.Context context) {
        return ContextCompat.getColor(context, R.color.surface);
    }

    @ColorInt
    public static int textPrimary(android.content.Context context) {
        return ContextCompat.getColor(context, R.color.text_primary);
    }

    @ColorInt
    public static int textSecondary(android.content.Context context) {
        return ContextCompat.getColor(context, R.color.text_secondary);
    }

    @ColorInt
    public static int border(android.content.Context context) {
        return ContextCompat.getColor(context, R.color.brand_border);
    }

    @ColorInt
    public static int danger(android.content.Context context) {
        return ContextCompat.getColor(context, R.color.danger);
    }

    @ColorInt
    public static int success(android.content.Context context) {
        return ContextCompat.getColor(context, R.color.success);
    }

    @ColorInt
    public static int warning(android.content.Context context) {
        return ContextCompat.getColor(context, R.color.warning);
    }

    public static void applyBrandStatusBar(Activity activity) {
        if (activity == null) return;

        activity.getWindow().setStatusBarColor(primary(activity));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    public static void applyLightStatusBar(Activity activity) {
        if (activity == null) return;

        activity.getWindow().setStatusBarColor(background(activity));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void applyWhiteNavigationBar(Activity activity) {
        if (activity == null) return;

        activity.getWindow().setNavigationBarColor(surface(activity));
    }
}