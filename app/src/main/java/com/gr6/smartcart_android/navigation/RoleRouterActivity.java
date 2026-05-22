package com.gr6.smartcart_android.navigation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.gr6.smartcart_android.auth.login.LoginActivity;
import com.gr6.smartcart_android.buyer.main.BuyerMainActivity;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.Constants;
import com.gr6.smartcart_android.seller.main.SellerHomeActivity;

public class RoleRouterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!TokenManager.getInstance(this).hasToken()) {
            openLogin();
            return;
        }

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

        openBuyerHome();
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