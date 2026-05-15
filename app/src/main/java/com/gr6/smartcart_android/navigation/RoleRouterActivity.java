package com.gr6.smartcart_android.navigation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.Constants;

public class RoleRouterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!TokenManager.getInstance(this).hasToken()) {
            AppNavigator.openLogin(this);
            finish();
            return;
        }

        String role = UserSession.getInstance(this).getRole();

//        if (Constants.ROLE_BUYER.equals(role)) {
//            AppNavigator.openBuyerHome(this);
//            finish();
//            return;
//        }
//
//        if (Constants.ROLE_SELLER.equals(role)) {
//            // Sau này làm SellerMainActivity thì mở ở đây
//            // AppNavigator.openSellerHome(this);
//            AppNavigator.openBuyerHome(this);
//            finish();
//            return;
//        }

        AppNavigator.openLogin(this);
        finish();
    }
}