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
            shops.addAll(cart.getShops());
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
        List<CheckoutSelectedShop> selectedShops = new ArrayList<>();

        for (CartDetailResponse.ShopCart shop : shops) {
            List<CheckoutSelectedShop.CheckoutSelectedItem> selectedItems = new ArrayList<>();

            for (CartDetailResponse.CartItem item : shop.getItems()) {
                if (!item.isSelected()) continue;

                if (item.getVariantId() == null || item.getVariantId() <= 0) {
                    showToast("Thiếu variantId trong giỏ hàng");
                    return;
                }

                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    showToast("Số lượng sản phẩm không hợp lệ");
                    return;
                }

                selectedItems.add(new CheckoutSelectedShop.CheckoutSelectedItem(
                        item.getVariantId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getVariantText(),
                        item.getImageUrl(),
                        item.getPrice(),
                        item.getQuantity()
                ));
            }

            if (!selectedItems.isEmpty()) {
                selectedShops.add(new CheckoutSelectedShop(
                        shop.getShopId(),
                        shop.getShopName(),
                        null,
                        selectedItems
                ));
            }
        }

        if (selectedShops.isEmpty()) {
            showToast("Vui lòng chọn sản phẩm cần thanh toán");
            return;
        }

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("checkout_source", CheckoutActivity.SOURCE_FROM_CART);
        intent.putExtra("selected_shops_json", new Gson().toJson(selectedShops));
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