package com.gr6.smartcart_android;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.gr6.smartcart_android.buyer.main.BuyerMainActivity;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.navigation.RoleRouterActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean hasToken = TokenManager.getInstance(this).hasToken();
        Long userId = UserSession.getInstance(this).getUserId();
        String role = UserSession.getInstance(this).getRole();

        Intent intent;

        if (hasToken && userId != null && role != null && !role.trim().isEmpty()) {
            intent = new Intent(this, RoleRouterActivity.class);
        } else {
            // Chưa đăng nhập thì vẫn vào trang chủ người mua như yêu cầu trước đó
            intent = new Intent(this, BuyerMainActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}