package com.gr6.smartcart_android.seller.notification;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.chat.ChatListActivity;
import com.gr6.smartcart_android.chat.ChatRoomActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.seller.notification.api.SellerNotificationApiService;
import com.gr6.smartcart_android.seller.notification.response.SellerNotificationResponse;
import com.gr6.smartcart_android.seller.order.SellerOrderDetailActivity;
import com.gr6.smartcart_android.seller.review.SellerProductReviewsActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerNotificationActivity extends BaseActivity {

    private ImageView imgBack;
    private TextView txtMarkAllRead;
    private RecyclerView rcvNotifications;
    private LinearLayout layoutEmpty;

    private SellerNotificationAdapter adapter;
    private SellerNotificationApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_notifications);

        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        apiService = ApiClient.createService(this, SellerNotificationApiService.class);

        initViews();
        setupRecyclerView();
        initEvents();
        loadNotifications();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        txtMarkAllRead = findViewById(R.id.txtMarkAllRead);
        rcvNotifications = findViewById(R.id.rcvNotifications);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        adapter = new SellerNotificationAdapter(new ArrayList<>(), this::handleNotificationClick);
        rcvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rcvNotifications.setAdapter(adapter);
    }

    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());

        txtMarkAllRead.setOnClickListener(v -> apiService.markAllAsRead().enqueue(new Callback<BaseResponse<Object>>() {
            @Override
            public void onResponse(Call<BaseResponse<Object>> call, Response<BaseResponse<Object>> response) {
                loadNotifications();
            }

            @Override
            public void onFailure(Call<BaseResponse<Object>> call, Throwable t) {
                Toast.makeText(SellerNotificationActivity.this, "Không đánh dấu được thông báo", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void loadNotifications() {
        apiService.getNotifications().enqueue(new Callback<BaseResponse<List<SellerNotificationResponse>>>() {
            @Override
            public void onResponse(
                    Call<BaseResponse<List<SellerNotificationResponse>>> call,
                    Response<BaseResponse<List<SellerNotificationResponse>>> response
            ) {
                List<SellerNotificationResponse> data = null;

                if (response.body() != null) {
                    data = response.body().getData();
                }

                if (data == null) {
                    data = new ArrayList<>();
                }

                adapter.setItems(data);
                layoutEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                rcvNotifications.setVisibility(data.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<SellerNotificationResponse>>> call, Throwable t) {
                Toast.makeText(SellerNotificationActivity.this, "Không tải được thông báo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleNotificationClick(SellerNotificationResponse notification) {
        if (notification == null) return;

        Long id = notification.getNotificationId();
        if (id != null) {
            apiService.markAsRead(id).enqueue(new Callback<BaseResponse<SellerNotificationResponse>>() {
                @Override
                public void onResponse(
                        Call<BaseResponse<SellerNotificationResponse>> call,
                        Response<BaseResponse<SellerNotificationResponse>> response
                ) {
                    openTarget(notification);
                }

                @Override
                public void onFailure(Call<BaseResponse<SellerNotificationResponse>> call, Throwable t) {
                    openTarget(notification);
                }
            });
        } else {
            openTarget(notification);
        }
    }

    private void openTarget(SellerNotificationResponse notification) {
        String routeKey = notification.getRouteKey();
        Long targetId = notification.getTargetId();

        if ("SELLER_ORDER_DETAIL".equals(routeKey) && targetId != null) {
            Intent intent = new Intent(this, SellerOrderDetailActivity.class);
            intent.putExtra(SellerOrderDetailActivity.EXTRA_ORDER_ID, targetId);
            startActivity(intent);
            return;
        }

        if ("CHAT_ROOM".equals(routeKey) && targetId != null) {
            Intent intent = new Intent(this, ChatRoomActivity.class);
            intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_ID, targetId);
            intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_NAME, readRouteParam(notification.getRouteParams(), "partnerName"));
            intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_AVATAR, readRouteParam(notification.getRouteParams(), "partnerAvatarUrl"));
            startActivity(intent);
            return;
        }

        if ("SELLER_MESSAGES".equals(routeKey) || "CHAT".equals(notification.getType())) {
            startActivity(new Intent(this, ChatListActivity.class));
            return;
        }

        if ("SELLER_PRODUCT_REVIEWS".equals(routeKey) && targetId != null) {
            Intent intent = new Intent(this, SellerProductReviewsActivity.class);
            intent.putExtra(SellerProductReviewsActivity.EXTRA_PRODUCT_ID, targetId);
            startActivity(intent);
            return;
        }

        if ("SELLER_ORDERS".equals(routeKey) || "ORDER".equals(notification.getType())) {
            Intent intent = new Intent(this, com.gr6.smartcart_android.seller.main.SellerHomeActivity.class);
            intent.putExtra("open_tab", "orders");
            startActivity(intent);
            return;
        }

        finish();
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
}


