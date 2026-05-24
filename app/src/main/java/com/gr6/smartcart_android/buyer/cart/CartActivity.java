package com.gr6.smartcart_android.buyer.cart;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.cart.response.CartDetailResponse;
import com.gr6.smartcart_android.buyer.product.response.ProductDetailResponse;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.content.Intent;

import com.google.gson.Gson;
import com.gr6.smartcart_android.buyer.checkout.CheckoutActivity;
import com.gr6.smartcart_android.buyer.checkout.CheckoutSelectedShop;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.gr6.smartcart_android.buyer.checkout.CheckoutActivity;
import com.gr6.smartcart_android.buyer.checkout.CheckoutSelectedShop;

public class CartActivity extends BaseActivity {

    private SwipeRefreshLayout swipeCart;
    private RecyclerView rcvCartShops;
    private LinearLayout layoutEmptyCart;

    private CheckBox cbSelectAll;
    private TextView txtSelectedCount;
    private TextView txtCartTotal;
    private TextView btnCheckout;

    private CartViewModel viewModel;
    private CartShopAdapter adapter;

    private final List<CartDetailResponse.ShopCart> shops = new ArrayList<>();
    private boolean bindingSelectAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        viewModel = new ViewModelProvider(this).get(CartViewModel.class);

        initViews();
        setupRecyclerView();
        initEvents();
        observeData();

        viewModel.loadCart();
    }

    private void initViews() {
        swipeCart = findViewById(R.id.swipeCart);
        rcvCartShops = findViewById(R.id.rcvCartShops);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);

        cbSelectAll = findViewById(R.id.cbSelectAll);
        txtSelectedCount = findViewById(R.id.txtSelectedCount);
        txtCartTotal = findViewById(R.id.txtCartTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
    }

    private void setupRecyclerView() {
        adapter = new CartShopAdapter();

        rcvCartShops.setLayoutManager(new LinearLayoutManager(this));
        rcvCartShops.setAdapter(adapter);

        adapter.setListener(new CartShopAdapter.CartListener() {
            @Override
            public void onSelectionChanged() {
                updateBottomBar();
            }

            @Override
            public void onChangeVariant(CartDetailResponse.CartItem item) {
                showChangeVariantBottomSheet(item);
            }

            @Override
            public void onIncrease(CartDetailResponse.CartItem item) {
                int newQuantity = item.getQuantity() + 1;

                if (newQuantity > item.getStock()) {
                    showToast("Số lượng đã đạt tồn kho tối đa");
                    return;
                }

                viewModel.updateQuantity(item.getVariantId(), newQuantity);
            }

            @Override
            public void onDecrease(CartDetailResponse.CartItem item) {
                int newQuantity = item.getQuantity() - 1;

                if (newQuantity < 1) {
                    confirmRemove(item);
                    return;
                }

                viewModel.updateQuantity(item.getVariantId(), newQuantity);
            }

            @Override
            public void onRemove(CartDetailResponse.CartItem item) {
                confirmRemove(item);
            }

            @Override
            public void onProductClick(CartDetailResponse.CartItem item) {
                openProductDetail(item);
            }
        });
    }

    private void showChangeVariantBottomSheet(CartDetailResponse.CartItem item) {
        CartVariantBottomSheet bottomSheet = new CartVariantBottomSheet(
                this,
                item,
                new CartVariantBottomSheet.OnVariantSelectedListener() {
                    @Override
                    public void onVariantSelected(
                            CartDetailResponse.CartItem oldItem,
                            ProductDetailResponse.VariantDTO selectedVariant
                    ) {
                        if (oldItem == null || oldItem.getCartItemId() == null) {
                            showToast("Thiếu cartItemId để đổi phân loại");
                            return;
                        }

                        if (selectedVariant == null || selectedVariant.getVariantId() == null) {
                            showToast("Phân loại không hợp lệ");
                            return;
                        }

                        viewModel.changeVariant(
                                oldItem.getCartItemId(),
                                selectedVariant.getVariantId()
                        );
                    }

                    @Override
                    public void onMessage(String message) {
                        showToast(message);
                    }
                }
        );

        bottomSheet.show();
    }
    private void initEvents() {
        findViewById(R.id.imgBack).setOnClickListener(v -> finish());

        swipeCart.setColorSchemeResources(R.color.brand_primary);
        swipeCart.setOnRefreshListener(() -> viewModel.loadCart());

        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (bindingSelectAll) return;

            for (CartDetailResponse.ShopCart shop : shops) {
                shop.setSelected(isChecked);
            }

            adapter.notifyDataSetChanged();
            updateBottomBar();
        });

        btnCheckout.setOnClickListener(v -> checkoutSelectedItems());
    }

    private void observeData() {
        viewModel.getCartState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                swipeCart.setRefreshing(true);
                return;
            }

            swipeCart.setRefreshing(false);

            if (state.isSuccess()) {
                bindCart(state.getData());
            } else {
                showLongToast(state.getMessage());
                showEmpty(true);
            }
        });

        viewModel.getActionState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                showToast(state.getMessage());
                viewModel.loadCart();
            } else {
                showLongToast(state.getMessage());
            }
        });
    }

    private void bindCart(CartDetailResponse cart) {
        shops.clear();

        if (cart != null && cart.getShops() != null) {
            List<CartDetailResponse.ShopCart> sortedShops = new ArrayList<>(cart.getShops());

            sortedShops.sort((s1, s2) -> {
                Long id1 = s1.getShopId() == null ? Long.MAX_VALUE : s1.getShopId();
                Long id2 = s2.getShopId() == null ? Long.MAX_VALUE : s2.getShopId();
                return id1.compareTo(id2);
            });

            for (CartDetailResponse.ShopCart shop : sortedShops) {
                shop.getItems().sort((i1, i2) -> {
                    Long id1 = i1.getCartItemId() == null ? Long.MAX_VALUE : i1.getCartItemId();
                    Long id2 = i2.getCartItemId() == null ? Long.MAX_VALUE : i2.getCartItemId();
                    return id1.compareTo(id2);
                });
            }

            shops.addAll(sortedShops);
        }

        boolean empty = shops.isEmpty();

        showEmpty(empty);

        adapter.setData(shops);
        updateBottomBar();
    }
    private void showEmpty(boolean empty) {
        layoutEmptyCart.setVisibility(empty ? View.VISIBLE : View.GONE);
        rcvCartShops.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void updateBottomBar() {
        int selectedCount = 0;
        double selectedTotal = 0;

        boolean hasItem = false;
        boolean allSelected = true;

        for (CartDetailResponse.ShopCart shop : shops) {
            for (CartDetailResponse.CartItem item : shop.getItems()) {
                hasItem = true;

                if (item.isSelected()) {
                    selectedCount += item.getQuantity();
                    selectedTotal += item.getLineTotal();
                } else {
                    allSelected = false;
                }
            }
        }

        if (!hasItem) allSelected = false;

        bindingSelectAll = true;
        cbSelectAll.setChecked(allSelected);
        bindingSelectAll = false;

        txtSelectedCount.setText("Đã chọn " + selectedCount + " sản phẩm");
        txtCartTotal.setText(formatMoney(selectedTotal));
        btnCheckout.setText("Thanh toán (" + selectedCount + ")");
        btnCheckout.setEnabled(selectedCount > 0);
        btnCheckout.setAlpha(selectedCount > 0 ? 1f : 0.55f);
    }

    private void confirmRemove(CartDetailResponse.CartItem item) {
        if (item == null || item.getVariantId() == null) {
            showToast("Không tìm thấy sản phẩm");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm?")
                .setMessage("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) ->
                        viewModel.removeItem(item.getVariantId())
                )
                .show();
    }

    private void checkoutSelectedItems() {
        if (adapter == null) {
            showToast("Giỏ hàng chưa sẵn sàng");
            return;
        }

        List<CheckoutSelectedShop> selectedShops = adapter.getSelectedCheckoutShops();

        if (selectedShops == null || selectedShops.isEmpty()) {
            showToast("Vui lòng chọn sản phẩm cần thanh toán");
            return;
        }

        StringBuilder debug = new StringBuilder();
        debug.append("Số shop gửi sang checkout: ")
                .append(selectedShops.size())
                .append("\n");

        for (CheckoutSelectedShop shop : selectedShops) {
            debug.append("Shop ")
                    .append(shop.getShopId())
                    .append(" - ")
                    .append(shop.getShopName())
                    .append(": ")
                    .append(shop.getItems().size())
                    .append(" sản phẩm\n");

            for (CheckoutSelectedShop.CheckoutSelectedItem item : shop.getItems()) {
                debug.append("   variantId=")
                        .append(item.getVariantId())
                        .append(", qty=")
                        .append(item.getQuantity())
                        .append("\n");
            }
        }

        android.util.Log.d("CART_CHECKOUT_DEBUG", debug.toString());

        String selectedShopsJson = new Gson().toJson(selectedShops);
        android.util.Log.d("CART_CHECKOUT_JSON", selectedShopsJson);

        showLongToast("Số shop gửi sang checkout: " + selectedShops.size());

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("checkout_source", CheckoutActivity.SOURCE_FROM_CART);
        intent.putExtra("selected_shops_json", selectedShopsJson);
        startActivity(intent);
    }
    private void openProductDetail(CartDetailResponse.CartItem item) {
        if (item == null || item.getProductId() == null) return;

        try {
            Intent intent = new Intent(
                    this,
                    com.gr6.smartcart_android.buyer.product.ProductDetailActivity.class
            );
            intent.putExtra("product_id", item.getProductId());
            startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    private String formatMoney(double value) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(value) + "đ";
    }
}