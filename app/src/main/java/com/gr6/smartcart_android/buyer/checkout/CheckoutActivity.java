package com.gr6.smartcart_android.buyer.checkout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ScrollView;

import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.address.AddressActivity;
import com.gr6.smartcart_android.buyer.checkout.request.CheckoutPreviewRequest;
import com.gr6.smartcart_android.buyer.checkout.request.CreateOrderRequest;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutOrderResponse;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutPreviewResponse;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutVoucherResponse;
import com.gr6.smartcart_android.buyer.order.OrderSuccessActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CheckoutActivity extends BaseActivity {

    public static final String SOURCE_BUY_NOW = "BUY_NOW";
    public static final String SOURCE_FROM_CART = "FROM_CART";
    private Long pendingVoucherShopItemTotal = 0L;
    private Long lastVoucherShopId;
    private String lastVoucherCode;

    private LinearLayout cardAddress;
    private TextView txtReceiver;
    private TextView txtAddress;

    private RecyclerView rvCheckoutShops;

    private TextView txtBillItem;
    private TextView txtBillShipping;
    private TextView txtBillDiscount;
    private TextView txtTotalAmount;
    private TextView txtBottomTotalAmount;
    private TextView txtPaymentSelected;
    private TextView btnPlaceOrder;

    private RadioButton rbCod;
    private RadioButton rbMomo;
    private RadioButton rbVnpay;

    private CheckoutViewModel viewModel;
    private CheckoutShopAdapter checkoutShopAdapter;

    private CheckoutPreviewResponse currentPreview;

    private String checkoutSource = SOURCE_BUY_NOW;

    private Long shopId;
    private Long variantId;
    private Integer quantity = 1;
    private Long selectedAddressId;

    private Long pendingVoucherShopId;
    private String pendingVoucherShopName;
    private String pendingVoucherCurrentCode;

    private List<CheckoutSelectedShop> selectedCheckoutShops = new ArrayList<>();

    private final ActivityResultLauncher<Intent> addressPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                            return;
                        }

                        Intent data = result.getData();

                        selectedAddressId = data.getLongExtra(AddressActivity.RESULT_ADDRESS_ID, -1L);

                        if (selectedAddressId == -1L) {
                            selectedAddressId = null;
                        }

                        String receiverName = data.getStringExtra(AddressActivity.RESULT_RECEIVER_NAME);
                        String receiverPhone = data.getStringExtra(AddressActivity.RESULT_RECEIVER_PHONE);
                        String fullAddress = data.getStringExtra(AddressActivity.RESULT_FULL_ADDRESS);

                        txtReceiver.setText(safe(receiverName) + " | " + safe(receiverPhone));
                        txtAddress.setText(safe(fullAddress));

                        loadPreview();
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);

        readIntent();
        initViews();
        setupRecyclerView();
        initEvents();
        observeViewModel();

        loadPreview();
    }

    private void readIntent() {
        Intent intent = getIntent();

        checkoutSource = intent.getStringExtra("checkout_source");

        if (isEmpty(checkoutSource)) {
            checkoutSource = SOURCE_BUY_NOW;
        }

        if ("CART".equalsIgnoreCase(checkoutSource)) {
            checkoutSource = SOURCE_FROM_CART;
        }

        if (SOURCE_FROM_CART.equalsIgnoreCase(checkoutSource)) {
            String json = intent.getStringExtra("selected_shops_json");

            if (!isEmpty(json)) {
                try {
                    Type type = new TypeToken<List<CheckoutSelectedShop>>() {
                    }.getType();

                    selectedCheckoutShops = new Gson().fromJson(json, type);

                    if (selectedCheckoutShops == null) {
                        selectedCheckoutShops = new ArrayList<>();
                    }
                } catch (Exception e) {
                    selectedCheckoutShops = new ArrayList<>();
                }
            }

            return;
        }

        shopId = intent.getLongExtra("shop_id", -1L);
        if (shopId == -1L) shopId = null;

        variantId = intent.getLongExtra("variant_id", -1L);
        if (variantId == -1L) variantId = null;

        quantity = intent.getIntExtra("quantity", 1);

        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }
    }

    private void initViews() {
        cardAddress = findViewById(R.id.cardAddress);
        txtReceiver = findViewById(R.id.txtReceiver);
        txtAddress = findViewById(R.id.txtAddress);

        rvCheckoutShops = findViewById(R.id.rvCheckoutShops);

        txtBillItem = findViewById(R.id.txtBillItem);
        txtBillShipping = findViewById(R.id.txtBillShipping);
        txtBillDiscount = findViewById(R.id.txtBillDiscount);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        txtBottomTotalAmount = findViewById(R.id.txtBottomTotalAmount);
        txtPaymentSelected = findViewById(R.id.txtPaymentSelected);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        rbCod = findViewById(R.id.rbCod);
        rbMomo = findViewById(R.id.rbMomo);
        rbVnpay = findViewById(R.id.rbVnpay);

        rbCod.setChecked(true);
        txtPaymentSelected.setText("COD");
    }

    private void setupRecyclerView() {
        checkoutShopAdapter = new CheckoutShopAdapter(this);

        rvCheckoutShops.setLayoutManager(new LinearLayoutManager(this));
        rvCheckoutShops.setAdapter(checkoutShopAdapter);

        rvCheckoutShops.setNestedScrollingEnabled(false);
        rvCheckoutShops.setHasFixedSize(false);
        rvCheckoutShops.setItemViewCacheSize(20);

        checkoutShopAdapter.setOnShopVoucherClickListener((shopId, shopName, currentVoucherCode, shopItemTotal) -> {
            pendingVoucherShopId = shopId;
            pendingVoucherShopName = shopName;
            pendingVoucherCurrentCode = currentVoucherCode;
            pendingVoucherShopItemTotal = shopItemTotal == null ? 0L : shopItemTotal;

            viewModel.loadShopVouchers(shopId);
        });
    }
    private void initEvents() {
        findViewById(R.id.btnBackCheckout).setOnClickListener(v -> finish());

        cardAddress.setOnClickListener(v -> openAddressPicker());

        btnPlaceOrder.setOnClickListener(v -> placeOrder());

        rbCod.setOnClickListener(v -> {
            rbCod.setChecked(true);
            rbMomo.setChecked(false);
            rbVnpay.setChecked(false);
            txtPaymentSelected.setText("COD");
        });

        rbMomo.setOnClickListener(v -> {
            rbCod.setChecked(false);
            rbMomo.setChecked(true);
            rbVnpay.setChecked(false);
            txtPaymentSelected.setText("Ví MoMo");
        });

        rbVnpay.setOnClickListener(v -> {
            rbCod.setChecked(false);
            rbMomo.setChecked(false);
            rbVnpay.setChecked(true);
            txtPaymentSelected.setText("VNPay");
        });
    }

    private void observeViewModel() {
        viewModel.getPreviewState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                lastVoucherShopId = null;
                lastVoucherCode = null;

                currentPreview = state.getData();
                bindPreview();
            } else {
                if (lastVoucherShopId != null && !isEmpty(lastVoucherCode)) {
                    checkoutShopAdapter.setVoucherCode(lastVoucherShopId, null);
                    lastVoucherShopId = null;
                    lastVoucherCode = null;
                }

                showLongToast(state.getMessage());
            }
        });

        viewModel.getOrderState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                handleOrderSuccess(state.getData());
            } else {
                showLongToast(state.getMessage());
            }
        });

        viewModel.getVoucherState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                showShopVoucherPicker(
                        pendingVoucherShopId,
                        pendingVoucherShopName,
                        pendingVoucherCurrentCode,
                        pendingVoucherShopItemTotal,
                        state.getData()
                );
            } else {
                showLongToast(state.getMessage());
            }
        });
    }

    private void openAddressPicker() {
        Intent intent = new Intent(this, AddressActivity.class);
        intent.putExtra(AddressActivity.EXTRA_SELECT_MODE, true);
        addressPickerLauncher.launch(intent);
    }

    private void loadPreview() {
        if (SOURCE_FROM_CART.equalsIgnoreCase(checkoutSource)) {
            if (selectedCheckoutShops == null || selectedCheckoutShops.isEmpty()) {
                showLongToast("Bạn chưa chọn sản phẩm nào để thanh toán");
                return;
            }

            for (CheckoutSelectedShop shop : selectedCheckoutShops) {
                if (shop.getShopId() == null || shop.getShopId() <= 0) {
                    showLongToast("Thiếu shopId để thanh toán");
                    return;
                }

                if (shop.getItems().isEmpty()) {
                    showLongToast("Có shop chưa có sản phẩm thanh toán");
                    return;
                }
            }

            viewModel.loadPreview(buildPreviewRequest(selectedAddressId));
            return;
        }

        if (shopId == null || shopId <= 0) {
            showLongToast("Thiếu shopId để thanh toán");
            return;
        }

        if (variantId == null || variantId <= 0) {
            showLongToast("Thiếu variantId để thanh toán");
            return;
        }

        if (quantity == null || quantity <= 0) {
            showLongToast("Số lượng thanh toán không hợp lệ");
            return;
        }

        viewModel.loadPreview(buildPreviewRequest(selectedAddressId));
    }

    private CheckoutPreviewRequest buildPreviewRequest(Long addressId) {
        List<CheckoutPreviewRequest.ShopOrderPreviewDto> shopOrders = new ArrayList<>();

        if (SOURCE_FROM_CART.equalsIgnoreCase(checkoutSource)) {
            for (CheckoutSelectedShop selectedShop : selectedCheckoutShops) {
                List<CheckoutPreviewRequest.ItemPreviewDto> items = new ArrayList<>();

                for (CheckoutSelectedShop.CheckoutSelectedItem selectedItem : selectedShop.getItems()) {
                    items.add(new CheckoutPreviewRequest.ItemPreviewDto(
                            selectedItem.getVariantId(),
                            selectedItem.getQuantity()
                    ));
                }

                String voucherCode = selectedShop.getVoucherCode();

                if (checkoutShopAdapter != null) {
                    String adapterVoucher = checkoutShopAdapter.getVoucherCode(selectedShop.getShopId());
                    if (!isEmpty(adapterVoucher)) {
                        voucherCode = adapterVoucher;
                    }
                }

                shopOrders.add(new CheckoutPreviewRequest.ShopOrderPreviewDto(
                        selectedShop.getShopId(),
                        voucherCode,
                        items
                ));
            }

            return new CheckoutPreviewRequest(
                    addressId,
                    checkoutSource,
                    shopOrders
            );
        }

        List<CheckoutPreviewRequest.ItemPreviewDto> items = new ArrayList<>();
        items.add(new CheckoutPreviewRequest.ItemPreviewDto(variantId, quantity));

        String voucherCode = null;

        if (checkoutShopAdapter != null) {
            voucherCode = checkoutShopAdapter.getVoucherCode(shopId);
        }

        shopOrders.add(new CheckoutPreviewRequest.ShopOrderPreviewDto(
                shopId,
                voucherCode,
                items
        ));

        return new CheckoutPreviewRequest(
                addressId,
                checkoutSource,
                shopOrders
        );
    }

    private CreateOrderRequest buildCreateOrderRequest() {
        List<CreateOrderRequest.ShopOrderRequest> shopOrders = new ArrayList<>();

        if (SOURCE_FROM_CART.equalsIgnoreCase(checkoutSource)) {
            for (CheckoutSelectedShop selectedShop : selectedCheckoutShops) {
                List<CreateOrderRequest.ItemRequest> items = new ArrayList<>();

                for (CheckoutSelectedShop.CheckoutSelectedItem selectedItem : selectedShop.getItems()) {
                    items.add(new CreateOrderRequest.ItemRequest(
                            selectedItem.getVariantId(),
                            selectedItem.getQuantity()
                    ));
                }

                String voucherCode = selectedShop.getVoucherCode();

                if (checkoutShopAdapter != null) {
                    String adapterVoucher = checkoutShopAdapter.getVoucherCode(selectedShop.getShopId());
                    if (!isEmpty(adapterVoucher)) {
                        voucherCode = adapterVoucher;
                    }
                }

                shopOrders.add(new CreateOrderRequest.ShopOrderRequest(
                        selectedShop.getShopId(),
                        voucherCode,
                        items
                ));
            }
        } else {
            List<CreateOrderRequest.ItemRequest> items = new ArrayList<>();
            items.add(new CreateOrderRequest.ItemRequest(variantId, quantity));

            String voucherCode = null;

            if (checkoutShopAdapter != null) {
                voucherCode = checkoutShopAdapter.getVoucherCode(shopId);
            }

            shopOrders.add(new CreateOrderRequest.ShopOrderRequest(
                    shopId,
                    voucherCode,
                    items
            ));
        }

        String paymentMethod;
        String paymentProvider;

        if (rbMomo.isChecked()) {
            paymentMethod = "ONLINE";
            paymentProvider = "MOMO";
        } else if (rbVnpay.isChecked()) {
            paymentMethod = "ONLINE";
            paymentProvider = "VNPAY";
        } else {
            paymentMethod = "COD";
            paymentProvider = "NONE";
        }

        return new CreateOrderRequest(
                selectedAddressId,
                paymentMethod,
                paymentProvider,
                checkoutSource,
                UUID.randomUUID().toString(),
                shopOrders
        );
    }

    private void bindPreview() {
        if (currentPreview == null) return;

        CheckoutPreviewResponse.AddressPreviewDto address = currentPreview.getDefaultAddress();

        if (address != null) {
            selectedAddressId = address.getAddressId();

            txtReceiver.setText(
                    safe(address.getReceiverName()) + " | " + safe(address.getReceiverPhone())
            );

            txtAddress.setText(safe(address.getFullAddress()));
        } else if (selectedAddressId == null) {
            txtReceiver.setText("Chưa có địa chỉ nhận hàng");
            txtAddress.setText("Bấm để chọn hoặc thêm địa chỉ giao hàng");
        }

        checkoutShopAdapter.submitList(currentPreview.getShops());

        txtBillItem.setText(formatVnd(currentPreview.getTotalItemPrice()));
        txtBillShipping.setText(formatVnd(currentPreview.getTotalShippingFee()));
        txtBillDiscount.setText("-" + formatVnd(currentPreview.getTotalDiscount()));
        txtTotalAmount.setText(formatVnd(currentPreview.getTotalAmount()));
        txtBottomTotalAmount.setText(formatVnd(currentPreview.getTotalAmount()));
    }
    private void showShopVoucherPicker(
            Long shopId,
            String shopName,
            String currentVoucherCode,
            Long shopItemTotal,
            List<CheckoutVoucherResponse> vouchers
    ) {
        if (shopId == null || shopId <= 0) {
            showToast("Shop không hợp lệ");
            return;
        }

        List<CheckoutVoucherResponse> activeVouchers = new ArrayList<>();

        if (vouchers != null) {
            for (CheckoutVoucherResponse voucher : vouchers) {
                if (voucher == null) continue;
                if (isEmpty(voucher.getCode())) continue;
                if (!"ACTIVE".equalsIgnoreCase(voucher.getStatus())) continue;

                activeVouchers.add(voucher);
            }
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(14), dp(18), dp(24));
        root.setBackgroundColor(ContextCompat.getColor(this, R.color.surface));

        scrollView.addView(root);

        View handle = new View(this);
        handle.setBackgroundResource(R.drawable.bg_bottom_sheet_handle);

        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(46), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(handle, handleParams);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        headerParams.topMargin = dp(18);
        root.addView(header, headerParams);

        TextView icon = new TextView(this);
        icon.setText("🎟");
        icon.setGravity(Gravity.CENTER);
        icon.setTextSize(24);
        icon.setBackgroundResource(R.drawable.bg_checkout_voucher_badge);

        header.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams titleBoxParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        titleBoxParams.leftMargin = dp(12);
        header.addView(titleBox, titleBoxParams);

        TextView title = createVoucherSheetText(
                "Chọn mã giảm giá",
                20,
                R.color.text_primary,
                Typeface.BOLD
        );
        titleBox.addView(title);

        TextView subtitle = createVoucherSheetText(
                isEmpty(shopName) ? "Voucher chỉ áp dụng cho shop này" : shopName,
                13,
                R.color.text_secondary,
                Typeface.NORMAL
        );

        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(3);
        titleBox.addView(subtitle, subtitleParams);

        TextView orderValue = createVoucherSheetText(
                "Giá trị sản phẩm của shop: " + formatVnd(shopItemTotal),
                13,
                R.color.brand_primary,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams orderValueParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        orderValueParams.topMargin = dp(14);
        root.addView(orderValue, orderValueParams);

        if (activeVouchers.isEmpty()) {
            TextView empty = createVoucherSheetText(
                    "Shop này chưa có mã giảm giá khả dụng.",
                    14,
                    R.color.text_secondary,
                    Typeface.NORMAL
            );
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(28), 0, dp(28));
            root.addView(empty);

            TextView close = createVoucherSheetText(
                    "Đóng",
                    15,
                    R.color.white,
                    Typeface.BOLD
            );
            close.setGravity(Gravity.CENTER);
            close.setBackgroundResource(R.drawable.bg_cart_checkout);

            LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(50)
            );
            root.addView(close, closeParams);

            close.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(scrollView);
            dialog.show();
            return;
        }

        for (CheckoutVoucherResponse voucher : activeVouchers) {
            View voucherView = createVoucherCard(
                    voucher,
                    currentVoucherCode,
                    shopItemTotal,
                    () -> {
                        applyShopVoucher(shopId, voucher.getCode());
                        dialog.dismiss();
                    }
            );

            LinearLayout.LayoutParams voucherParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            voucherParams.topMargin = dp(12);
            root.addView(voucherView, voucherParams);
        }

        if (!isEmpty(currentVoucherCode)) {
            TextView remove = createVoucherSheetText(
                    "Không áp dụng voucher",
                    15,
                    R.color.text_secondary,
                    Typeface.BOLD
            );
            remove.setGravity(Gravity.CENTER);
            remove.setBackgroundResource(R.drawable.bg_checkout_remove_voucher);

            LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(50)
            );
            removeParams.topMargin = dp(16);
            root.addView(remove, removeParams);

            remove.setOnClickListener(v -> {
                applyShopVoucher(shopId, null);
                dialog.dismiss();
            });
        }

        dialog.setContentView(scrollView);
        dialog.show();
    }

    private View createVoucherCard(
            CheckoutVoucherResponse voucher,
            String currentVoucherCode,
            Long shopItemTotal,
            Runnable onClick
    ) {
        boolean selected = !isEmpty(currentVoucherCode)
                && currentVoucherCode.equalsIgnoreCase(voucher.getCode());

        String unavailableReason = getVoucherUnavailableReason(voucher, shopItemTotal);
        boolean canUse = unavailableReason == null;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackgroundResource(
                selected
                        ? R.drawable.bg_checkout_voucher_card_selected
                        : canUse
                        ? R.drawable.bg_checkout_voucher_card
                        : R.drawable.bg_checkout_voucher_card_disabled
        );

        TextView badge = new TextView(this);
        badge.setText("PERCENT".equalsIgnoreCase(voucher.getDiscountType()) ? "%" : "₫");
        badge.setGravity(Gravity.CENTER);
        badge.setTextSize(22);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setTextColor(ContextCompat.getColor(this, canUse ? R.color.white : R.color.text_secondary));
        badge.setBackgroundResource(canUse
                ? R.drawable.bg_checkout_voucher_badge_red
                : R.drawable.bg_checkout_voucher_badge
        );

        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(58), dp(58));
        card.addView(badge, badgeParams);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        infoParams.leftMargin = dp(12);
        card.addView(info, infoParams);

        TextView title = createVoucherSheetText(
                voucher.getDisplayTitle(),
                16,
                canUse ? R.color.text_primary : R.color.text_secondary,
                Typeface.BOLD
        );
        info.addView(title);

        TextView code = createVoucherSheetText(
                "Mã: " + voucher.getCode(),
                13,
                canUse ? R.color.price_red : R.color.text_secondary,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        codeParams.topMargin = dp(3);
        info.addView(code, codeParams);

        String subtitleText = canUse ? voucher.getDisplaySubtitle() : unavailableReason;

        TextView subtitle = createVoucherSheetText(
                subtitleText,
                12,
                canUse ? R.color.text_secondary : R.color.price_red,
                canUse ? Typeface.NORMAL : Typeface.BOLD
        );

        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(3);
        info.addView(subtitle, subtitleParams);

        TextView action = new TextView(this);
        action.setGravity(Gravity.CENTER);
        action.setText(selected ? "Đã chọn" : canUse ? "Chọn" : "Không đủ");
        action.setTextSize(13);
        action.setTypeface(null, Typeface.BOLD);
        action.setTextColor(ContextCompat.getColor(
                this,
                selected ? R.color.white : canUse ? R.color.price_red : R.color.text_secondary
        ));
        action.setBackgroundResource(
                selected
                        ? R.drawable.bg_checkout_voucher_action_selected
                        : canUse
                        ? R.drawable.bg_checkout_voucher_action
                        : R.drawable.bg_checkout_voucher_action_disabled
        );

        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(dp(78), dp(34));
        actionParams.leftMargin = dp(8);
        card.addView(action, actionParams);

        if (canUse) {
            card.setOnClickListener(v -> onClick.run());
            action.setOnClickListener(v -> onClick.run());
        } else {
            card.setAlpha(0.78f);
            card.setOnClickListener(v -> showToast(unavailableReason));
        }

        return card;
    }

    private String getVoucherUnavailableReason(
            CheckoutVoucherResponse voucher,
            Long shopItemTotal
    ) {
        if (voucher == null) {
            return "Voucher không hợp lệ";
        }

        long itemTotal = shopItemTotal == null ? 0L : shopItemTotal;
        long minOrder = voucher.getMinOrderValue();

        if (minOrder > 0 && itemTotal < minOrder) {
            long missing = minOrder - itemTotal;
            return "Cần mua thêm " + formatVnd(missing) + " để dùng mã này";
        }

        int usageLimit = voucher.getUsageLimit();
        int usedCount = voucher.getUsedCount();

        if (usageLimit > 0 && usedCount >= usageLimit) {
            return "Mã này đã hết lượt sử dụng";
        }

        return null;
    }

    private TextView createVoucherSheetText(
            String text,
            int sp,
            int colorRes,
            int style
    ) {
        TextView textView = new TextView(this);
        textView.setText(text == null ? "" : text);
        textView.setTextSize(sp);
        textView.setTextColor(ContextCompat.getColor(this, colorRes));
        textView.setTypeface(null, style);
        return textView;
    }

    private void applyShopVoucher(Long shopId, String voucherCode) {
        if (checkoutShopAdapter == null) return;

        lastVoucherShopId = shopId;
        lastVoucherCode = voucherCode;

        checkoutShopAdapter.setVoucherCode(shopId, voucherCode);

        if (isEmpty(voucherCode)) {
            showToast("Đã xóa voucher của shop");
        } else {
            showToast("Đang kiểm tra mã: " + voucherCode);
        }

        loadPreview();
    }



    private void placeOrder() {
        if (selectedAddressId == null || selectedAddressId <= 0) {
            showLongToast("Bạn chưa chọn địa chỉ nhận hàng");
            openAddressPicker();
            return;
        }

        viewModel.createOrder(buildCreateOrderRequest());
    }

    private void handleOrderSuccess(CheckoutOrderResponse response) {
        if (response == null) {
            showLongToast("Server không trả dữ liệu đơn hàng");
            return;
        }

        String provider = safeText(response.getPaymentProvider(), "NONE");
        String paymentUrl = safeText(response.getPaymentUrl(), "");

        android.util.Log.d(
                "CHECKOUT_PAYMENT_RESPONSE",
                "orderId=" + response.getOrderId()
                        + ", shopOrderId=" + response.getShopOrderId()
                        + ", provider=" + provider
                        + ", paymentUrl=" + paymentUrl
                        + ", orderStatus=" + response.getOrderStatus()
                        + ", paymentStatus=" + response.getPaymentStatus()
        );

        boolean onlinePayment = "MOMO".equalsIgnoreCase(provider)
                || "VNPAY".equalsIgnoreCase(provider);

        if (onlinePayment && paymentUrl.isEmpty()) {
            showLongToast(
                    "Server chưa trả link thanh toán " + provider
                            + ". Kiểm tra backend createPaymentUrl()."
            );
            return;
        }

        if (!paymentUrl.isEmpty()) {
            showToast("Chuyển tới cổng thanh toán " + provider);

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                startActivity(intent);
            } catch (Exception e) {
                showLongToast("Không mở được cổng thanh toán " + provider);
            }

            finish();
            return;
        }

        Intent intent = new Intent(this, OrderSuccessActivity.class);
        intent.putExtra(OrderSuccessActivity.EXTRA_SUCCESS, true);

        if (response.getOrderId() != null) {
            intent.putExtra(OrderSuccessActivity.EXTRA_ORDER_ID, response.getOrderId());
        }

        if (response.getShopOrderId() != null) {
            intent.putExtra(OrderSuccessActivity.EXTRA_SHOP_ORDER_ID, response.getShopOrderId());
        }

        intent.putExtra(OrderSuccessActivity.EXTRA_TOTAL_AMOUNT, response.getTotalAmount());
        intent.putExtra(OrderSuccessActivity.EXTRA_PAYMENT_METHOD, "COD");
        intent.putExtra(OrderSuccessActivity.EXTRA_PAYMENT_PROVIDER, "NONE");
        intent.putExtra(OrderSuccessActivity.EXTRA_PAYMENT_STATUS, safeText(response.getPaymentStatus(), "PENDING"));
        intent.putExtra(OrderSuccessActivity.EXTRA_ORDER_STATUS, safeText(response.getOrderStatus(), "PENDING"));
        intent.putExtra(OrderSuccessActivity.EXTRA_MESSAGE, "Đặt hàng thành công");

        startActivity(intent);
        finish();
    }
    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }
    private String formatVnd(Long value) {
        long amount = value == null ? 0L : value;
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + "đ";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

}