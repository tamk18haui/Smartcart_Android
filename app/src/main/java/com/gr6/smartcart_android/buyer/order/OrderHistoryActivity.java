package com.gr6.smartcart_android.buyer.order;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

public class OrderHistoryActivity extends BaseActivity {

    private ImageView imgBack;

    private LinearLayout tabAll;
    private LinearLayout tabPending;
    private LinearLayout tabShipping;
    private LinearLayout tabCompleted;
    private LinearLayout tabCancelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        initViews();
        initEvents();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);

        tabAll = findViewById(R.id.tabAll);
        tabPending = findViewById(R.id.tabPending);
        tabShipping = findViewById(R.id.tabShipping);
        tabCompleted = findViewById(R.id.tabCompleted);
        tabCancelled = findViewById(R.id.tabCancelled);
    }

    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());

        tabAll.setOnClickListener(v ->
                showToast("Tất cả đơn hàng")
        );

        tabPending.setOnClickListener(v ->
                showToast("Đơn chờ xác nhận")
        );

        tabShipping.setOnClickListener(v ->
                showToast("Đơn đang giao")
        );

        tabCompleted.setOnClickListener(v ->
                showToast("Đơn đã hoàn thành")
        );

        tabCancelled.setOnClickListener(v ->
                showToast("Đơn đã hủy")
        );
    }
}