package com.gr6.smartcart_android;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.gr6.smartcart_android.auth.login.LoginActivity;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.navigation.RoleRouterActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean hasToken = TokenManager.getInstance(this).hasToken();
        Long userId = UserSession.getInstance(this).getUserId();

        if (hasToken && userId != null) {
            Intent intent = new Intent(this, RoleRouterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            TokenManager.getInstance(this).clearAll();
            UserSession.getInstance(this).clear();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        finish();
    }
}