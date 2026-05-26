package com.gr6.smartcart_android.navigation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.gr6.smartcart_android.auth.login.LoginActivity;
import com.gr6.smartcart_android.buyer.main.BuyerMainActivity;
import com.gr6.smartcart_android.common.notification.NotificationHelper;
import com.gr6.smartcart_android.common.repository.FcmTokenRepository;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.Constants;
import com.gr6.smartcart_android.seller.main.SellerHomeActivity;

public class RoleRouterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationHelper.createChannel(this);
        requestNotificationPermissionIfNeeded();

        if (!TokenManager.getInstance(this).hasToken()) {
            openLogin();
            return;
        }

        FcmTokenRepository.getInstance(this).refreshAndSendTokenWithRetry();

        String role = UserSession.getInstance(this).getRole();

        if (role == null || role.trim().isEmpty()) {
            openLogin();
            return;
        }

        role = role.trim().toUpperCase();

        if (Constants.ROLE_BUYER.equals(role) || "BUYER".equals(role) || "ROLE_BUYER".equals(role)) {
            openBuyerHome();
            return;
        }

        if (Constants.ROLE_SELLER.equals(role) || "SELLER".equals(role) || "ROLE_SELLER".equals(role)) {
            openSellerHome();
            return;
        }

        if (Constants.ROLE_ADMIN.equals(role) || "ADMIN".equals(role) || "ROLE_ADMIN".equals(role)) {
            openBuyerHome();
            return;
        }

        // Role không hợp lệ thì bắt đăng nhập lại, không fallback về BUYER nữa.
        openLogin();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1001
            );
        }
    }

    private void openBuyerHome() {
        Intent intent = new Intent(this, BuyerMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openSellerHome() {
        Intent intent = new Intent(this, SellerHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openLogin() {
        TokenManager.getInstance(this).clearAll();
        UserSession.getInstance(this).clear();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}


