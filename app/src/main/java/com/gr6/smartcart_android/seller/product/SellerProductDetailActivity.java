package com.gr6.smartcart_android.seller.product;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.seller.inventory.SellerInventoryViewModel;
import com.gr6.smartcart_android.seller.product.repository.SellerProductRepository;
import com.gr6.smartcart_android.seller.product.response.ProductResponse;
import com.gr6.smartcart_android.seller.product.response.VariantResponse;
import com.gr6.smartcart_android.seller.voucher.CreateVoucherActivity;
import com.gr6.smartcart_android.seller.review.SellerProductReviewsActivity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SellerProductDetailActivity extends BaseActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private long currentProductId = -1L;
    private boolean pendingInventoryReload = false;

    private ImageView imgProductMain;
    private ProgressBar progressBar;

    private TextView btnBack;
    private TextView txtProductStatus;
    private TextView txtProductName;
    private TextView txtProductSku;

    private TextView txtRevenue;
    private TextView txtSold;
    private TextView txtConversion;
    private TextView txtViews;

    private LinearLayout stockContainer;

    private TextView txtRetailPrice;
    private TextView txtSalePrice;
    private TextView txtWholesalePrice;

    private TextView txtCategory;
    private TextView txtBrand;
    private TextView txtSize;
    private TextView txtWeight;
    private TextView txtCondition;

    private TextView txtDescription;

    private TextView btnCreateVoucher;
    private TextView btnViewReviews;
    private TextView btnHideProduct;
    private TextView btnEditProduct;

    private SellerProductViewModel productViewModel;
    private SellerInventoryViewModel inventoryViewModel;
    private SellerProductRepository productRepository;
    private ProductResponse currentProduct;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);
        setContentView(R.layout.activity_seller_product_detail);

        currentProductId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1L);

        initViews();
        initActions();

        productViewModel = new ViewModelProvider(this).get(SellerProductViewModel.class);
        inventoryViewModel = new ViewModelProvider(this).get(SellerInventoryViewModel.class);
        productRepository = new SellerProductRepository(this);

        observeProductViewModel();
        observeInventoryViewModel();

        if (currentProductId <= 0) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productViewModel.loadProductForSeller(currentProductId);
    }

    private void initViews() {
        imgProductMain = findViewById(R.id.imgProductMain);
        progressBar = findViewById(R.id.progressBar);

        btnBack = findViewById(R.id.btnBack);
        txtProductStatus = findViewById(R.id.txtProductStatus);
        txtProductName = findViewById(R.id.txtProductName);
        txtProductSku = findViewById(R.id.txtProductSku);

        txtRevenue = findViewById(R.id.txtRevenue);
        txtSold = findViewById(R.id.txtSold);
        txtConversion = findViewById(R.id.txtConversion);
        txtViews = findViewById(R.id.txtViews);

        stockContainer = findViewById(R.id.stockContainer);

        txtRetailPrice = findViewById(R.id.txtRetailPrice);
        txtSalePrice = findViewById(R.id.txtSalePrice);
        txtWholesalePrice = findViewById(R.id.txtWholesalePrice);

        txtCategory = findViewById(R.id.txtCategory);
        txtBrand = findViewById(R.id.txtBrand);
        txtSize = findViewById(R.id.txtSize);
        txtWeight = findViewById(R.id.txtWeight);
        txtCondition = findViewById(R.id.txtCondition);

        txtDescription = findViewById(R.id.txtDescription);

        btnCreateVoucher = findViewById(R.id.btnCreateVoucher);
        btnViewReviews = findViewById(R.id.btnViewReviews);
        btnHideProduct = findViewById(R.id.btnHideProduct);
        btnEditProduct = findViewById(R.id.btnEditProduct);
    }

    private void initActions() {
        btnBack.setOnClickListener(v -> finish());

        btnCreateVoucher.setOnClickListener(v ->
                startActivity(new Intent(this, CreateVoucherActivity.class))
        );

        btnViewReviews.setOnClickListener(v -> openProductReviews());

        btnHideProduct.setOnClickListener(v -> confirmToggleProductVisibility());

        btnEditProduct.setOnClickListener(v -> openEditProduct());
    }

    private void observeProductViewModel() {
        productViewModel.getProductDetailState().observe(this, state -> {
            if (state == null) return;

            showPageLoading(state.isLoading());

            if (state.isSuccess()) {
                currentProduct = state.getProduct();
                renderProduct(currentProduct);
            } else if (state.isError()) {
                Toast.makeText(
                        this,
                        state.getMessage() == null ? "Không tải được chi tiết sản phẩm" : state.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void observeInventoryViewModel() {
        inventoryViewModel.getActionState().observe(this, state -> {
            if (state == null) return;

            showPageLoading(state.isLoading());

            if (state.isSuccess() && pendingInventoryReload) {
                pendingInventoryReload = false;

                Toast.makeText(
                        this,
                        state.getMessage() == null ? "Cập nhật tồn kho thành công" : state.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();

                productViewModel.loadProductForSeller(currentProductId);
            } else if (state.isError() && pendingInventoryReload) {
                pendingInventoryReload = false;

                Toast.makeText(
                        this,
                        state.getMessage() == null ? "Cập nhật tồn kho thất bại" : state.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void openEditProduct() {
        if (currentProductId <= 0) {
            Toast.makeText(this, "Không tìm thấy sản phẩm cần sửa", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AddProductActivity.class);
        intent.putExtra(AddProductActivity.EXTRA_EDIT_PRODUCT_ID, currentProductId);
        startActivity(intent);
    }

    private void confirmToggleProductVisibility() {
        if (currentProduct == null || currentProduct.getProductId() == null) {
            Toast.makeText(this, "Chưa tải xong sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hiddenNow = "HIDDEN".equals(normalizeStatus(currentProduct.getStatus()));
        boolean nextHidden = !hiddenNow;

        new AlertDialog.Builder(this)
                .setTitle(nextHidden ? "Tạm ẩn sản phẩm?" : "Hiển thị lại sản phẩm?")
                .setMessage(nextHidden
                        ? "Sản phẩm sẽ không còn hiển thị cho người mua."
                        : "Sản phẩm sẽ được mở bán lại cho người mua.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton(nextHidden ? "Tạm ẩn" : "Hiển thị", (dialog, which) -> toggleProductVisibility(nextHidden))
                .show();
    }

    private void toggleProductVisibility(boolean hidden) {
        showPageLoading(true);
        productRepository.setProductHidden(currentProductId, hidden, new SellerProductRepository.ProductCallback<ProductResponse>() {
            @Override
            public void onSuccess(ProductResponse data, String message) {
                runOnUiThread(() -> {
                    showPageLoading(false);
                    currentProduct = data;
                    renderProduct(currentProduct);
                    Toast.makeText(
                            SellerProductDetailActivity.this,
                            message == null ? (hidden ? "Đã tạm ẩn sản phẩm" : "Đã hiển thị sản phẩm") : message,
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    showPageLoading(false);
                    Toast.makeText(
                            SellerProductDetailActivity.this,
                            message == null ? "Cập nhật trạng thái sản phẩm thất bại" : message,
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void renderProduct(ProductResponse product) {
        if (product == null) {
            Toast.makeText(this, "Không có dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentProductId = product.getProductId() == null ? currentProductId : product.getProductId();

        ImageLoader.load(this, product.getFirstImage(), imgProductMain);

        txtProductStatus.setText(statusText(product.getStatus()));
        txtProductStatus.setTextColor(ContextCompat.getColor(this, statusTextColor(product.getStatus())));
        updateHideButton(product.getStatus());

        txtProductName.setText(product.getName());
        txtProductSku.setText(buildSkuText(product));

        int sold = product.getSoldQuantity() == null ? 0 : product.getSoldQuantity();

        txtRevenue.setText(formatMoney(calculateRevenue(product)));
        txtSold.setText(String.valueOf(sold));
        txtConversion.setText(formatRating(product.getAverageRating()));
        txtViews.setText(String.valueOf(product.getReviewCount()));

        renderStock(product.getVariants());

        txtRetailPrice.setText(formatMoney(product.getBasePrice()));
        txtSalePrice.setText(formatMoney(product.getBasePrice()));
        txtWholesalePrice.setText(formatMoney(product.getBasePrice()));

        txtCategory.setText(product.getCategoryId() == null
                ? "Chưa có danh mục"
                : "Mã danh mục #" + product.getCategoryId());

        txtBrand.setText(isTextEmpty(product.getBrand())
                ? "Chưa có thương hiệu"
                : product.getBrand());

        txtSize.setText(buildSizeText(product));

        txtWeight.setText(product.getWeight() == null
                ? "Chưa cập nhật"
                : formatDecimal(product.getWeight()) + "g");

        txtCondition.setText(isTextEmpty(product.getCondition())
                ? "Chưa cập nhật"
                : product.getCondition());

        txtDescription.setText(isTextEmpty(product.getDescription())
                ? "Chưa có mô tả sản phẩm"
                : product.getDescription());
    }

    private void openProductReviews() {
        if (currentProduct == null || currentProduct.getProductId() == null || currentProduct.getProductId() <= 0) {
            Toast.makeText(this, "Chưa tải xong sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, SellerProductReviewsActivity.class);
        intent.putExtra(SellerProductReviewsActivity.EXTRA_PRODUCT_ID, currentProduct.getProductId());
        intent.putExtra(SellerProductReviewsActivity.EXTRA_PRODUCT_NAME, currentProduct.getName());
        startActivity(intent);
    }

    private void updateHideButton(String status) {
        if (btnHideProduct == null) return;
        boolean hidden = "HIDDEN".equals(normalizeStatus(status));
        btnHideProduct.setText(hidden ? "HIỂN THỊ LẠI" : "TẠM ẨN");
    }

    private void renderStock(List<VariantResponse> variants) {
        stockContainer.removeAllViews();

        List<VariantResponse> safeVariants = variants == null ? new ArrayList<>() : variants;

        if (safeVariants.isEmpty()) {
            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_seller_product_stock, stockContainer, false);

            TextView txtVariantName = row.findViewById(R.id.txtVariantName);
            TextView txtVariantSku = row.findViewById(R.id.txtVariantSku);
            TextView txtVariantStock = row.findViewById(R.id.txtVariantStock);
            TextView btnEditStock = row.findViewById(R.id.btnEditStock);

            txtVariantName.setText("Phân loại mặc định");
            txtVariantSku.setText("Sản phẩm chưa có biến thể để quản lý kho");
            txtVariantStock.setText("0");
            btnEditStock.setEnabled(false);
            btnEditStock.setAlpha(0.45f);

            stockContainer.addView(row);
            return;
        }

        for (VariantResponse variant : safeVariants) {
            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_seller_product_stock, stockContainer, false);

            TextView txtVariantName = row.findViewById(R.id.txtVariantName);
            TextView txtVariantSku = row.findViewById(R.id.txtVariantSku);
            TextView txtVariantStock = row.findViewById(R.id.txtVariantStock);
            TextView btnEditStock = row.findViewById(R.id.btnEditStock);

            txtVariantName.setText(buildVariantName(variant));
            txtVariantSku.setText(isTextEmpty(variant.getSku()) ? "Chưa có SKU" : "SKU: " + variant.getSku());
            txtVariantStock.setText(String.valueOf(variant.getStockQuantity()));

            btnEditStock.setOnClickListener(v -> showUpdateStockDialog(variant));

            stockContainer.addView(row);
        }
    }

    private void showUpdateStockDialog(VariantResponse variant) {
        if (variant == null || variant.getVariantId() == null || variant.getVariantId() <= 0) {
            Toast.makeText(this, "Không tìm thấy biến thể cần sửa kho", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_seller_update_stock, null, false);

        TextView txtDialogVariantName = dialogView.findViewById(R.id.txtDialogVariantName);
        TextView txtDialogCurrentStock = dialogView.findViewById(R.id.txtDialogCurrentStock);
        EditText edtNewStock = dialogView.findViewById(R.id.edtNewStock);

        int currentStock = variant.getStockQuantity();

        txtDialogVariantName.setText(buildVariantName(variant));
        txtDialogCurrentStock.setText("Tồn kho hiện tại: " + currentStock);
        edtNewStock.setText(String.valueOf(currentStock));
        edtNewStock.setSelection(edtNewStock.getText().length());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Điều chỉnh tồn kho")
                .setView(dialogView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String rawValue = edtNewStock.getText() == null ? "" : edtNewStock.getText().toString().trim();

            if (rawValue.isEmpty()) {
                edtNewStock.setError("Vui lòng nhập số lượng mới");
                return;
            }

            int newStock;
            try {
                newStock = Integer.parseInt(rawValue);
            } catch (NumberFormatException e) {
                edtNewStock.setError("Số lượng không hợp lệ");
                return;
            }

            if (newStock < 0) {
                edtNewStock.setError("Số lượng không được âm");
                return;
            }

            dialog.dismiss();
            updateStockToValue(variant, newStock);
        }));

        dialog.show();
    }

    private void updateStockToValue(VariantResponse variant, int newStock) {
        int currentStock = variant.getStockQuantity();
        int delta = newStock - currentStock;

        if (delta == 0) {
            Toast.makeText(this, "Số lượng tồn kho không thay đổi", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingInventoryReload = true;

        if (delta > 0) {
            inventoryViewModel.increaseStock(variant.getVariantId(), delta);
        } else {
            inventoryViewModel.decreaseStock(variant.getVariantId(), Math.abs(delta));
        }
    }

    private BigDecimal calculateRevenue(ProductResponse product) {
        if (product == null) {
            return BigDecimal.ZERO;
        }

        Long serverRevenue = product.getTotalRevenue();
        if (serverRevenue != null && serverRevenue > 0) {
            return BigDecimal.valueOf(serverRevenue);
        }

        if (product.getBasePrice() == null) {
            return BigDecimal.ZERO;
        }

        return product.getBasePrice().multiply(BigDecimal.valueOf(product.getSoldQuantity()));
    }

    private String formatRating(Double rating) {
        if (rating == null || rating <= 0) {
            return "--";
        }

        return new DecimalFormat("0.0").format(rating) + " ★";
    }

    private String buildSkuText(ProductResponse product) {
        if (product == null || product.getVariants().isEmpty()) {
            return "SKU: SP-" + (product == null || product.getProductId() == null
                    ? "N/A"
                    : product.getProductId());
        }

        VariantResponse firstVariant = product.getVariants().get(0);

        if (!isTextEmpty(firstVariant.getSku())) {
            return "SKU: " + firstVariant.getSku();
        }

        return "SKU: SP-" + (product.getProductId() == null ? "N/A" : product.getProductId());
    }

    private String buildVariantName(VariantResponse variant) {
        if (variant == null) {
            return "Phân loại mặc định";
        }

        Map<String, String> attrs = variant.getAttributes();

        if (attrs == null || attrs.isEmpty()) {
            return "Phân loại mặc định";
        }

        List<String> parts = new ArrayList<>();

        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            if (!isTextEmpty(entry.getKey()) && !isTextEmpty(entry.getValue())) {
                parts.add(entry.getKey() + ": " + entry.getValue());
            }
        }

        return parts.isEmpty() ? "Phân loại mặc định" : TextUtils.join(" / ", parts);
    }

    private String buildSizeText(ProductResponse product) {
        if (product == null) {
            return "Chưa cập nhật";
        }

        if (product.getLength() == null && product.getWidth() == null && product.getHeight() == null) {
            return "Chưa cập nhật";
        }

        return formatDecimal(product.getLength()) + " x "
                + formatDecimal(product.getWidth()) + " x "
                + formatDecimal(product.getHeight()) + " cm";
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0đ";
        }

        return moneyFormat.format(value) + "đ";
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "0";
        }

        return value.stripTrailingZeros().toPlainString();
    }

    private String statusText(String status) {
        String normalized = normalizeStatus(status);

        switch (normalized) {
            case "ACTIVE":
                return "Đang hoạt động";
            case "INACTIVE":
            case "HIDDEN":
                return "Tạm ẩn";
            case "DELETED":
                return "Đã xóa";
            default:
                return normalized;
        }
    }

    private int statusTextColor(String status) {
        String normalized = normalizeStatus(status);

        switch (normalized) {
            case "ACTIVE":
                return R.color.success;
            case "INACTIVE":
            case "HIDDEN":
                return R.color.warning;
            case "DELETED":
                return R.color.danger;
            default:
                return R.color.text_secondary;
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "ACTIVE";
        }

        return status.trim().toUpperCase();
    }

    private boolean isTextEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void showPageLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }
}


