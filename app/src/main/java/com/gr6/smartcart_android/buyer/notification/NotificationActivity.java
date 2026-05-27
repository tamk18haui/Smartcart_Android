package com.gr6.smartcart_android.buyer.notification;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.main.BuyerMainActivity;
import com.gr6.smartcart_android.buyer.notification.api.BuyerNotificationApiService;
import com.gr6.smartcart_android.buyer.notification.response.BuyerNotificationResponse;
import com.gr6.smartcart_android.buyer.order.OrderDetailActivity;
import com.gr6.smartcart_android.chat.ChatRoomActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.navigation.BuyerBottomNavHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends BaseActivity {

    private ImageView imgBack;
    private TextView txtMarkAllRead;
    private RecyclerView rcvNotifications;
    private LinearLayout layoutEmpty;

    private LinearLayout tabAll;
    private LinearLayout tabUnread;
    private LinearLayout tabOrder;
    private LinearLayout tabPromotion;

    private BuyerNotificationAdapter adapter;
    private BuyerNotificationApiService apiService;
    private final List<BuyerNotificationResponse> allItems = new ArrayList<>();
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        apiService = ApiClient.createService(this, BuyerNotificationApiService.class);

        initViews();
        setupRecyclerView();
        initEvents();

        // Giữ nguyên nút Thông báo ở thanh menu buyer phía dưới.
        BuyerBottomNavHelper.setup(this, BuyerBottomNavHelper.TAB_NOTIFICATION);
        loadNotifications();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        txtMarkAllRead = findViewById(R.id.txtMarkAllRead);
        rcvNotifications = findViewById(R.id.rcvNotifications);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        tabAll = findViewById(R.id.tabAll);
        tabUnread = findViewById(R.id.tabUnread);
        tabOrder = findViewById(R.id.tabOrder);
        tabPromotion = findViewById(R.id.tabPromotion);
    }

    private void setupRecyclerView() {
        adapter = new BuyerNotificationAdapter(new ArrayList<>(), this::handleNotificationClick);
        rcvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rcvNotifications.setAdapter(adapter);
    }

    private void initEvents() {
        // Nút trở về luôn quay về trang chủ buyer, không chỉ finish màn hình hiện tại.
        imgBack.setOnClickListener(v -> openBuyerHome());

        txtMarkAllRead.setOnClickListener(v -> apiService.markAllAsRead().enqueue(new Callback<BaseResponse<Object>>() {
            @Override
            public void onResponse(Call<BaseResponse<Object>> call, Response<BaseResponse<Object>> response) {
                loadNotifications();
            }

            @Override
            public void onFailure(Call<BaseResponse<Object>> call, Throwable t) {
                showToast("Không đánh dấu được thông báo");
            }
        }));

        tabAll.setOnClickListener(v -> applyFilter("ALL"));
        tabUnread.setOnClickListener(v -> applyFilter("UNREAD"));
        tabOrder.setOnClickListener(v -> applyFilter("ORDER"));
        tabPromotion.setOnClickListener(v -> applyFilter("PROMOTION"));
    }

    private void loadNotifications() {
        apiService.getNotifications().enqueue(new Callback<BaseResponse<List<BuyerNotificationResponse>>>() {
            @Override
            public void onResponse(
                    Call<BaseResponse<List<BuyerNotificationResponse>>> call,
                    Response<BaseResponse<List<BuyerNotificationResponse>>> response
            ) {
                allItems.clear();

                if (response.body() != null && response.body().getData() != null) {
                    allItems.addAll(response.body().getData());
                }

                applyFilter(currentFilter);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<BuyerNotificationResponse>>> call, Throwable t) {
                showToast("Không tải được thông báo");
                applyFilter(currentFilter);
            }
        });
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        applyTabUi();

        List<BuyerNotificationResponse> result = new ArrayList<>();

        for (BuyerNotificationResponse item : allItems) {
            if ("UNREAD".equals(filter) && item.isRead()) continue;
            if ("ORDER".equals(filter) && !"ORDER".equalsIgnoreCase(item.getType())) continue;
            if ("PROMOTION".equals(filter) && !"PROMOTION".equalsIgnoreCase(item.getType())) continue;
            result.add(item);
        }

        adapter.setItems(result);
        boolean empty = result.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rcvNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void applyTabUi() {
        styleTab(tabAll, "ALL".equals(currentFilter));
        styleTab(tabUnread, "UNREAD".equals(currentFilter));
        styleTab(tabOrder, "ORDER".equals(currentFilter));
        styleTab(tabPromotion, "PROMOTION".equals(currentFilter));
    }

    private void styleTab(LinearLayout tab, boolean selected) {
        if (tab == null) return;

        int bgColor = selected
                ? ContextCompat.getColor(this, R.color.brand_primary_light)
                : ContextCompat.getColor(this, R.color.surface);

        int strokeColor = selected
                ? ContextCompat.getColor(this, R.color.brand_primary)
                : ContextCompat.getColor(this, R.color.border);

        int textColor = selected
                ? ContextCompat.getColor(this, R.color.brand_primary)
                : ContextCompat.getColor(this, R.color.text_secondary);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(bgColor);
        drawable.setCornerRadius(dp(999));
        drawable.setStroke(dp(1), strokeColor);
        tab.setBackground(drawable);

        if (tab.getChildCount() > 0 && tab.getChildAt(0) instanceof TextView) {
            ((TextView) tab.getChildAt(0)).setTextColor(textColor);
        }
    }

    private void handleNotificationClick(BuyerNotificationResponse notification) {
        if (notification == null) return;

        Long notificationId = notification.getNotificationId();
        if (notificationId == null) {
            openTarget(notification);
            return;
        }

        apiService.markAsRead(notificationId).enqueue(new Callback<BaseResponse<BuyerNotificationResponse>>() {
            @Override
            public void onResponse(
                    Call<BaseResponse<BuyerNotificationResponse>> call,
                    Response<BaseResponse<BuyerNotificationResponse>> response
            ) {
                openTarget(notification);
            }

            @Override
            public void onFailure(Call<BaseResponse<BuyerNotificationResponse>> call, Throwable t) {
                openTarget(notification);
            }
        });
    }

    private void openTarget(BuyerNotificationResponse notification) {
        String routeKey = notification.getRouteKey();
        Long targetId = notification.getTargetId();

        if (("CHAT_ROOM".equals(routeKey) || "CHAT".equalsIgnoreCase(notification.getType()))
                && targetId != null) {
            Intent intent = new Intent(this, ChatRoomActivity.class);
            intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_ID, targetId);
            intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_NAME, readRouteParam(notification.getRouteParams(), "partnerName"));
            intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_AVATAR, readRouteParam(notification.getRouteParams(), "partnerAvatarUrl"));
            startActivity(intent);
            return;
        }

        if (("BUYER_ORDER_DETAIL".equals(routeKey)
                || "ORDER_DETAIL".equals(routeKey)
                || "SHOP_ORDER_DETAIL".equals(routeKey)
                || "ORDER".equalsIgnoreCase(notification.getType()))
                && targetId != null) {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra(OrderDetailActivity.EXTRA_SHOP_ORDER_ID, targetId);
            startActivity(intent);
            return;
        }

        showToast("Đã mở thông báo");
        loadNotifications();
    }

    private String readRouteParam(String rawJson, String key) {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            return "";
        }

        try {
            JSONObject object = new JSONObject(rawJson);
            return object.optString(key, "");
        } catch (Exception ignored) {
            return "";
        }
    }

    private void openBuyerHome() {
        Intent intent = new Intent(this, BuyerMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }
}


