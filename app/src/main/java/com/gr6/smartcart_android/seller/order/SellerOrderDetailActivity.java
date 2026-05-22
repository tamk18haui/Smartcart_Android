package com.gr6.smartcart_android.seller.order;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.chat.ChatRoomActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.seller.order.model.OrderDetailResponse;
import com.gr6.smartcart_android.seller.order.repository.SellerOrderRepository;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerOrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";

    private SellerOrderRepository repository;
    private OrderItemAdapter itemAdapter;
    private Long orderId;
    private OrderDetailResponse currentOrder;

    private TextView txtStatus;
    private TextView txtOrderCode;
    private TextView txtTime;
    private TextView txtReceiverName;
    private TextView txtReceiverPhone;
    private TextView txtAddress;
    private TextView txtTimeline1;
    private TextView txtTimeline2;
    private TextView txtTimeline3;
    private TextView txtTimeline4;
    private TextView txtSubtotal;
    private TextView txtShippingFee;
    private TextView txtTotal;
    private TextView btnContactBuyer;
    private TextView btnUpdateStatus;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_order_detail);

        repository = new SellerOrderRepository(this);
        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1);

        bindViews();
        setupRecyclerView();
        setupEvents();

        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOrderDetail();
    }

    private void bindViews() {
        txtStatus = findViewById(R.id.txtStatus);
        txtOrderCode = findViewById(R.id.txtOrderCode);
        txtTime = findViewById(R.id.txtTime);
        txtReceiverName = findViewById(R.id.txtReceiverName);
        txtReceiverPhone = findViewById(R.id.txtReceiverPhone);
        txtAddress = findViewById(R.id.txtAddress);
        txtTimeline1 = findViewById(R.id.txtTimeline1);
        txtTimeline2 = findViewById(R.id.txtTimeline2);
        txtTimeline3 = findViewById(R.id.txtTimeline3);
        txtTimeline4 = findViewById(R.id.txtTimeline4);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtShippingFee = findViewById(R.id.txtShippingFee);
        txtTotal = findViewById(R.id.txtTotal);
        btnContactBuyer = findViewById(R.id.btnContactBuyer);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
    }

    private void setupRecyclerView() {
        RecyclerView rvItems = findViewById(R.id.rvOrderItems);
        itemAdapter = new OrderItemAdapter();
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(itemAdapter);
    }

    private void setupEvents() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnContactBuyer.setOnClickListener(v -> openBuyerChat());

        btnUpdateStatus.setOnClickListener(v -> {
            if (currentOrder == null) return;
            showUpdateStatusDialog();
        });
    }

    private void loadOrderDetail() {
        repository.getOrderDetail(orderId).enqueue(new Callback<BaseResponse<OrderDetailResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<OrderDetailResponse>> call,
                                   @NonNull Response<BaseResponse<OrderDetailResponse>> response) {
                BaseResponse<OrderDetailResponse> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    Toast.makeText(SellerOrderDetailActivity.this,
                            body == null ? "Không lấy được chi tiết đơn hàng" : body.getSafeMessage(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                currentOrder = body.getData();
                renderOrder();
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<OrderDetailResponse>> call, @NonNull Throwable t) {
                Toast.makeText(SellerOrderDetailActivity.this,
                        "Không kết nối được server: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderOrder() {
        txtStatus.setText(OrderStatusHelper.label(currentOrder.getStatus()));
        txtOrderCode.setText(currentOrder.getOrderCode());
        txtTime.setText(currentOrder.getCreatedAt() == null ? "" : currentOrder.getCreatedAt());

        txtReceiverName.setText(currentOrder.getReceiverName());
        txtReceiverPhone.setText(currentOrder.getReceiverPhone());
        txtAddress.setText(currentOrder.getShippingAddress());

        itemAdapter.submitList(currentOrder.getItems());

        BigDecimal subtotal = currentOrder.getSubtotalAmount();
        if (subtotal == null) {
            subtotal = currentOrder.getTotalAmount();
        }

        BigDecimal shipping = currentOrder.getShippingFee();
        if (shipping == null) {
            shipping = BigDecimal.ZERO;
        }

        txtSubtotal.setText(formatMoney(subtotal));
        txtShippingFee.setText(formatMoney(shipping));
        txtTotal.setText(formatMoney(currentOrder.getTotalAmount()));

        renderTimeline(currentOrder.getStatus());

        String status = OrderStatusHelper.normalize(currentOrder.getStatus());
        boolean canUpdate = status.equals("PENDING") || status.equals("CONFIRMED") || status.equals("SHIPPING");
        btnUpdateStatus.setEnabled(canUpdate);
        btnUpdateStatus.setAlpha(canUpdate ? 1f : 0.45f);
    }

    private void renderTimeline(String status) {
        String s = OrderStatusHelper.normalize(status);
        txtTimeline1.setText("Đã đặt hàng");
        txtTimeline2.setText(s.equals("PENDING") ? "Chờ shop xác nhận" : "Shop đã xác nhận");

        if (s.equals("SHIPPING") || s.equals("DELIVERED") || s.equals("COMPLETED")) {
            txtTimeline3.setText("Đang giao");
        } else if (s.equals("CANCELLED")) {
            txtTimeline3.setText("Đã hủy");
        } else {
            txtTimeline3.setText("Chờ giao hàng");
        }

        if (s.equals("DELIVERED") || s.equals("COMPLETED")) {
            txtTimeline4.setText("Hoàn thành");
        } else if (s.equals("CANCELLED")) {
            txtTimeline4.setText("Đơn đã hủy");
        } else {
            txtTimeline4.setText("Chờ hoàn thành");
        }
    }

    private void openBuyerChat() {
        if (currentOrder == null) return;

        if (currentOrder.getBuyerId() == null) {
            Toast.makeText(this, "Backend chưa trả buyerId nên chưa mở được chat từ đơn hàng", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_ID, currentOrder.getBuyerId());
        intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_NAME, currentOrder.getBuyerName());
        startActivity(intent);
    }

    private void showUpdateStatusDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_order_status, null, false);
        RadioButton rbPending = dialogView.findViewById(R.id.rbPending);
        RadioButton rbConfirmed = dialogView.findViewById(R.id.rbConfirmed);
        RadioButton rbShipping = dialogView.findViewById(R.id.rbShipping);
        RadioButton rbDelivered = dialogView.findViewById(R.id.rbDelivered);
        RadioButton rbCancelled = dialogView.findViewById(R.id.rbCancelled);
        EditText edtCancelReason = dialogView.findViewById(R.id.edtCancelReason);

        String currentStatus = OrderStatusHelper.normalize(currentOrder.getStatus());

        rbPending.setEnabled(currentStatus.equals("PENDING"));
        rbConfirmed.setEnabled(currentStatus.equals("PENDING"));
        rbShipping.setEnabled(currentStatus.equals("CONFIRMED"));
        rbDelivered.setEnabled(currentStatus.equals("SHIPPING"));
        rbCancelled.setEnabled(currentStatus.equals("PENDING") || currentStatus.equals("CONFIRMED"));

        if (currentStatus.equals("PENDING")) rbPending.setChecked(true);
        if (currentStatus.equals("CONFIRMED")) rbConfirmed.setChecked(true);
        if (currentStatus.equals("SHIPPING")) rbShipping.setChecked(true);
        if (currentStatus.equals("DELIVERED") || currentStatus.equals("COMPLETED")) rbDelivered.setChecked(true);
        if (currentStatus.equals("CANCELLED")) rbCancelled.setChecked(true);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String selectedStatus = currentStatus;

            if (rbConfirmed.isChecked()) selectedStatus = "CONFIRMED";
            if (rbShipping.isChecked()) selectedStatus = "SHIPPING";
            if (rbDelivered.isChecked()) selectedStatus = "DELIVERED";
            if (rbCancelled.isChecked()) selectedStatus = "CANCELLED";

            String reason = edtCancelReason.getText().toString().trim();

            if (selectedStatus.equals("CANCELLED") && reason.isEmpty()) {
                edtCancelReason.setError("Bắt buộc nhập lý do hủy");
                return;
            }

            if (selectedStatus.equals(currentStatus)) {
                dialog.dismiss();
                return;
            }

            updateStatus(selectedStatus, reason, dialog);
        });

        dialog.show();
    }

    private void updateStatus(String status, String reason, AlertDialog dialog) {
        repository.updateStatus(orderId, status, reason).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<String>> call,
                                   @NonNull Response<BaseResponse<String>> response) {
                BaseResponse<String> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    Toast.makeText(SellerOrderDetailActivity.this,
                            body == null ? "Cập nhật trạng thái thất bại" : body.getSafeMessage(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(SellerOrderDetailActivity.this, body.getSafeMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadOrderDetail();
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<String>> call, @NonNull Throwable t) {
                Toast.makeText(SellerOrderDetailActivity.this,
                        "Không kết nối được server: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0đ";
        return moneyFormat.format(value) + "đ";
    }
}
