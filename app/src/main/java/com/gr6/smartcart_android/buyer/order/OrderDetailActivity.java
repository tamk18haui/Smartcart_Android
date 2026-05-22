package com.gr6.smartcart_android.buyer.order;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.order.response.OrderDetailResponse;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderDetailActivity extends BaseActivity {

    public static final String EXTRA_SHOP_ORDER_ID = "shop_order_id";

    private TextView txtOrderCode;
    private TextView txtShopName;
    private TextView txtStatus;
    private TextView txtCreatedAt;
    private TextView txtPaymentMethod;
    private TextView txtReceiverName;
    private TextView txtShippingAddress;
    private TextView txtItemCount;
    private TextView txtShippingFee;
    private TextView txtDiscountAmount;
    private TextView txtTotalAmount;
    private TextView btnCancelOrder;
    private TextView txtEmpty;

    private View layoutContent;
    private View layoutEmpty;
    private RecyclerView rcvItems;

    private OrderDetailViewModel viewModel;
    private OrderDetailItemAdapter adapter;

    private Long shopOrderId;

    private final NumberFormat moneyFormat =
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        viewModel = new ViewModelProvider(this).get(OrderDetailViewModel.class);

        readIntent();
        initViews();
        setupRecyclerView();
        initEvents();
        observeData();

        viewModel.loadOrderDetail(shopOrderId);
    }

    private void readIntent() {
        shopOrderId = getIntent().getLongExtra(EXTRA_SHOP_ORDER_ID, -1L);

        if (shopOrderId == -1L) {
            shopOrderId = null;
        }
    }

    private void initViews() {
        txtOrderCode = findViewById(R.id.txtOrderCode);
        txtShopName = findViewById(R.id.txtShopName);
        txtStatus = findViewById(R.id.txtStatus);
        txtCreatedAt = findViewById(R.id.txtCreatedAt);
        txtReceiverName = findViewById(R.id.txtReceiverName);
        txtShippingAddress = findViewById(R.id.txtShippingAddress);
        txtItemCount = findViewById(R.id.txtItemCount);
        txtShippingFee = findViewById(R.id.txtShippingFee);
        txtDiscountAmount = findViewById(R.id.txtDiscountAmount);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtPaymentMethod = findViewById(R.id.txtPaymentMethod);

        layoutContent = findViewById(R.id.layoutContent);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        rcvItems = findViewById(R.id.rcvItems);
    }

    private void setupRecyclerView() {
        adapter = new OrderDetailItemAdapter();

        rcvItems.setLayoutManager(new LinearLayoutManager(this));
        rcvItems.setAdapter(adapter);
        rcvItems.setNestedScrollingEnabled(false);
    }

    private void initEvents() {
        findViewById(R.id.imgBack).setOnClickListener(v -> finish());

        btnCancelOrder.setOnClickListener(v ->
                showToast("Muốn hủy đơn thì quay lại danh sách đơn hàng")
        );
    }

    private void observeData() {
        viewModel.getDetailState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                bindOrderDetail(state.getData());
            } else {
                showEmpty(state.getMessage());
            }
        });
    }

    private void bindOrderDetail(OrderDetailResponse detail) {
        if (detail == null) {
            showEmpty("Không có dữ liệu chi tiết đơn hàng");
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);

        txtOrderCode.setText(detail.getOrderCode());
        txtShopName.setText(detail.getShopName());
        txtStatus.setText(formatStatus(detail.getStatus()));
        txtCreatedAt.setText(formatDate(detail.getCreatedAt()));

        txtReceiverName.setText(detail.getReceiverName());
        txtShippingAddress.setText(detail.getShippingAddress());
        txtPaymentMethod.setText(detail.getPaymentText());

        txtItemCount.setText(detail.getItemCount() + " sản phẩm");
        txtShippingFee.setText("Phí vận chuyển: " + formatMoney(detail.getShippingFee()));
        txtDiscountAmount.setText("Giảm giá: -" + formatMoney(detail.getDiscountAmount()));
        txtTotalAmount.setText(formatMoney(detail.getTotalAmount()));

        btnCancelOrder.setVisibility(detail.canCancel() ? View.VISIBLE : View.GONE);

        adapter.setData(detail.getItems());
    }

    private void showEmpty(String message) {
        layoutContent.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);

        if (message == null || message.trim().isEmpty()) {
            message = "Không tải được chi tiết đơn hàng";
        }

        txtEmpty.setText(message);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        return moneyFormat.format(value);
    }

    private String formatDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Không rõ thời gian";
        }

        String result = value.trim().replace("T", " ");

        if (result.length() >= 16) {
            result = result.substring(0, 16);
        }

        return result;
    }

    private String formatStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Không xác định";
        }

        switch (status.trim().toUpperCase()) {
            case "PENDING_PAYMENT":
                return "Chờ thanh toán";
            case "PENDING":
                return "Chờ xác nhận";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "PREPARING":
                return "Đang chuẩn bị hàng";
            case "SHIPPING":
                return "Đang giao hàng";
            case "DELIVERED":
                return "Đã giao hàng";
            case "COMPLETED":
                return "Hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            case "PAYMENT_FAILED":
                return "Thanh toán thất bại";
            default:
                return status;
        }
    }
}