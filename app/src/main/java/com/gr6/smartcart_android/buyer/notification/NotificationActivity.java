package com.gr6.smartcart_android.buyer.notification;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

public class NotificationActivity extends BaseActivity {

    private ImageView imgBack;

    private LinearLayout tabAll;
    private LinearLayout tabUnread;
    private LinearLayout tabOrder;
    private LinearLayout tabPromotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        initViews();
        initEvents();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);

        tabAll = findViewById(R.id.tabAll);
        tabUnread = findViewById(R.id.tabUnread);
        tabOrder = findViewById(R.id.tabOrder);
        tabPromotion = findViewById(R.id.tabPromotion);
    }

    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());

        tabAll.setOnClickListener(v -> showToast("Tất cả thông báo"));
        tabUnread.setOnClickListener(v -> showToast("Thông báo chưa đọc"));
        tabOrder.setOnClickListener(v -> showToast("Thông báo đơn hàng"));
        tabPromotion.setOnClickListener(v -> showToast("Thông báo khuyến mãi"));
    }
}