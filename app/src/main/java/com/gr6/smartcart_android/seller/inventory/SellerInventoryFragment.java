package com.gr6.smartcart_android.seller.inventory;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.PageResponse;
import com.gr6.smartcart_android.seller.inventory.repository.SellerInventoryRepository;
import com.gr6.smartcart_android.seller.inventory.response.InventoryHistoryItem;
import com.gr6.smartcart_android.seller.inventory.response.InventoryVariantItem;
import com.gr6.smartcart_android.seller.product.repository.SellerProductRepository;
import com.gr6.smartcart_android.seller.product.response.ProductResponse;
import com.gr6.smartcart_android.seller.product.response.VariantResponse;
import com.gr6.smartcart_android.seller.shop.repository.SellerShopRepository;
import com.gr6.smartcart_android.seller.shop.response.SellerShopInfoResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SellerInventoryFragment extends Fragment {

    private static final String ARG_LOW_STOCK_ONLY = "arg_low_stock_only";
    private static final int LOW_STOCK_THRESHOLD = 5;

    private SellerInventoryRepository inventoryRepository;
    private SellerProductRepository productRepository;
    private SellerShopRepository shopRepository;

    private InventoryVariantAdapter inventoryAdapter;
    private LowStockAdapter lowStockAdapter;
    private StockHistoryAdapter historyAdapter;

    private ProgressBar progressBar;
    private TextView txtLowStockTitle;
    private TextView txtEmpty;

    private Long shopId;
    private boolean lowStockOnly = false;

    public static SellerInventoryFragment newInstance(boolean lowStockOnly) {
        SellerInventoryFragment fragment = new SellerInventoryFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_LOW_STOCK_ONLY, lowStockOnly);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        Bundle args = getArguments();
        lowStockOnly = args != null && args.getBoolean(ARG_LOW_STOCK_ONLY, false);

        inventoryRepository = new SellerInventoryRepository(requireContext());
        productRepository = new SellerProductRepository(requireContext());
        shopRepository = new SellerShopRepository(requireContext());

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(12), dp(18), dp(18));
        root.setBackgroundResource(R.drawable.bg_seller_screen);

        TextView title = titleText(lowStockOnly ? "Sản phẩm tồn kho thấp" : "Quản lý tồn kho");
        root.addView(title);

        txtLowStockTitle = sectionText("Cảnh báo tồn kho thấp");
        root.addView(txtLowStockTitle);

        RecyclerView rvLowStock = new RecyclerView(requireContext());
        lowStockAdapter = new LowStockAdapter();
        lowStockAdapter.setListener(item -> showAdjustDialog(item, true));
        rvLowStock.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        rvLowStock.setAdapter(lowStockAdapter);
        root.addView(rvLowStock, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(112)
        ));

        TextView listTitle = sectionText("Danh sách tồn kho theo biến thể");
        root.addView(listTitle);

        progressBar = new ProgressBar(requireContext());
        LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        progressLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(progressBar, progressLp);

        txtEmpty = new TextView(requireContext());
        txtEmpty.setText("Chưa có dữ liệu tồn kho");
        txtEmpty.setTextColor(color(R.color.text_secondary));
        txtEmpty.setTextSize(14);
        txtEmpty.setGravity(Gravity.CENTER);
        txtEmpty.setPadding(0, dp(12), 0, dp(12));
        root.addView(txtEmpty, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        RecyclerView rvInventory = new RecyclerView(requireContext());
        inventoryAdapter = new InventoryVariantAdapter();
        inventoryAdapter.setListener(new InventoryVariantAdapter.OnStockActionListener() {
            @Override public void onIncrease(InventoryVariantItem item) { showAdjustDialog(item, true); }
            @Override public void onDecrease(InventoryVariantItem item) { showAdjustDialog(item, false); }
        });
        rvInventory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvInventory.setAdapter(inventoryAdapter);
        root.addView(rvInventory, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        TextView historyTitle = sectionText("Lịch sử nhập/xuất kho tạm thời");
        root.addView(historyTitle);

        RecyclerView rvHistory = new RecyclerView(requireContext());
        historyAdapter = new StockHistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setAdapter(historyAdapter);
        root.addView(rvHistory, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(130)
        ));

        loadInventoryFromProducts();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (productRepository != null) loadInventoryFromProducts();
    }

    private void loadInventoryFromProducts() {
        showLoading(true);
        if (shopId == null || shopId <= 0) {
            shopRepository.loadMyShopInfo(new SellerShopRepository.ShopCallback<SellerShopInfoResponse>() {
                @Override
                public void onSuccess(SellerShopInfoResponse data, String message) {
                    shopId = data == null ? null : data.getShopId();
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> loadInventoryFromProducts());
                }

                @Override
                public void onError(String message) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> showError(message));
                }
            });
            return;
        }

        productRepository.loadProductsByShop(shopId, 1, 100, new SellerProductRepository.ProductCallback<PageResponse<ProductResponse>>() {
            @Override
            public void onSuccess(PageResponse<ProductResponse> data, String message) {
                if (!isAdded()) return;
                List<ProductResponse> products = data == null ? new ArrayList<>() : data.getData();
                requireActivity().runOnUiThread(() -> renderInventory(products));
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> showError(message));
            }
        });
    }

    private void renderInventory(List<ProductResponse> products) {
        showLoading(false);
        List<InventoryVariantItem> variants = new ArrayList<>();
        List<InventoryVariantItem> lowStock = new ArrayList<>();

        if (products != null) {
            for (ProductResponse product : products) {
                if (product == null || product.getVariants() == null) continue;
                for (VariantResponse variant : product.getVariants()) {
                    InventoryVariantItem item = new InventoryVariantItem(product, variant);
                    variants.add(item);
                    if (item.getStock() <= LOW_STOCK_THRESHOLD) {
                        lowStock.add(item);
                    }
                }
            }
        }

        txtLowStockTitle.setText("Cảnh báo tồn kho thấp • " + lowStock.size() + " mặt hàng");
        lowStockAdapter.submitList(lowStock);

        List<InventoryVariantItem> displayList = lowStockOnly ? lowStock : variants;
        inventoryAdapter.submitList(displayList);

        boolean empty = displayList.isEmpty();
        txtEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        txtEmpty.setText(empty
                ? (lowStockOnly ? "Không có sản phẩm nào đang tồn kho thấp." : "Chưa có biến thể tồn kho. Hãy đăng sản phẩm hoặc thêm phân loại hàng.")
                : "");
    }

    private void showAdjustDialog(InventoryVariantItem item, boolean increase) {
        if (item == null || item.getVariant() == null) return;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_stock_adjust, null, false);
        TextView txtDialogTitle = dialogView.findViewById(R.id.txtDialogTitle);
        TextView txtProduct = dialogView.findViewById(R.id.txtProduct);
        EditText edtQuantity = dialogView.findViewById(R.id.edtQuantity);
        EditText edtReason = dialogView.findViewById(R.id.edtReason);
        TextView btnCancel = dialogView.findViewById(R.id.btnCancel);
        TextView btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        txtDialogTitle.setText(increase ? "Nhập thêm kho" : "Giảm/điều chỉnh kho");
        txtProduct.setText(item.getProductName() + " - " + item.getVariant().getAttributeText());
        edtReason.setHint(increase ? "Lý do nhập kho" : "Lý do giảm/điều chỉnh kho");

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String quantityText = edtQuantity.getText().toString().trim();
            String reason = edtReason.getText().toString().trim();

            if (TextUtils.isEmpty(quantityText)) {
                edtQuantity.setError("Nhập số lượng");
                return;
            }
            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
            } catch (Exception e) {
                edtQuantity.setError("Số lượng không hợp lệ");
                return;
            }
            if (quantity <= 0) {
                edtQuantity.setError("Số lượng phải lớn hơn 0");
                return;
            }
            if (!increase && quantity > item.getStock()) {
                edtQuantity.setError("Không được giảm quá số lượng tồn hiện tại");
                return;
            }
            if (TextUtils.isEmpty(reason)) {
                edtReason.setError("Vui lòng nhập lý do");
                return;
            }

            adjustStock(item, quantity, increase, dialog);
        });
        dialog.show();
    }

    private void adjustStock(InventoryVariantItem item, int quantity, boolean increase, AlertDialog dialog) {
        SellerInventoryRepository.InventoryCallback<String> callback = new SellerInventoryRepository.InventoryCallback<String>() {
            @Override
            public void onSuccess(String data, String message) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    addLocalHistory(item, quantity, increase);
                    Toast.makeText(requireContext(), message == null ? "Cập nhật tồn kho thành công" : message, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadInventoryFromProducts();
                });
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show());
            }
        };

        Long variantId = item.getVariant().getVariantId();
        if (increase) {
            inventoryRepository.addStock(variantId, quantity, callback);
        } else {
            inventoryRepository.subtractStock(variantId, quantity, callback);
        }
    }

    private void addLocalHistory(InventoryVariantItem item, int quantity, boolean increase) {
        String time = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(new Date());
        historyAdapter.addFirst(new InventoryHistoryItem(
                increase ? InventoryHistoryItem.Type.IMPORT : InventoryHistoryItem.Type.ADJUSTMENT,
                item.getProductName(),
                time,
                "Seller",
                increase ? quantity : -quantity
        ));
    }

    private void showError(String message) {
        showLoading(false);
        txtEmpty.setVisibility(View.VISIBLE);
        txtEmpty.setText(message == null ? "Không tải được tồn kho" : message);
    }

    private TextView titleText(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextColor(color(R.color.text_primary));
        tv.setTextSize(22);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private TextView sectionText(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextColor(color(R.color.text_primary));
        tv.setTextSize(16);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(0, dp(14), 0, dp(8));
        return tv;
    }

    private void showLoading(boolean loading) {
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private int color(int id) {
        return androidx.core.content.ContextCompat.getColor(requireContext(), id);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
