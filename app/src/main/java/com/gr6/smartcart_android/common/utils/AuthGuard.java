package com.gr6.smartcart_android.common.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.gr6.smartcart_android.auth.login.LoginActivity;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;

public class AuthGuard {

    public static final String EXTRA_NEXT_ACTIVITY = "next_activity";

    private AuthGuard() {
    }

    public static boolean isLoggedIn(Activity activity) {
        if (activity == null) return false;

        boolean hasToken = TokenManager.getInstance(activity).hasToken();
        Long userId = UserSession.getInstance(activity).getUserId();

        return hasToken && userId != null;
    }

    public static boolean requireLogin(Activity activity) {
        if (isLoggedIn(activity)) {
            return true;
        }

        Toast.makeText(
                activity,
                "Vui lòng đăng nhập để sử dụng chức năng này",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);

        return false;
    }

    public static boolean requireLogin(Activity activity, Class<?> nextActivity) {
        if (isLoggedIn(activity)) {
            return true;
        }

        Toast.makeText(
                activity,
                "Vui lòng đăng nhập để sử dụng chức năng này",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(activity, LoginActivity.class);

        if (nextActivity != null) {
            intent.putExtra(EXTRA_NEXT_ACTIVITY, nextActivity.getName());
        }

        activity.startActivity(intent);

        return false;
    }
}