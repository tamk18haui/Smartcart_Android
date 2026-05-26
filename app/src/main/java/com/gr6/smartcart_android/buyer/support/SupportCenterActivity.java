package com.gr6.smartcart_android.buyer.support;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.main.BuyerMainActivity;
import com.gr6.smartcart_android.chat.ChatListActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

public class SupportCenterActivity extends BaseActivity {

    private ImageView imgBack;
    private LinearLayout itemSupportChat;
    private LinearLayout itemSupportOrder;
    private LinearLayout itemSupportPayment;
    private LinearLayout itemSupportSeller;
    private LinearLayout itemSupportEmail;
    private LinearLayout itemSupportHotline;
    private LinearLayout btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_center);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        bindViews();
        bindEvents();
    }

    private void bindViews() {
        imgBack = findViewById(R.id.imgBack);
        itemSupportChat = findViewById(R.id.itemSupportChat);
        itemSupportOrder = findViewById(R.id.itemSupportOrder);
        itemSupportPayment = findViewById(R.id.itemSupportPayment);
        itemSupportSeller = findViewById(R.id.itemSupportSeller);
        itemSupportEmail = findViewById(R.id.itemSupportEmail);
        itemSupportHotline = findViewById(R.id.itemSupportHotline);
        btnBackHome = findViewById(R.id.btnBackHome);
    }

    private void bindEvents() {
        imgBack.setOnClickListener(v -> finish());

        itemSupportChat.setOnClickListener(v -> startActivity(new Intent(this, ChatListActivity.class)));

        itemSupportOrder.setOnClickListener(v ->
                showToast("Vào Lịch sử đơn hàng, chọn đơn cần hỗ trợ rồi nhắn cho shop hoặc xem trạng thái xử lý.")
        );

        itemSupportPayment.setOnClickListener(v ->
                showToast("Thanh toán/hoàn tiền sẽ được SmartCart đối soát theo trạng thái đơn hàng.")
        );

        itemSupportSeller.setOnClickListener(v ->
                showToast("Seller cần chuẩn bị mã đơn, ảnh sản phẩm và mô tả lỗi để SmartCart hỗ trợ nhanh hơn.")
        );

        itemSupportEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@smartcart.vn"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Cần hỗ trợ SmartCart");
            try {
                startActivity(intent);
            } catch (Exception e) {
                showToast("Không mở được ứng dụng email");
            }
        });

        itemSupportHotline.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:19001009"));
            try {
                startActivity(intent);
            } catch (Exception e) {
                showToast("Không mở được trình gọi điện");
            }
        });

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, BuyerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
