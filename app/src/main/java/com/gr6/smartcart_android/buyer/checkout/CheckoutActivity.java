package com.gr6.smartcart_android.buyer.checkout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.address.AddressActivity;
import com.gr6.smartcart_android.buyer.checkout.request.CheckoutPreviewRequest;
import com.gr6.smartcart_android.buyer.checkout.request.CreateOrderRequest;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutOrderResponse;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutPreviewResponse;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutVoucherResponse;
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
        rvCheckoutShops.setNestedScrollingEnabled(false);
        rvCheckoutShops.setAdapter(checkoutShopAdapter);

        checkoutShopAdapter.setOnShopVoucherClickListener((shopId, shopName, currentVoucherCode) -> {
            pendingVoucherShopId = shopId;
            pendingVoucherShopName = shopName;
            pendingVoucherCurrentCode = currentVoucherCode;

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
                currentPreview = state.getData();
                bindPreview();
            } else {
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

        if (activeVouchers.isEmpty()) {
            showToast("Shop này chưa có mã giảm giá khả dụng");
            return;
        }

        List<String> displayItems = new ArrayList<>();

        for (CheckoutVoucherResponse voucher : activeVouchers) {
            String selectedMark = "";

            if (!isEmpty(currentVoucherCode)
                    && currentVoucherCode.equalsIgnoreCase(voucher.getCode())) {
                selectedMark = "  ✓";
            }

            displayItems.add(
                    voucher.getCode()
                            + " - "
                            + voucher.getDisplayTitle()
                            + "\n"
                            + voucher.getDisplaySubtitle()
                            + selectedMark
            );
        }

        if (!isEmpty(currentVoucherCode)) {
            displayItems.add("Không áp dụng voucher");
        }

        String title = isEmpty(shopName)
                ? "Chọn mã giảm giá"
                : "Chọn mã giảm giá - " + shopName;

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setItems(displayItems.toArray(new String[0]), (dialog, which) -> {
                    if (!isEmpty(currentVoucherCode) && which == displayItems.size() - 1) {
                        applyShopVoucher(shopId, null);
                        return;
                    }

                    CheckoutVoucherResponse selectedVoucher = activeVouchers.get(which);
                    applyShopVoucher(shopId, selectedVoucher.getCode());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void applyShopVoucher(Long shopId, String voucherCode) {
        if (checkoutShopAdapter == null) return;

        checkoutShopAdapter.setVoucherCode(shopId, voucherCode);

        if (isEmpty(voucherCode)) {
            showToast("Đã xóa voucher của shop");
        } else {
            showToast("Đang áp dụng mã: " + voucherCode);
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
            showToast("Đặt hàng thành công");
            finish();
            return;
        }

        String paymentUrl = response.getPaymentUrl();

        if (!isEmpty(paymentUrl)) {
            showToast("Chuyển tới cổng thanh toán");

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
            startActivity(intent);
            finish();
            return;
        }

        showLongToast("Đặt hàng thành công. Mã đơn #" + response.getOrderId());
        finish();
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