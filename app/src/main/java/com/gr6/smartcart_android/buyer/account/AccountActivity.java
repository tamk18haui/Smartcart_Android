package com.gr6.smartcart_android.buyer.account;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.auth.login.LoginActivity;
import com.gr6.smartcart_android.buyer.account.profile.ProfileActivity;
import com.gr6.smartcart_android.buyer.account.profile.repository.ProfileRepository;
import com.gr6.smartcart_android.buyer.account.profile.response.ProfileResponse;
import com.gr6.smartcart_android.buyer.main.BuyerMainActivity;
import com.gr6.smartcart_android.buyer.notification.NotificationActivity;
import com.gr6.smartcart_android.buyer.notification.api.BuyerNotificationApiService;
import com.gr6.smartcart_android.buyer.order.OrderHistoryActivity;
import com.gr6.smartcart_android.buyer.support.SupportCenterActivity;
import com.gr6.smartcart_android.chat.ChatListActivity;
import com.gr6.smartcart_android.chat.api.ChatApiService;
import com.gr6.smartcart_android.chat.response.ConversationResponse;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.navigation.BuyerBottomNavHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountActivity extends BaseActivity {

    private SwipeRefreshLayout swipeAccount;

    private ProfileRepository profileRepository;
    private BuyerNotificationApiService notificationApiService;
    private ChatApiService chatApiService;

    private ImageView imgBack;
    private ImageView imgAvatar;

    private TextView txtFullName;
    private TextView txtEmail;
    private TextView txtUserCode;
    private TextView txtRole;
    private TextView txtChatBadge;
    private TextView txtNotificationBadge;

    private LinearLayout itemOrders;
    private LinearLayout itemPendingOrders;
    private LinearLayout itemShippingOrders;
    private LinearLayout itemReviewOrders;
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
        notificationApiService = ApiClient.createService(this, BuyerNotificationApiService.class);
        chatApiService = ApiClient.createService(this, ChatApiService.class);

        bindUserInfo();
        initEvents();
        loadProfileFromServer(true);
        loadBadges();

        BuyerBottomNavHelper.setup(this, BuyerBottomNavHelper.TAB_ACCOUNT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindUserInfo();
        loadBadges();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        imgAvatar = findViewById(R.id.imgAvatar);

        txtFullName = findViewById(R.id.txtFullName);
        txtEmail = findViewById(R.id.txtEmail);
        txtUserCode = findViewById(R.id.txtUserCode);
        txtRole = findViewById(R.id.txtRole);
        txtChatBadge = findViewById(R.id.txtChatBadge);
        txtNotificationBadge = findViewById(R.id.txtNotificationBadge);

        itemOrders = findViewById(R.id.itemOrders);
        itemPendingOrders = findViewById(R.id.itemPendingOrders);
        itemShippingOrders = findViewById(R.id.itemShippingOrders);
        itemReviewOrders = findViewById(R.id.itemReviewOrders);
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

    private void initEvents() {
        setupSwipeRefresh(swipeAccount, () -> {
            loadProfileFromServer(false);
            loadBadges();
        });

        imgBack.setOnClickListener(v -> openHome());

        itemOrders.setOnClickListener(v -> openOrderHistory());

        itemPendingOrders.setOnClickListener(v ->
                openOrderHistory(OrderHistoryActivity.TAB_PENDING)
        );

        itemShippingOrders.setOnClickListener(v ->
                openOrderHistory(OrderHistoryActivity.TAB_SHIPPING)
        );

        itemReviewOrders.setOnClickListener(v ->
                openOrderHistory(OrderHistoryActivity.TAB_REVIEW)
        );

        itemOrderHistory.setOnClickListener(v -> openOrderHistory());

        itemAddress.setOnClickListener(v -> openAddress());

        if (itemVoucher != null) {
            itemVoucher.setOnClickListener(v ->
                    showToast("Kho voucher đã được ẩn khỏi tài khoản buyer")
            );
        }

        itemChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatListActivity.class);
            startActivity(intent);
        });

        itemNotification.setOnClickListener(v -> openNotification());

        itemChangePassword.setOnClickListener(v ->
                showToast("Đổi mật khẩu sẽ làm ở bước sau")
        );

        itemSupport.setOnClickListener(v ->
                startActivity(new Intent(this, SupportCenterActivity.class))
        );

        itemAbout.setOnClickListener(v -> openHome());

        itemLogout.setOnClickListener(v -> logout());

        imgAvatar.setOnClickListener(v -> openProfile());
        txtFullName.setOnClickListener(v -> openProfile());
        txtEmail.setOnClickListener(v -> openProfile());
        txtUserCode.setOnClickListener(v -> openProfile());
        txtRole.setOnClickListener(v -> openProfile());
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
                    session.saveUserId(profile.getUserId());
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
            txtFullName.setText(fullName.trim());
        }

        if (email == null || email.trim().isEmpty()) {
            txtEmail.setText("Chưa có email");
        } else {
            txtEmail.setText(email.trim());
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

    private void loadBadges() {
        if (!TokenManager.getInstance(this).hasToken()) {
            renderBadge(txtNotificationBadge, 0);
            renderBadge(txtChatBadge, 0);
            return;
        }

        loadNotificationBadge();
        loadChatBadge();
    }

    private void loadNotificationBadge() {
        notificationApiService.getUnreadCount().enqueue(new Callback<BaseResponse<Long>>() {
            @Override
            public void onResponse(
                    Call<BaseResponse<Long>> call,
                    Response<BaseResponse<Long>> response
            ) {
                long count = 0L;

                if (response.body() != null && response.body().getData() != null) {
                    count = response.body().getData();
                }

                renderBadge(txtNotificationBadge, count);
            }

            @Override
            public void onFailure(Call<BaseResponse<Long>> call, Throwable t) {
                renderBadge(txtNotificationBadge, 0);
            }
        });
    }

    private void loadChatBadge() {
        chatApiService.getConversations().enqueue(new Callback<BaseResponse<List<ConversationResponse>>>() {
            @Override
            public void onResponse(
                    Call<BaseResponse<List<ConversationResponse>>> call,
                    Response<BaseResponse<List<ConversationResponse>>> response
            ) {
                int totalUnread = 0;

                if (response.body() != null && response.body().getData() != null) {
                    for (ConversationResponse conversation : response.body().getData()) {
                        if (conversation != null) {
                            totalUnread += Math.max(conversation.getUnreadCount(), 0);
                        }
                    }
                }

                renderBadge(txtChatBadge, totalUnread);
            }

            @Override
            public void onFailure(
                    Call<BaseResponse<List<ConversationResponse>>> call,
                    Throwable t
            ) {
                renderBadge(txtChatBadge, 0);
            }
        });
    }

    private void renderBadge(TextView badge, long count) {
        if (badge == null) {
            return;
        }

        if (count <= 0) {
            badge.setVisibility(View.GONE);
            return;
        }

        badge.setVisibility(View.VISIBLE);
        badge.setText(count > 99 ? "99+" : String.valueOf(count));
    }

    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void openOrderHistory() {
        openOrderHistory(OrderHistoryActivity.TAB_ALL);
    }

    private void openOrderHistory(String initialTab) {
        Intent intent = new Intent(this, OrderHistoryActivity.class);
        intent.putExtra(OrderHistoryActivity.EXTRA_INITIAL_TAB, initialTab);
        startActivity(intent);
    }

    private void openHome() {
        Intent intent = new Intent(this, BuyerMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    private void openNotification() {
        Intent intent = new Intent(this, NotificationActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
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


