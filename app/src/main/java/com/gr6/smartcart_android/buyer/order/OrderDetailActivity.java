package com.gr6.smartcart_android.buyer.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.checkout.CheckoutActivity;
import com.gr6.smartcart_android.buyer.checkout.CheckoutSelectedShop;
import com.gr6.smartcart_android.buyer.order.response.OrderDetailResponse;
import com.gr6.smartcart_android.buyer.product.repository.ProductRepository;
import com.gr6.smartcart_android.buyer.product.repository.ProductRepository.ActionCallback;
import com.gr6.smartcart_android.buyer.review.ReviewActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends BaseActivity {

    public static final String EXTRA_SHOP_ORDER_ID = "shop_order_id";
    private static final int REQUEST_REVIEW = 2001;

    private TextView txtOrderCode;
    private TextView txtShopName;
    private TextView txtStatus;
    private TextView txtCreatedAt;
    private TextView txtPaymentMethod;
    private TextView txtReceiverName;
    private TextView txtReceiverPhone;
    private TextView txtShippingAddress;
    private TextView txtItemCount;
    private TextView txtShippingFee;
    private TextView txtDiscountAmount;
    private TextView txtTotalAmount;
    private TextView btnCancelOrder;
    private TextView btnCompleteOrder;
    private TextView btnBuyAgain;
    private TextView txtEmpty;

    private TextView txtJourneyCurrentTitle;
    private TextView txtJourneyCurrentDesc;
    private TextView txtJourneyCurrentTime;
    private TextView txtJourneyCreatedTime;

    private View layoutContent;
    private View layoutEmpty;
    private RecyclerView rcvItems;

    private OrderDetailViewModel viewModel;
    private OrderDetailItemAdapter adapter;
    private ProductRepository productRepository;

    private Long shopOrderId;
    private OrderDetailResponse currentDetail;

    private final NumberFormat moneyFormat =
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        viewModel = new ViewModelProvider(this).get(OrderDetailViewModel.class);
        productRepository = new ProductRepository(this);

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
        txtPaymentMethod = findViewById(R.id.txtPaymentMethod);
        txtReceiverName = findViewById(R.id.txtReceiverName);
        txtReceiverPhone = findViewById(R.id.txtReceiverPhone);
        txtShippingAddress = findViewById(R.id.txtShippingAddress);
        txtItemCount = findViewById(R.id.txtItemCount);
        txtShippingFee = findViewById(R.id.txtShippingFee);
        txtDiscountAmount = findViewById(R.id.txtDiscountAmount);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnCompleteOrder = findViewById(R.id.btnCompleteOrder);
        btnBuyAgain = findViewById(R.id.btnBuyAgain);
        txtEmpty = findViewById(R.id.txtEmpty);

        layoutContent = findViewById(R.id.layoutContent);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        rcvItems = findViewById(R.id.rcvItems);

        txtJourneyCurrentTitle = findViewById(R.id.txtJourneyCurrentTitle);
        txtJourneyCurrentDesc = findViewById(R.id.txtJourneyCurrentDesc);
        txtJourneyCurrentTime = findViewById(R.id.txtJourneyCurrentTime);
        txtJourneyCreatedTime = findViewById(R.id.txtJourneyCreatedTime);
    }

    private void setupRecyclerView() {
        adapter = new OrderDetailItemAdapter();
        adapter.setOnReviewClickListener(this::openReview);

        rcvItems.setLayoutManager(new LinearLayoutManager(this));
        rcvItems.setAdapter(adapter);
        rcvItems.setNestedScrollingEnabled(false);
    }

    private void initEvents() {
        findViewById(R.id.imgBack).setOnClickListener(v -> finish());

        btnCancelOrder.setOnClickListener(v ->
                showToast("Muốn hủy đơn thì quay lại danh sách đơn hàng")
        );

        btnCompleteOrder.setOnClickListener(v ->
                viewModel.completeOrder(shopOrderId)
        );

        btnBuyAgain.setOnClickListener(v ->
                buyAgain(currentDetail)
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

        viewModel.getCompleteState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                showToast("Đã xác nhận hoàn thành đơn hàng");
                setResult(RESULT_OK);
                viewModel.loadOrderDetail(shopOrderId);
            } else {
                showLongToast(state.getMessage());
            }
        });
    }

    private void bindOrderDetail(OrderDetailResponse detail) {
        if (detail == null) {
            showEmpty("Không có dữ liệu chi tiết đơn hàng");
            return;
        }

        currentDetail = detail;

        layoutEmpty.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);

        txtOrderCode.setText(detail.getOrderCode());
        txtShopName.setText(detail.getShopName());
        txtStatus.setText(formatStatus(detail.getStatus()));
        txtCreatedAt.setText(formatDate(detail.getCreatedAt()));

        txtReceiverName.setText(detail.getReceiverName());
        txtReceiverPhone.setText(detail.getReceiverPhone());
        txtShippingAddress.setText(detail.getShippingAddress());
        txtPaymentMethod.setText(detail.getPaymentText());

        bindJourney(detail);

        txtItemCount.setText(detail.getItemCount() + " sản phẩm");
        txtShippingFee.setText(formatMoney(detail.getShippingFee()));
        txtDiscountAmount.setText("-" + formatMoney(detail.getDiscountAmount()));
        txtTotalAmount.setText(formatMoney(detail.getTotalAmount()));

        btnCancelOrder.setVisibility(detail.canCancel() ? View.VISIBLE : View.GONE);
        btnCompleteOrder.setVisibility(detail.isDelivered() ? View.VISIBLE : View.GONE);

        boolean canBuyAgain = detail.isCompleted() || detail.isCancelled();
        btnBuyAgain.setVisibility(canBuyAgain ? View.VISIBLE : View.GONE);

        adapter.setData(detail.getItems());
    }

    private void buyAgain(OrderDetailResponse detail) {
        if (detail == null) {
            showToast("Không có dữ liệu đơn hàng để mua lại");
            return;
        }

        if (detail.getItems() == null || detail.getItems().isEmpty()) {
            showToast("Đơn hàng không có sản phẩm để mua lại");
            return;
        }

        if (detail.getShopId() == null || detail.getShopId() <= 0) {
            showLongToast("Thiếu shopId trong chi tiết đơn hàng. Cần backend trả thêm shopId để mua lại.");
            return;
        }

        showLoading();
        addDetailItemsToCartThenCheckout(detail, 0);
    }

    private void addDetailItemsToCartThenCheckout(
            OrderDetailResponse detail,
            int index
    ) {
        if (detail == null || detail.getItems() == null) {
            hideLoading();
            showToast("Đơn hàng không hợp lệ");
            return;
        }

        if (index >= detail.getItems().size()) {
            hideLoading();
            openCheckoutForBuyAgain(detail);
            return;
        }

        OrderDetailResponse.OrderItemResponse item = detail.getItems().get(index);

        if (item == null || item.getVariantId() == null || item.getVariantId() <= 0) {
            addDetailItemsToCartThenCheckout(detail, index + 1);
            return;
        }

        int quantity = item.getQuantity() <= 0 ? 1 : item.getQuantity();

        productRepository.addToCart(item.getVariantId(), quantity, new ActionCallback() {
            @Override
            public void onSuccess(String message) {
                addDetailItemsToCartThenCheckout(detail, index + 1);
            }

            @Override
            public void onError(String message) {
                hideLoading();
                showLongToast("Không thể mua lại sản phẩm: "
                        + item.getProductName()
                        + "\n"
                        + message
                );
            }
        });
    }

    private void openCheckoutForBuyAgain(OrderDetailResponse detail) {
        List<CheckoutSelectedShop> selectedShops = buildCheckoutSelectedShops(detail);

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

    private List<CheckoutSelectedShop> buildCheckoutSelectedShops(OrderDetailResponse detail) {
        List<CheckoutSelectedShop> result = new ArrayList<>();

        if (detail == null || detail.getItems() == null || detail.getItems().isEmpty()) {
            return result;
        }

        Long shopId = detail.getShopId();

        if (shopId == null || shopId <= 0) {
            return result;
        }

        List<CheckoutSelectedShop.CheckoutSelectedItem> items = new ArrayList<>();

        for (OrderDetailResponse.OrderItemResponse item : detail.getItems()) {
            if (item == null || item.getVariantId() == null || item.getVariantId() <= 0) {
                continue;
            }

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
                    shopId,
                    detail.getShopName(),
                    null,
                    items
            ));
        }

        return result;
    }

    private void openReview(OrderDetailResponse.OrderItemResponse item) {
        if (item == null || item.getOrderItemId() == null) {
            showToast("Không tìm thấy sản phẩm trong đơn hàng");
            return;
        }

        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putExtra(ReviewActivity.EXTRA_ORDER_ITEM_ID, item.getOrderItemId());
        intent.putExtra(ReviewActivity.EXTRA_PRODUCT_NAME, item.getProductName());
        intent.putExtra(ReviewActivity.EXTRA_PRODUCT_IMAGE, item.getImageUrl());

        startActivityForResult(intent, REQUEST_REVIEW);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_REVIEW && resultCode == RESULT_OK) {
            viewModel.loadOrderDetail(shopOrderId);
        }
    }

    private void bindJourney(OrderDetailResponse detail) {
        txtJourneyCurrentTitle.setText(detail.getJourneyCurrentTitle());
        txtJourneyCurrentDesc.setText(detail.getJourneyCurrentDescription());

        String currentTime = detail.getUpdatedAt();

        if (currentTime == null || currentTime.trim().isEmpty()) {
            currentTime = detail.getCreatedAt();
        }

        txtJourneyCurrentTime.setText(formatDate(currentTime));
        txtJourneyCreatedTime.setText(formatDate(detail.getCreatedAt()));
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