package com.gr6.smartcart_android.navigation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.account.AccountActivity;
import com.gr6.smartcart_android.buyer.main.BuyerMainActivity;
import com.gr6.smartcart_android.buyer.notification.NotificationActivity;

public class BuyerBottomNavHelper {

    public static final int TAB_HOME = 0;
    public static final int TAB_CATEGORY = 1;
    public static final int TAB_NOTIFICATION = 2;
    public static final int TAB_ACCOUNT = 3;

    private BuyerBottomNavHelper() {
    }

    public static void setup(Activity activity, int activeTab) {
        if (activity == null) return;

        View navHome = activity.findViewById(R.id.navHome);
        View navCategory = activity.findViewById(R.id.navCategory);
        View navNotification = activity.findViewById(R.id.navNotification);
        View navAccount = activity.findViewById(R.id.navAccount);

        applyActiveState(activity, navHome, activeTab == TAB_HOME);
        applyActiveState(activity, navCategory, activeTab == TAB_CATEGORY);
        applyActiveState(activity, navNotification, activeTab == TAB_NOTIFICATION);
        applyActiveState(activity, navAccount, activeTab == TAB_ACCOUNT);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (activeTab == TAB_HOME) return;

                Intent intent = new Intent(activity, BuyerMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            });
        }

        if (navCategory != null) {
            navCategory.setOnClickListener(v -> {
                if (activeTab == TAB_CATEGORY) return;

                showToast(activity, "Danh mục sẽ làm ở bước sau");
            });
        }

        if (navNotification != null) {
            navNotification.setOnClickListener(v -> {
                if (activeTab == TAB_NOTIFICATION) return;

                Intent intent = new Intent(activity, NotificationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            });
        }

        if (navAccount != null) {
            navAccount.setOnClickListener(v -> {
                if (activeTab == TAB_ACCOUNT) return;

                Intent intent = new Intent(activity, AccountActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            });
        }
    }

    private static void applyActiveState(Activity activity, View item, boolean active) {
        if (activity == null || item == null) return;

        item.setBackgroundResource(active ? R.drawable.bg_bottom_nav_selected : 0);

        int color = ContextCompat.getColor(
                activity,
                active ? R.color.brand_primary : R.color.nav_inactive
        );

        applyColorRecursive(item, color, active);
    }

    private static void applyColorRecursive(View view, int color, boolean active) {
        if (view instanceof ImageView) {
            ((ImageView) view).setColorFilter(color);
        }

        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(color);
            textView.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                applyColorRecursive(group.getChildAt(i), color, active);
            }
        }
    }

    private static void showToast(Activity activity, String message) {
        if (activity == null || message == null || message.trim().isEmpty()) return;
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
}