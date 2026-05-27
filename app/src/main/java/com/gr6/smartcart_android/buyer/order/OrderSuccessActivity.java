package com.gr6.smartcart_android.buyer.order;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.main.BuyerMainActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderSuccessActivity extends BaseActivity {

    public static final String EXTRA_SUCCESS = "success";
    public static final String EXTRA_ORDER_ID = "orderId";
    public static final String EXTRA_SHOP_ORDER_ID = "shopOrderId";
    public static final String EXTRA_TOTAL_AMOUNT = "totalAmount";
    public static final String EXTRA_PAYMENT_METHOD = "paymentMethod";
    public static final String EXTRA_PAYMENT_PROVIDER = "paymentProvider";
    public static final String EXTRA_PAYMENT_STATUS = "paymentStatus";
    public static final String EXTRA_ORDER_STATUS = "orderStatus";
    public static final String EXTRA_MESSAGE = "message";

    private TextView imgClose;
    private TextView txtIcon;
    private TextView txtTitle;
    private TextView txtSubtitle;
    private TextView txtBadge;
    private TextView txtOrderCode;
    private TextView txtPaymentMethod;
    private TextView txtTotalAmount;
    private TextView btnViewOrder;
    private TextView btnContinueShopping;

    private boolean success = true;
    private Long orderId;
    private Long shopOrderId;
    private long totalAmount;
    private String paymentMethod;
    private String paymentProvider;
    private String paymentStatus;
    private String orderStatus;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        initViews();
        readData(getIntent());
        bindData();
        initEvents();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        readData(intent);
        bindData();
    }

    private void initViews() {
        imgClose = findViewById(R.id.imgClose);
        txtIcon = findViewById(R.id.txtIcon);
        txtTitle = findViewById(R.id.txtTitle);
        txtSubtitle = findViewById(R.id.txtSubtitle);
        txtBadge = findViewById(R.id.txtBadge);
        txtOrderCode = findViewById(R.id.txtOrderCode);
        txtPaymentMethod = findViewById(R.id.txtPaymentMethod);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        btnViewOrder = findViewById(R.id.btnViewOrder);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
    }

    private void readData(Intent intent) {
        success = true;
        orderId = null;
        shopOrderId = null;
        totalAmount = 0L;
        paymentMethod = "";
        paymentProvider = "";
        paymentStatus = "";
        orderStatus = "";
        message = "";

        if (intent == null) return;

        Uri data = intent.getData();

        if (data != null) {
            success = Boolean.parseBoolean(data.getQueryParameter("success"));

            orderId = parseLong(data.getQueryParameter("orderId"));
            shopOrderId = parseLong(data.getQueryParameter("shopOrderId"));

            Long amount = parseLong(data.getQueryParameter("totalAmount"));
            totalAmount = amount == null ? 0L : amount;

            paymentMethod = safe(data.getQueryParameter("paymentMethod"));

            paymentProvider = safe(data.getQueryParameter("paymentProvider"));

            if (isEmpty(paymentProvider)) {
                paymentProvider = safe(data.getQueryParameter("provider"));
            }

            paymentStatus = safe(data.getQueryParameter("paymentStatus"));
            orderStatus = safe(data.getQueryParameter("orderStatus"));
            message = safe(data.getQueryParameter("message"));

            return;
        }

        success = intent.getBooleanExtra(EXTRA_SUCCESS, true);

        if (intent.hasExtra(EXTRA_ORDER_ID)) {
            orderId = intent.getLongExtra(EXTRA_ORDER_ID, -1L);
            if (orderId <= 0) orderId = null;
        }

        if (intent.hasExtra(EXTRA_SHOP_ORDER_ID)) {
            shopOrderId = intent.getLongExtra(EXTRA_SHOP_ORDER_ID, -1L);
            if (shopOrderId <= 0) shopOrderId = null;
        }

        totalAmount = intent.getLongExtra(EXTRA_TOTAL_AMOUNT, 0L);
        paymentMethod = safe(intent.getStringExtra(EXTRA_PAYMENT_METHOD));
        paymentProvider = safe(intent.getStringExtra(EXTRA_PAYMENT_PROVIDER));
        paymentStatus = safe(intent.getStringExtra(EXTRA_PAYMENT_STATUS));
        orderStatus = safe(intent.getStringExtra(EXTRA_ORDER_STATUS));
        message = safe(intent.getStringExtra(EXTRA_MESSAGE));
    }

    private void bindData() {
        if (success) {
            txtIcon.setText("✓");
            txtTitle.setText("Cảm ơn bạn đã đặt hàng!");
            txtSubtitle.setText("Đơn hàng của bạn đã được tiếp nhận\nvà đang được xử lý.");
            txtBadge.setText("Đang xử lý");
        } else {
            txtIcon.setText("!");
            txtTitle.setText("Thanh toán thất bại");
            txtSubtitle.setText(isEmpty(message)
                    ? "Giao dịch chưa hoàn tất. Bạn có thể thanh toán lại trong lịch sử đơn hàng."
                    : message);
            txtBadge.setText("Thất bại");
        }

        if (orderId == null) {
            txtOrderCode.setText("#ORD--");
        } else {
            txtOrderCode.setText("#ORD-" + orderId);
        }

        txtPaymentMethod.setText(buildPaymentText());

        if (totalAmount > 0) {
            txtTotalAmount.setText(formatVnd(totalAmount));
        } else {
            txtTotalAmount.setText("--");
        }
    }

    private void initEvents() {
        imgClose.setOnClickListener(v -> openHome());
        btnContinueShopping.setOnClickListener(v -> openHome());
        btnViewOrder.setOnClickListener(v -> openOrder());
    }

    private void openOrder() {
        if (shopOrderId != null && shopOrderId > 0) {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra(OrderDetailActivity.EXTRA_SHOP_ORDER_ID, shopOrderId);
            startActivity(intent);
            finish();
            return;
        }

        Intent intent = new Intent(this, OrderHistoryActivity.class);
        startActivity(intent);
        finish();
    }

    private void openHome() {
        Intent intent = new Intent(this, BuyerMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    private String buildPaymentText() {
        if (!isEmpty(paymentProvider) && !"NONE".equalsIgnoreCase(paymentProvider)) {
            if ("VNPAY".equalsIgnoreCase(paymentProvider)) {
                return "VNPay";
            }

            if ("MOMO".equalsIgnoreCase(paymentProvider)) {
                return "MoMo";
            }

            return paymentProvider;
        }

        if ("COD".equalsIgnoreCase(paymentMethod)) {
            return "Thanh toán khi nhận hàng";
        }

        return "SmartCart Pay";
    }

    private String formatVnd(long value) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(value) + "đ";
    }

    private Long parseLong(String raw) {
        try {
            if (raw == null || raw.trim().isEmpty()) return null;

            long value = Long.parseLong(raw.trim());
            return value > 0 ? value : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}