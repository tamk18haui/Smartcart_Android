package com.gr6.smartcart_android.buyer.order;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.checkout.CheckoutActivity;
import com.gr6.smartcart_android.buyer.checkout.CheckoutSelectedShop;
import com.gr6.smartcart_android.buyer.order.response.OrderHistoryResponse;
import com.gr6.smartcart_android.buyer.product.repository.ProductRepository;
import com.gr6.smartcart_android.buyer.product.repository.ProductRepository.ActionCallback;
import com.gr6.smartcart_android.buyer.review.ReviewActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends BaseActivity {

    public static final String EXTRA_INITIAL_TAB = "extra_initial_tab";

    public static final String TAB_ALL = "ALL";
    public static final String TAB_PENDING = "PENDING_GROUP";
    public static final String TAB_SHIPPING = "SHIPPING";
    public static final String TAB_COMPLETED = "COMPLETED_GROUP";
    public static final String TAB_CANCELLED = "CANCELLED_GROUP";
    public static final String TAB_REVIEW = "REVIEW_PENDING";
    private ProductRepository productRepository;

    private ImageView imgBack;
    private EditText edtSearchOrder;
    private TextView txtOrderCount;

    private LinearLayout tabAll;
    private LinearLayout tabPending;
    private LinearLayout tabShipping;
    private LinearLayout tabCompleted;
    private LinearLayout tabCancelled;

    private SwipeRefreshLayout swipeOrderHistory;
    private RecyclerView rcvOrders;
    private LinearLayout layoutEmpty;

    private OrderHistoryAdapter adapter;
    private OrderHistoryViewModel viewModel;

    private final List<OrderHistoryUiModel> allOrders = new ArrayList<>();

    private String currentTab = TAB_ALL;
    private String currentKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        viewModel = new ViewModelProvider(this).get(OrderHistoryViewModel.class);
        productRepository = new ProductRepository(this);

        initViews();          // BẮT BUỘC PHẢI CÓ DÒNG NÀY
        initRecyclerView();
        initEvents();
        readInitialTab();
        observeOrderHistory();
        observeCancelOrder();
        observeCompleteOrder();
        observeRetryPayment();

        viewModel.loadOrderHistory();
    }

    private void readInitialTab() {
        String initialTab = getIntent().getStringExtra(EXTRA_INITIAL_TAB);

        if (TAB_PENDING.equals(initialTab)
                || TAB_SHIPPING.equals(initialTab)
                || TAB_COMPLETED.equals(initialTab)
                || TAB_CANCELLED.equals(initialTab)
                || TAB_REVIEW.equals(initialTab)) {
            currentTab = initialTab;
        } else {
            currentTab = TAB_ALL;
        }

        applyTabUi();
    }

    private void observeCompleteOrder() {
        viewModel.getCompleteState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                showToast("Đã xác nhận hoàn thành đơn hàng");
                viewModel.loadOrderHistory();
            } else {
                showLongToast(state.getMessage());
            }
        });
    }

    private void observeRetryPayment() {
        viewModel.getRetryPaymentState().observe(this, response -> {
            if (response == null) return;

            String paymentUrl = response.getPaymentUrl();

            if (paymentUrl == null || paymentUrl.trim().isEmpty()) {
                showToast("Không có link thanh toán");
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl.trim()));
            startActivity(intent);
        });

        viewModel.getRetryPaymentErrorState().observe(this, message -> {
            if (message == null || message.trim().isEmpty()) return;
            showLongToast(message);
        });
    }

    private void buyAgain(OrderHistoryUiModel order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            showToast("Đơn hàng không có sản phẩm để mua lại");
            return;
        }

        showLoading();
        addOrderItemsToCartThenCheckout(order, 0);
    }

    private void addOrderItemsToCartThenCheckout(OrderHistoryUiModel order, int index) {
        if (index >= order.getItems().size()) {
            hideLoading();
            openCheckoutForBuyAgain(order);
            return;
        }

        OrderHistoryUiModel.OrderItemUiModel item = order.getItems().get(index);

        if (item == null || item.getVariantId() == null || item.getVariantId() <= 0) {
            addOrderItemsToCartThenCheckout(order, index + 1);
            return;
        }

        int quantity = item.getQuantity() <= 0 ? 1 : item.getQuantity();

        productRepository.addToCart(item.getVariantId(), quantity, new ActionCallback() {
            @Override
            public void onSuccess(String message) {
                addOrderItemsToCartThenCheckout(order, index + 1);
            }

            @Override
            public void onError(String message) {
                hideLoading();
                showLongToast("Không thể mua lại sản phẩm: " + item.getProductName() + "\n" + message);
            }
        });
    }

    private void openCheckoutForBuyAgain(OrderHistoryUiModel order) {
        List<CheckoutSelectedShop> selectedShops = buildCheckoutSelectedShops(order);

        if (selectedShops.isEmpty()) {
            showToast("Không có sản phẩm hợp lệ để mua lại");
            return;
        }

        String selectedShopsJson = new Gson().toJson(selectedShops);

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("checkout_source", CheckoutActivity.SOURCE_FROM_CART);
        intent.putExtra("selected_shops_json", selectedShopsJson);
        startActivity(intent);
    }

    private List<CheckoutSelectedShop> buildCheckoutSelectedShops(OrderHistoryUiModel order) {
        List<CheckoutSelectedShop> result = new ArrayList<>();

        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return result;
        }

        Long shopId = order.getShopId();
        if (shopId == null || shopId <= 0) {
            return result;
        }

        List<CheckoutSelectedShop.CheckoutSelectedItem> items = new ArrayList<>();

        for (OrderHistoryUiModel.OrderItemUiModel item : order.getItems()) {
            if (item == null || item.getVariantId() == null) continue;

            items.add(new CheckoutSelectedShop.CheckoutSelectedItem(
                    item.getVariantId(),
                    item.getProductId(),
                    item.getProductName(),
                    item.getVariantSku(),
                    item.getImageUrl(),
                    (double) item.getPriceAtPurchase(),
                    item.getQuantity() <= 0 ? 1 : item.getQuantity()
            ));
        }

        if (!items.isEmpty()) {
            result.add(new CheckoutSelectedShop(
                    order.getShopId(),
                    order.getShopName(),
                    null,
                    items
            ));
        }

        return result;
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        edtSearchOrder = findViewById(R.id.edtSearchOrder);
        txtOrderCount = findViewById(R.id.txtOrderCount);

        tabAll = findViewById(R.id.tabAll);
        tabPending = findViewById(R.id.tabPending);
        tabShipping = findViewById(R.id.tabShipping);
        tabCompleted = findViewById(R.id.tabCompleted);
        tabCancelled = findViewById(R.id.tabCancelled);

        swipeOrderHistory = findViewById(R.id.swipeOrderHistory);
        rcvOrders = findViewById(R.id.rcvOrders);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void initRecyclerView() {
        adapter = new OrderHistoryAdapter(this);

        adapter.setOnOrderActionListener(new OrderHistoryAdapter.OnOrderActionListener() {
            @Override
            public void onOrderClick(OrderHistoryUiModel order) {
                openOrderDetail(order);
            }

            @Override
            public void onCancelClick(OrderHistoryUiModel order) {
                showCancelDialog(order);
            }

            @Override
            public void onReviewClick(OrderHistoryUiModel order) {
                openReviewFromHistory(order);
            }

            @Override
            public void onBuyAgainClick(OrderHistoryUiModel order) {
                buyAgain(order);
            }

            @Override
            public void onCompleteClick(OrderHistoryUiModel order) {
                if (order == null || order.getShopOrderId() == null) {
                    showToast("Đơn hàng không hợp lệ");
                    return;
                }

                viewModel.completeOrder(order.getShopOrderId());
            }

            @Override
            public void onPayAgainClick(OrderHistoryUiModel order) {
                if (order == null || order.getShopOrderId() == null) {
                    showToast("Đơn hàng không hợp lệ");
                    return;
                }

                viewModel.retryPayment(order.getShopOrderId());
            }
        });

        rcvOrders.setLayoutManager(new LinearLayoutManager(this));
        rcvOrders.setAdapter(adapter);
    }

    private void openReviewFromHistory(OrderHistoryUiModel order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            showToast("Đơn hàng không có sản phẩm để đánh giá");
            return;
        }

        if (order.isAllReviewed()) {
            showToast("Đơn hàng này đã được đánh giá");
            return;
        }

        ArrayList<ReviewActivity.ReviewItemArg> reviewItems = new ArrayList<>();

        for (OrderHistoryUiModel.OrderItemUiModel item : order.getItems()) {
            if (item == null
                    || item.getOrderItemId() == null
                    || item.getOrderItemId() <= 0
                    || item.isReviewed()) {
                continue;
            }

            reviewItems.add(new ReviewActivity.ReviewItemArg(
                    item.getOrderItemId(),
                    item.getProductName(),
                    item.getImageUrl(),
                    item.getVariantSku(),
                    false
            ));
        }

        if (reviewItems.isEmpty()) {
            showToast("Tất cả sản phẩm trong đơn này đã được đánh giá");
            viewModel.loadOrderHistory();
            return;
        }

        Intent intent = new Intent(this, ReviewActivity.class);

        if (reviewItems.size() == 1) {
            ReviewActivity.ReviewItemArg item = reviewItems.get(0);

            intent.putExtra(ReviewActivity.EXTRA_ORDER_ITEM_ID, item.getOrderItemId());
            intent.putExtra(ReviewActivity.EXTRA_PRODUCT_NAME, item.getProductName());
            intent.putExtra(ReviewActivity.EXTRA_PRODUCT_IMAGE, item.getProductImage());
        } else {
            String json = new Gson().toJson(reviewItems);
            intent.putExtra(ReviewActivity.EXTRA_REVIEW_ITEMS_JSON, json);
        }

        startActivityForResult(intent, 2001);
    }
    private void openOrderDetail(OrderHistoryUiModel order) {
        if (order == null || order.getShopOrderId() == null || order.getShopOrderId() <= 0) {
            showToast("Không tìm thấy mã đơn hàng");
            return;
        }

        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_SHOP_ORDER_ID, order.getShopOrderId());
        startActivity(intent);
    }
    private void showCancelDialog(OrderHistoryUiModel order) {
        if (order == null || order.getShopOrderId() == null) {
            showToast("Đơn hàng không hợp lệ");
            return;
        }

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_cancel_order, null, false);

        TextView txtInfo = dialogView.findViewById(R.id.txtCancelOrderInfo);
        EditText edtReason = dialogView.findViewById(R.id.edtCancelReason);
        TextView txtCounter = dialogView.findViewById(R.id.txtCancelReasonCounter);
        TextView txtError = dialogView.findViewById(R.id.txtCancelReasonError);

        TextView chipWrongProduct = dialogView.findViewById(R.id.chipWrongProduct);
        TextView chipChangeAddress = dialogView.findViewById(R.id.chipChangeAddress);
        TextView chipNoNeed = dialogView.findViewById(R.id.chipNoNeed);
        TextView chipPaymentFailed = dialogView.findViewById(R.id.chipPaymentFailed);

        TextView btnClose = dialogView.findViewById(R.id.btnCancelDialogClose);
        TextView btnConfirm = dialogView.findViewById(R.id.btnConfirmCancelOrder);

        txtInfo.setText(
                "Order #" + order.getOrderId()
                        + " · Shop #" + order.getShopId()
                        + "\n" + order.getShopName()
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        View.OnClickListener chipListener = v -> {
            if (!(v instanceof TextView)) return;

            String reason = ((TextView) v).getText().toString().trim();
            edtReason.setText(reason);
            edtReason.setSelection(edtReason.getText().length());
            edtReason.setBackgroundResource(R.drawable.bg_cancel_reason_input);
            txtError.setVisibility(View.GONE);
        };

        chipWrongProduct.setOnClickListener(chipListener);
        chipChangeAddress.setOnClickListener(chipListener);
        chipNoNeed.setOnClickListener(chipListener);
        chipPaymentFailed.setOnClickListener(chipListener);

        edtReason.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s == null ? 0 : s.length();
                txtCounter.setText(length + "/200 ký tự");

                if (length > 0) {
                    edtReason.setBackgroundResource(R.drawable.bg_cancel_reason_input);
                    txtError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnConfirm.setOnClickListener(v -> {
            String reason = edtReason.getText().toString().trim();

            if (reason.isEmpty()) {
                txtError.setText("Vui lòng nhập lý do hủy đơn");
                txtError.setVisibility(View.VISIBLE);
                edtReason.setBackgroundResource(R.drawable.bg_cancel_reason_input_error);
                edtReason.requestFocus();
                return;
            }

            if (reason.length() < 5) {
                txtError.setText("Lý do hủy đơn phải có ít nhất 5 ký tự");
                txtError.setVisibility(View.VISIBLE);
                edtReason.setBackgroundResource(R.drawable.bg_cancel_reason_input_error);
                edtReason.requestFocus();
                return;
            }

            dialog.dismiss();
            viewModel.cancelOrder(order.getShopOrderId(), reason);
        });

        dialog.setOnShowListener(d -> {
            Window window = dialog.getWindow();
            if (window == null) return;

            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(window.getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);

            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        });

        dialog.show();
    }
    private android.graphics.drawable.GradientDrawable makeCancelReasonInputBg(boolean focused) {
        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();

        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dp(16));

        drawable.setColor(ContextCompat.getColor(this, R.color.surface_soft));

        drawable.setStroke(
                dp(focused ? 2 : 1),
                ContextCompat.getColor(
                        this,
                        focused ? R.color.brand_primary : R.color.border
                )
        );

        return drawable;
    }

    private android.graphics.drawable.GradientDrawable makeCancelReasonErrorBg() {
        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();

        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dp(16));
        drawable.setColor(ContextCompat.getColor(this, R.color.danger_light));
        drawable.setStroke(dp(2), ContextCompat.getColor(this, R.color.danger));

        return drawable;
    }
    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());

        tabAll.setOnClickListener(v -> selectTab(TAB_ALL));
        tabPending.setOnClickListener(v -> selectTab(TAB_PENDING));
        tabShipping.setOnClickListener(v -> selectTab(TAB_SHIPPING));
        tabCompleted.setOnClickListener(v -> selectTab(TAB_COMPLETED));
        tabCancelled.setOnClickListener(v -> selectTab(TAB_CANCELLED));

        swipeOrderHistory.setColorSchemeResources(R.color.brand_primary);
        swipeOrderHistory.setOnRefreshListener(() -> viewModel.loadOrderHistory());

        edtSearchOrder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không dùng
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentKeyword = s == null ? "" : s.toString().trim();
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không dùng
            }
        });
    }

    private void observeOrderHistory() {
        viewModel.getOrderHistoryState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                swipeOrderHistory.setRefreshing(true);
                return;
            }

            swipeOrderHistory.setRefreshing(false);

            if (state.isSuccess()) {
                allOrders.clear();

                List<OrderHistoryResponse> data = state.getData();

                if (data != null) {
                    for (OrderHistoryResponse item : data) {
                        allOrders.add(mapToUiModel(item));
                    }
                }

                applyFilter();
            } else {
                showLongToast(state.getMessage());
                allOrders.clear();
                applyFilter();
            }
        });
    }

    private void observeCancelOrder() {
        viewModel.getCancelState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                showLongToast(state.getMessage());
                viewModel.loadOrderHistory();
            } else {
                showLongToast(state.getMessage());
            }
        });
    }
    private OrderHistoryUiModel mapToUiModel(OrderHistoryResponse response) {
        Long orderId = response.getOrderId();
        Long shopOrderId = response.getShopOrderId();
        Long shopId = response.getShopId();

        String shopName = response.getShopName();
        String status = response.getStatus();
        long totalAmount = toLong(response.getTotalAmount());
        String createdAt = formatDateTime(response.getCreatedAt());
        String paymentStatus = response.getPaymentStatus();

        List<OrderHistoryUiModel.OrderItemUiModel> itemUiModels = new ArrayList<>();

        for (OrderHistoryResponse.OrderItemResponse item : response.getItems()) {
            itemUiModels.add(new OrderHistoryUiModel.OrderItemUiModel(
                    item.getOrderItemId(),
                    item.getProductId(),
                    item.getVariantId(),
                    item.getProductName(),
                    item.getVariantSku(),
                    item.getQuantity(),
                    item.getPriceAtPurchase(),
                    item.getImageUrl(),
                    item.canReview(),
                    item.isReviewed()
            ));
        }

        return new OrderHistoryUiModel(
                orderId,
                shopOrderId,
                shopId,
                shopName,
                status,
                paymentStatus,
                totalAmount,
                createdAt,
                response.canCancel(),
                itemUiModels
        );
    }
    private long toLong(BigDecimal value) {
        if (value == null) return 0L;
        return value.longValue();
    }

    private String formatDateTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "--";
        }

        String value = raw.trim();

        // Backend LocalDateTime thường trả dạng: 2026-05-18T09:20:30
        value = value.replace("T", " ");

        if (value.length() >= 16) {
            value = value.substring(0, 16);
        }

        // Đổi 2026-05-18 09:20 -> 18/05/2026 09:20
        try {
            String datePart = value.substring(0, 10);
            String timePart = value.length() >= 16 ? value.substring(11, 16) : "";

            String[] parts = datePart.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0] + " " + timePart;
            }
        } catch (Exception ignored) {
        }

        return value;
    }

    private void selectTab(String tab) {
        currentTab = tab;
        applyTabUi();
        applyFilter();
    }

    private void applyTabUi() {
        styleTab(tabAll, TAB_ALL.equals(currentTab));
        styleTab(tabPending, TAB_PENDING.equals(currentTab));
        styleTab(tabShipping, TAB_SHIPPING.equals(currentTab));
        styleTab(tabCompleted, TAB_COMPLETED.equals(currentTab) || TAB_REVIEW.equals(currentTab));
        styleTab(tabCancelled, TAB_CANCELLED.equals(currentTab));
    }

    private void styleTab(LinearLayout tab, boolean selected) {
        if (tab == null) return;

        int bgColor = selected
                ? ContextCompat.getColor(this, R.color.brand_primary_light)
                : ContextCompat.getColor(this, R.color.surface_soft);

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

    private void applyFilter() {
        applyTabUi();

        List<OrderHistoryUiModel> filtered = new ArrayList<>();

        for (OrderHistoryUiModel order : allOrders) {
            if (!matchTab(order)) continue;
            if (!matchKeyword(order)) continue;

            filtered.add(order);
        }

        adapter.submitList(filtered);

        txtOrderCount.setText(filtered.size() + " đơn hàng");

        boolean isEmpty = filtered.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rcvOrders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private boolean matchTab(OrderHistoryUiModel order) {
        if (TAB_ALL.equals(currentTab)) return true;

        String status = order.getStatus();
        if (status == null) return false;

        if (TAB_PENDING.equals(currentTab)) {
            return "PENDING_PAYMENT".equals(status)
                    || "PENDING".equals(status)
                    || "CONFIRMED".equals(status)
                    || "PREPARING".equals(status);
        }

        if (TAB_SHIPPING.equals(currentTab)) {
            return "SHIPPING".equals(status)
                    || "DELIVERING".equals(status)
                    || "OUT_FOR_DELIVERY".equals(status);
        }

        if (TAB_REVIEW.equals(currentTab)) {
            return isCompletedStatus(status) && hasPendingReview(order);
        }

        if (TAB_COMPLETED.equals(currentTab)) {
            return isCompletedStatus(status);
        }

        if (TAB_CANCELLED.equals(currentTab)) {
            return "CANCELLED".equals(status)
                    || "PAYMENT_FAILED".equals(status);
        }

        return true;
    }

    private boolean isCompletedStatus(String status) {
        return "DELIVERED".equals(status)
                || "COMPLETED".equals(status);
    }

    private boolean hasPendingReview(OrderHistoryUiModel order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return false;
        }

        for (OrderHistoryUiModel.OrderItemUiModel item : order.getItems()) {
            if (item != null && item.canReview() && !item.isReviewed()) {
                return true;
            }
        }

        return false;
    }

    private boolean matchKeyword(OrderHistoryUiModel order) {
        if (currentKeyword == null || currentKeyword.trim().isEmpty()) {
            return true;
        }

        String keyword = currentKeyword.toLowerCase();

        String orderCode = "order " + order.getOrderId() + " sc-" + order.getShopOrderId();
        String shopId = "shop " + order.getShopId();
        String shopName = safeLower(order.getShopName());
        String status = safeLower(order.getStatus());

        if (orderCode.contains(keyword)
                || shopId.contains(keyword)
                || shopName.contains(keyword)
                || status.contains(keyword)) {
            return true;
        }

        for (OrderHistoryUiModel.OrderItemUiModel item : order.getItems()) {
            String productName = safeLower(item.getProductName());
            String variantSku = safeLower(item.getVariantSku());

            if (productName.contains(keyword) || variantSku.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}


