package com.gr6.smartcart_android.seller.order;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.seller.order.response.OrderDetailResponse;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class SellerOrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";

    private SellerOrderViewModel viewModel;
    private OrderItemAdapter itemAdapter;

    private Long orderId = -1L;
    private OrderDetailResponse currentOrder;
    private AlertDialog updateStatusDialog;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_seller_order_detail);
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được giao diện chi tiết đơn hàng", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(SellerOrderViewModel.class);
        orderId = readOrderId();

        bindViews();

        if (!validateViews()) {
            Toast.makeText(this, "Layout chi tiết đơn hàng đang thiếu ID view", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupRecyclerView();
        setupEvents();
        observeViewModel();

        if (orderId == null || orderId <= 0) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel.loadOrderDetail(orderId);
    }

    @Override
    protected void onDestroy() {
        if (updateStatusDialog != null && updateStatusDialog.isShowing()) {
            updateStatusDialog.dismiss();
        }
        super.onDestroy();
    }

    private Long readOrderId() {
        try {
            if (getIntent() == null || getIntent().getExtras() == null) {
                return -1L;
            }

            Object raw = getIntent().getExtras().get(EXTRA_ORDER_ID);

            if (raw instanceof Long) {
                return (Long) raw;
            }

            if (raw instanceof Integer) {
                return ((Integer) raw).longValue();
            }

            if (raw instanceof Number) {
                return ((Number) raw).longValue();
            }

            if (raw instanceof String) {
                String value = ((String) raw).trim();
                return value.isEmpty() ? -1L : Long.parseLong(value);
            }
        } catch (Exception ignored) {
        }

        return -1L;
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

    private boolean validateViews() {
        return txtStatus != null
                && txtOrderCode != null
                && txtTime != null
                && txtReceiverName != null
                && txtReceiverPhone != null
                && txtAddress != null
                && txtTimeline1 != null
                && txtTimeline2 != null
                && txtTimeline3 != null
                && txtTimeline4 != null
                && txtSubtotal != null
                && txtShippingFee != null
                && txtTotal != null
                && btnContactBuyer != null
                && btnUpdateStatus != null
                && findViewById(R.id.rvOrderItems) != null
                && findViewById(R.id.btnBack) != null;
    }

    private void setupRecyclerView() {
        RecyclerView rvItems = findViewById(R.id.rvOrderItems);

        itemAdapter = new OrderItemAdapter();

        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(itemAdapter);
        rvItems.setNestedScrollingEnabled(false);
    }

    private void setupEvents() {
        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnContactBuyer.setOnClickListener(v -> Toast.makeText(
                this,
                "Chức năng chat với người mua sẽ nối sau",
                Toast.LENGTH_SHORT
        ).show());

        btnUpdateStatus.setOnClickListener(v -> {
            if (currentOrder == null) {
                Toast.makeText(this, "Đang tải đơn hàng, vui lòng chờ", Toast.LENGTH_SHORT).show();
                return;
            }

            String status = OrderStatusHelper.normalize(currentOrder.getStatus());

            if (isFinalStatus(status)) {
                Toast.makeText(this, "Đơn hàng này không thể cập nhật thêm", Toast.LENGTH_SHORT).show();
                return;
            }

            showUpdateStatusDialog();
        });
    }

    private void observeViewModel() {
        viewModel.getOrderDetailState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                setActionButtonEnabled(false);
                return;
            }

            if (state.isError()) {
                setActionButtonEnabled(true);
                Toast.makeText(
                        this,
                        safeText(state.getMessage(), "Không tải được chi tiết đơn hàng"),
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            if (state.isSuccess()) {
                currentOrder = state.getOrder();

                try {
                    renderOrder();
                } catch (Exception e) {
                    Toast.makeText(
                            this,
                            "Lỗi hiển thị chi tiết đơn hàng: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });

        viewModel.getActionState().observe(this, state -> {
            if (state == null || state.isIdle()) return;

            if (state.isLoading()) {
                setActionButtonEnabled(false);
                return;
            }

            setActionButtonEnabled(true);

            if (state.isError()) {
                Toast.makeText(
                        this,
                        safeText(state.getMessage(), "Cập nhật trạng thái thất bại"),
                        Toast.LENGTH_LONG
                ).show();
                viewModel.resetActionState();
                return;
            }

            if (state.isSuccess()) {
                Toast.makeText(
                        this,
                        safeText(state.getMessage(), "Cập nhật trạng thái thành công"),
                        Toast.LENGTH_SHORT
                ).show();

                if (updateStatusDialog != null && updateStatusDialog.isShowing()) {
                    updateStatusDialog.dismiss();
                }

                viewModel.resetActionState();

                if (orderId != null && orderId > 0) {
                    viewModel.loadOrderDetail(orderId);
                }
            }
        });
    }

    private void renderOrder() {
        if (currentOrder == null) {
            Toast.makeText(this, "Không có dữ liệu chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String status = OrderStatusHelper.normalize(currentOrder.getStatus());

        txtStatus.setText(OrderStatusHelper.label(status));
        txtOrderCode.setText(safeText(currentOrder.getOrderCode(), "#ORD-" + orderId));
        txtTime.setText(formatTime(currentOrder.getCreatedAt()));

        txtReceiverName.setText("Người nhận: " + safeText(currentOrder.getReceiverName(), "Chưa có tên người nhận"));
        txtReceiverPhone.setText("SĐT: " + safeText(currentOrder.getReceiverPhone(), "Chưa có số điện thoại"));
        txtAddress.setText("Địa chỉ: " + safeText(currentOrder.getShippingAddress(), "Chưa có địa chỉ giao hàng"));

        if (itemAdapter != null) {
            itemAdapter.submitList(currentOrder.getItems());
        }

        BigDecimal subtotal = currentOrder.getSubtotalAmount();
        if (subtotal == null) {
            subtotal = currentOrder.getTotalAmount();
        }

        BigDecimal shipping = currentOrder.getShippingFee();
        if (shipping == null) {
            shipping = BigDecimal.ZERO;
        }

        txtSubtotal.setText("Tạm tính: " + formatMoney(subtotal));
        txtShippingFee.setText("Phí vận chuyển: " + formatMoney(shipping));
        txtTotal.setText("Tổng tiền: " + formatMoney(currentOrder.getTotalAmount()));

        renderTimeline(status);
        updateActionButton(status);
    }

    private void renderTimeline(String status) {
        txtTimeline1.setText("1. Đã đặt hàng");
        txtTimeline2.setText("2. Chờ shop xác nhận");
        txtTimeline3.setText("3. Chờ giao hàng");
        txtTimeline4.setText("4. Chờ hoàn thành");

        int step = getStep(status);

        txtTimeline1.setAlpha(step >= 1 ? 1f : 0.45f);
        txtTimeline2.setAlpha(step >= 2 ? 1f : 0.45f);
        txtTimeline3.setAlpha(step >= 3 ? 1f : 0.45f);
        txtTimeline4.setAlpha(step >= 4 ? 1f : 0.45f);

        if ("CONFIRMED".equals(status)) {
            txtTimeline2.setText("2. Shop đã xác nhận");
        } else if ("SHIPPING".equals(status)) {
            txtTimeline2.setText("2. Shop đã xác nhận");
            txtTimeline3.setText("3. Đang giao hàng");
        } else if ("DELIVERED".equals(status)) {
            txtTimeline2.setText("2. Shop đã xác nhận");
            txtTimeline3.setText("3. Đã giao hàng");
            txtTimeline4.setText("4. Chờ người mua hoàn tất");
        } else if ("COMPLETED".equals(status)) {
            txtTimeline2.setText("2. Shop đã xác nhận");
            txtTimeline3.setText("3. Đã giao hàng");
            txtTimeline4.setText("4. Đã hoàn thành");
        } else if ("CANCELLED".equals(status)) {
            txtTimeline4.setText("4. Đã hủy");
            txtTimeline1.setAlpha(1f);
            txtTimeline2.setAlpha(1f);
            txtTimeline3.setAlpha(1f);
            txtTimeline4.setAlpha(1f);
        }
    }

    private int getStep(String status) {
        switch (status) {
            case "PENDING":
                return 1;
            case "CONFIRMED":
                return 2;
            case "SHIPPING":
            case "DELIVERED":
                return 3;
            case "COMPLETED":
            case "CANCELLED":
                return 4;
            default:
                return 1;
        }
    }

    private void updateActionButton(String status) {
        if ("PENDING_PAYMENT".equals(status)) {
            setFinalButton("Chờ thanh toán");
            return;
        }

        if ("PAYMENT_FAILED".equals(status)) {
            setFinalButton("Thanh toán lỗi");
            return;
        }

        if ("DELIVERED".equals(status)) {
            setFinalButton("Chờ người mua hoàn tất");
            return;
        }

        if ("COMPLETED".equals(status)) {
            setFinalButton("Đã hoàn thành");
            return;
        }

        if ("CANCELLED".equals(status)) {
            setFinalButton("Đã hủy");
            return;
        }

        btnUpdateStatus.setText(OrderStatusHelper.nextActionLabel(status));
        setActionButtonEnabled(true);
    }

    private void setFinalButton(String text) {
        btnUpdateStatus.setText(text);
        btnUpdateStatus.setEnabled(false);
        btnUpdateStatus.setAlpha(0.55f);
    }

    private void setActionButtonEnabled(boolean enabled) {
        if (btnUpdateStatus == null) return;

        btnUpdateStatus.setEnabled(enabled);
        btnUpdateStatus.setAlpha(enabled ? 1f : 0.55f);
    }

    private void showUpdateStatusDialog() {
        if (currentOrder == null) return;

        View dialogView;

        try {
            dialogView = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_seller_update_order_status, null, false);
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được hộp thoại cập nhật trạng thái", Toast.LENGTH_LONG).show();
            return;
        }

        RadioButton rbPending = dialogView.findViewById(R.id.rbPending);
        RadioButton rbConfirmed = dialogView.findViewById(R.id.rbConfirmed);
        RadioButton rbShipping = dialogView.findViewById(R.id.rbShipping);
        RadioButton rbDelivered = findRadioButtonByAnyName(dialogView, "rbDelivered", "rbCompleted");
        RadioButton rbCancelled = dialogView.findViewById(R.id.rbCancelled);

        EditText edtCancelReason = dialogView.findViewById(R.id.edtCancelReason);
        TextView btnCancel = dialogView.findViewById(R.id.btnCancel);
        TextView btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        if (rbPending == null
                || rbConfirmed == null
                || rbShipping == null
                || rbDelivered == null
                || rbCancelled == null
                || edtCancelReason == null
                || btnCancel == null
                || btnConfirm == null) {
            Toast.makeText(this, "dialog_seller_update_order_status.xml đang thiếu ID", Toast.LENGTH_LONG).show();
            return;
        }

        rbDelivered.setText("Đã giao");

        String currentStatus = OrderStatusHelper.normalize(currentOrder.getStatus());

        rbPending.setEnabled(false);
        rbConfirmed.setEnabled("PENDING".equals(currentStatus));
        rbShipping.setEnabled("CONFIRMED".equals(currentStatus));
        rbDelivered.setEnabled("SHIPPING".equals(currentStatus));
        rbCancelled.setEnabled("PENDING".equals(currentStatus) || "CONFIRMED".equals(currentStatus));

        rbPending.setChecked(false);
        rbConfirmed.setChecked(false);
        rbShipping.setChecked(false);
        rbDelivered.setChecked(false);
        rbCancelled.setChecked(false);

        String suggested = OrderStatusHelper.nextStatus(currentStatus);

        if ("CONFIRMED".equals(suggested)) {
            rbConfirmed.setChecked(true);
        } else if ("SHIPPING".equals(suggested)) {
            rbShipping.setChecked(true);
        } else if ("DELIVERED".equals(suggested)) {
            rbDelivered.setChecked(true);
        }

        edtCancelReason.setVisibility(View.GONE);

        rbPending.setOnClickListener(v -> edtCancelReason.setVisibility(View.GONE));
        rbConfirmed.setOnClickListener(v -> edtCancelReason.setVisibility(View.GONE));
        rbShipping.setOnClickListener(v -> edtCancelReason.setVisibility(View.GONE));
        rbDelivered.setOnClickListener(v -> edtCancelReason.setVisibility(View.GONE));
        rbCancelled.setOnClickListener(v -> edtCancelReason.setVisibility(View.VISIBLE));

        updateStatusDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> updateStatusDialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String selectedStatus = getSelectedStatus(
                    rbConfirmed,
                    rbShipping,
                    rbDelivered,
                    rbCancelled
            );

            if (selectedStatus.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn trạng thái mới", Toast.LENGTH_SHORT).show();
                return;
            }

            String reason = edtCancelReason.getText() == null
                    ? ""
                    : edtCancelReason.getText().toString().trim();

            if ("CANCELLED".equals(selectedStatus) && reason.isEmpty()) {
                edtCancelReason.setError("Bắt buộc nhập lý do hủy");
                return;
            }

            if (selectedStatus.equals(currentStatus)) {
                updateStatusDialog.dismiss();
                return;
            }

            viewModel.updateStatus(orderId, selectedStatus, reason);
        });

        updateStatusDialog.show();
    }

    private RadioButton findRadioButtonByAnyName(View root, String... names) {
        if (root == null || names == null) return null;

        for (String name : names) {
            int id = getResources().getIdentifier(name, "id", getPackageName());
            if (id != 0) {
                View view = root.findViewById(id);
                if (view instanceof RadioButton) {
                    return (RadioButton) view;
                }
            }
        }

        return null;
    }

    private String getSelectedStatus(
            RadioButton rbConfirmed,
            RadioButton rbShipping,
            RadioButton rbDelivered,
            RadioButton rbCancelled
    ) {
        if (rbConfirmed.isChecked()) return "CONFIRMED";
        if (rbShipping.isChecked()) return "SHIPPING";
        if (rbDelivered.isChecked()) return "DELIVERED";
        if (rbCancelled.isChecked()) return "CANCELLED";
        return "";
    }

    private boolean isFinalStatus(String status) {
        return "DELIVERED".equals(status)
                || "COMPLETED".equals(status)
                || "CANCELLED".equals(status)
                || "PAYMENT_FAILED".equals(status);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0đ";
        return moneyFormat.format(value) + "đ";
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String formatTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "Không rõ thời gian";

        String value = raw.replace('T', ' ');

        int dotIndex = value.indexOf('.');
        if (dotIndex > 0) {
            value = value.substring(0, dotIndex);
        }

        if (value.length() >= 16) {
            return value.substring(0, 16);
        }

        return value;
    }
}