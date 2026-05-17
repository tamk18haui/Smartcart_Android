package com.gr6.smartcart_android.buyer.account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.auth.login.LoginActivity;
import com.gr6.smartcart_android.buyer.order.OrderHistoryActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.navigation.BuyerBottomNavHelper;
import com.gr6.smartcart_android.buyer.account.profile.ProfileActivity;
import com.gr6.smartcart_android.buyer.account.profile.repository.ProfileRepository;
import com.gr6.smartcart_android.buyer.account.profile.response.ProfileResponse;
import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class AccountActivity extends BaseActivity {
    private SwipeRefreshLayout swipeAccount;

    private ProfileRepository profileRepository;
    private ImageView imgBack;
    private ImageView imgAvatar;

    private TextView txtFullName;
    private TextView txtEmail;
    private TextView txtUserCode;
    private TextView txtRole;

    private LinearLayout itemOrders;
    private LinearLayout itemOrderHistory;
    private LinearLayout itemAddress;
    private LinearLayout itemVoucher;
    private LinearLayout itemChat;
    private LinearLayout itemNotification;
    private LinearLayout itemChangePassword;
    private LinearLayout itemSupport;
    private LinearLayout itemAbout;
    private LinearLayout itemLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        initViews();
        profileRepository = new ProfileRepository(this);

        bindUserInfo();
        initEvents();
        loadProfileFromServer(true);
        BuyerBottomNavHelper.setup(this, BuyerBottomNavHelper.TAB_ACCOUNT);
    }
    private void loadProfileFromServer(boolean showFullLoading) {
        if (!TokenManager.getInstance(this).hasToken()) {
            bindUserInfo();
            stopSwipeRefresh(swipeAccount);
            return;
        }

        if (showFullLoading) {
            showLoading();
        }

        profileRepository.getProfile(new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(ProfileResponse profile, String message) {
                runOnUiThread(() -> {
                    hideLoading();
                    stopSwipeRefresh(swipeAccount);

                    if (profile == null) {
                        bindUserInfo();
                        return;
                    }

                    UserSession session = UserSession.getInstance(AccountActivity.this);
                    session.saveFullName(profile.getFullName());
                    session.saveEmail(profile.getEmail());
                    session.saveAvatarUrl(profile.getAvatarUrl());
                    session.saveRole(profile.getRole());
                    session.savePhoneNumber(profile.getPhoneNumber());

                    bindUserInfo();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    hideLoading();
                    stopSwipeRefresh(swipeAccount);
                    bindUserInfo();
                    android.util.Log.e("ACCOUNT_PROFILE", message);
                    showLongToast(message);
                });
            }
        });
    }
    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        imgAvatar = findViewById(R.id.imgAvatar);

        txtFullName = findViewById(R.id.txtFullName);
        txtEmail = findViewById(R.id.txtEmail);
        txtUserCode = findViewById(R.id.txtUserCode);
        txtRole = findViewById(R.id.txtRole);

        itemOrders = findViewById(R.id.itemOrders);
        itemOrderHistory = findViewById(R.id.itemOrderHistory);
        itemAddress = findViewById(R.id.itemAddress);
        itemVoucher = findViewById(R.id.itemVoucher);
        itemChat = findViewById(R.id.itemChat);
        itemNotification = findViewById(R.id.itemNotification);
        itemChangePassword = findViewById(R.id.itemChangePassword);
        itemSupport = findViewById(R.id.itemSupport);
        itemAbout = findViewById(R.id.itemAbout);
        itemLogout = findViewById(R.id.itemLogout);
        swipeAccount = findViewById(R.id.swipeAccount);
    }

    private void bindUserInfo() {
        UserSession session = UserSession.getInstance(this);

        String fullName = session.getFullName();
        String email = session.getEmail();
        String avatarUrl = session.getAvatarUrl();
        String role = session.getRole();
        Long userId = session.getUserId();

        if (fullName == null || fullName.trim().isEmpty()) {
            txtFullName.setText("Người dùng SmartCart");
        } else {
            txtFullName.setText(fullName);
        }

        if (email == null || email.trim().isEmpty()) {
            txtEmail.setText("Chưa có email");
        } else {
            txtEmail.setText(email);
        }

        if (userId == null) {
            txtUserCode.setText("ID: --");
        } else {
            txtUserCode.setText("ID: SC-" + userId);
        }

        if (role == null || role.trim().isEmpty()) {
            txtRole.setText("BUYER");
        } else {
            txtRole.setText(role.trim().toUpperCase());
        }

        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            imgAvatar.setPadding(dp(16), dp(16), dp(16), dp(16));

            ImageViewCompat.setImageTintList(
                    imgAvatar,
                    ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.brand_primary)
                    )
            );

            imgAvatar.setImageResource(R.drawable.ic_user);
        } else {
            imgAvatar.setPadding(0, 0, 0, 0);
            ImageViewCompat.setImageTintList(imgAvatar, null);

            ImageLoader.loadCircle(this, avatarUrl.trim(), imgAvatar);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindUserInfo();
    }
    private void initEvents() {
        setupSwipeRefresh(swipeAccount, () -> loadProfileFromServer(false));
        imgBack.setOnClickListener(v -> finish());

        itemOrders.setOnClickListener(v -> openOrderHistory());

        itemOrderHistory.setOnClickListener(v -> openOrderHistory());

        itemAddress.setOnClickListener(v -> openAddress());

        itemVoucher.setOnClickListener(v ->
                showToast("Kho voucher sẽ làm ở bước sau")
        );

        itemChat.setOnClickListener(v ->
                showToast("Tin nhắn sẽ làm ở bước sau")
        );

        itemNotification.setOnClickListener(v ->
                showToast("Thông báo sẽ làm ở bước sau")
        );

        itemChangePassword.setOnClickListener(v ->
                showToast("Đổi mật khẩu sẽ làm ở bước sau")
        );

        itemSupport.setOnClickListener(v ->
                showToast("Trung tâm hỗ trợ sẽ làm ở bước sau")
        );

        itemAbout.setOnClickListener(v ->
                showToast("SmartCart - ứng dụng mua sắm thông minh")
        );

        itemLogout.setOnClickListener(v -> logout());
        imgAvatar.setOnClickListener(v -> openProfile());
        txtFullName.setOnClickListener(v -> openProfile());
        txtEmail.setOnClickListener(v -> openProfile());
        txtUserCode.setOnClickListener(v -> openProfile());
        txtRole.setOnClickListener(v -> openProfile());
    }
    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void openOrderHistory() {
        Intent intent = new Intent(this, OrderHistoryActivity.class);
        startActivity(intent);
    }

    private void openHome() {
        try {
            Class<?> homeClass = Class.forName(
                    "com.gr6.smartcart_android.buyer.main.BuyerMainActivity"
            );

            Intent intent = new Intent(this, homeClass);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            finish();
        }
    }

    private void openNotification() {
        Intent intent = new Intent(this, com.gr6.smartcart_android.buyer.notification.NotificationActivity.class);
        startActivity(intent);
    }
    private void openAddress() {
        try {
            Class<?> addressClass = Class.forName(
                    "com.gr6.smartcart_android.buyer.address.AddressActivity"
            );

            startActivity(new Intent(this, addressClass));
        } catch (Exception e) {
            showToast("Địa chỉ giao hàng sẽ làm ở bước sau");
        }
    }

    private void logout() {
        TokenManager.getInstance(this).clearAll();
        UserSession.getInstance(this).clear();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}