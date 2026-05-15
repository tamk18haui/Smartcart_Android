package com.gr6.smartcart_android.navigation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.gr6.smartcart_android.auth.login.LoginActivity;
import com.gr6.smartcart_android.buyer.main.BuyerMainActivity;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.Constants;

public class RoleRouterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!TokenManager.getInstance(this).hasToken()) {
            openLogin();
            return;
        }

        String role = UserSession.getInstance(this).getRole();

        if (Constants.ROLE_BUYER.equals(role)) {
            openBuyerHome();
            return;
        }

        if (Constants.ROLE_SELLER.equals(role)) {
            openBuyerHome();
            return;
        }

        if (Constants.ROLE_ADMIN.equals(role)) {
            openBuyerHome();
            return;
        }

        // Có token nhưng role chưa lưu được thì vẫn cho vào Home tạm.
        openBuyerHome();
    }

    private void openBuyerHome() {
        Intent intent = new Intent(this, BuyerMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}