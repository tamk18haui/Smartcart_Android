package com.gr6.smartcart_android.seller.product;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.seller.utils.ApiErrorUtils;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.seller.cloudinary.CloudinaryUploader;
import com.gr6.smartcart_android.seller.product.request.ProductRequest;
import com.gr6.smartcart_android.seller.product.response.ProductResponse;
import com.gr6.smartcart_android.seller.product.request.ProductVariantRequest;
import com.gr6.smartcart_android.seller.product.repository.SellerProductRepository;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("deprecation")
public class AddProductActivity extends BaseActivity {

    private static final int REQ_PICK_PRODUCT_IMAGES = 2101;
    private static final int REQ_PICK_CATEGORY = 2102;
    private static final int REQ_PICK_BRAND = 2103;
    private static final int REQ_EDIT_VARIANTS = 2104;
    private static final int REQ_EDIT_SHIPPING = 2105;

    public static final String EXTRA_EDIT_PRODUCT_ID = "extra_edit_product_id";

    private static final int MAX_PRODUCT_IMAGES = 9;

    private EditText etProductName;
    private EditText etProductDesc;
    private EditText edtProductPrice;
    private EditText edtProductStock;

    private TextView tvImageCount;
    private TextView tvNameCount;
    private TextView tvDescCount;
    private TextView tvCategoryValue;
    private TextView tvBrandValue;
    private TextView tvVariantValue;
    private TextView tvShippingValue;
    private TextView tvConditionValue;
    private TextView btnPublishProduct;
    private TextView btnSaveDraft;

    private final List<Uri> productImageUris = new ArrayList<>();
    private final List<String> existingImageUrls = new ArrayList<>();
    private final List<ProductVariantRequest> selectedVariants = new ArrayList<>();

    private ImageAdapter imageAdapter;
    private SellerProductRepository repository;

    private Long selectedCategoryId;
    private String selectedBrand;
    private String selectedCondition = "NEW";

    private String weightText = "";
    private String lengthText = "";
    private String widthText = "";
    private String heightText = "";

    private boolean submitting = false;
    private boolean editMode = false;
    private Long editProductId = null;

    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_add_product);
        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        repository = new SellerProductRepository(this);
        long rawEditProductId = getIntent().getLongExtra(EXTRA_EDIT_PRODUCT_ID, -1L);
        if (rawEditProductId > 0) {
            editMode = true;
            editProductId = rawEditProductId;
        }

        bindViews();
        setupImageList();
        bindCounters();
        bindActions();
        renderSelectedState();

        if (editMode) {
            setupEditMode();
            loadProductForEdit();
        }
    }

    private void bindViews() {
        etProductName = findViewById(R.id.etProductName);
        etProductDesc = findViewById(R.id.etProductDesc);
        edtProductPrice = findViewById(R.id.edtProductPrice);
        edtProductStock = findViewById(R.id.edtProductStock);

        tvImageCount = findViewById(R.id.tvImageCount);
        tvNameCount = findViewById(R.id.tvNameCount);
        tvDescCount = findViewById(R.id.tvDescCount);
        tvCategoryValue = findViewById(R.id.tvCategoryValue);
        tvBrandValue = findViewById(R.id.tvBrandValue);
        tvVariantValue = findViewById(R.id.tvVariantValue);
        tvShippingValue = findViewById(R.id.tvShippingValue);
        tvConditionValue = findViewById(R.id.tvConditionValue);

        btnPublishProduct = findViewById(R.id.btnPublishProduct);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
    }

    private void setupImageList() {
        RecyclerView rvProductImages = findViewById(R.id.rvProductImages);
        rvProductImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        imageAdapter = new ImageAdapter(productImageUris, this::updateImageCount);
        rvProductImages.setAdapter(imageAdapter);
        updateImageCount();
    }

    private void bindCounters() {
        addCounter(etProductName, tvNameCount, 120);
        addCounter(etProductDesc, tvDescCount, 3000);
    }

    private void bindActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddImage).setOnClickListener(v -> openProductImagePicker());

        findViewById(R.id.rowCategory).setOnClickListener(v ->
                startActivityForResult(new Intent(this, CategoryPickerActivity.class), REQ_PICK_CATEGORY)
        );

        findViewById(R.id.rowBrand).setOnClickListener(v -> openBrandPicker());

        findViewById(R.id.rowVariants).setOnClickListener(v -> openVariantEditor());

        findViewById(R.id.rowShipping).setOnClickListener(v ->
                startActivityForResult(new Intent(this, ShippingActivity.class), REQ_EDIT_SHIPPING)
        );

        findViewById(R.id.rowCondition).setOnClickListener(v -> showConditionDialog());

        btnPublishProduct.setOnClickListener(v -> submitProduct());

        btnSaveDraft.setOnClickListener(v -> {
            if (editMode) {
                finish();
            } else {
                showToast("Hiện backend chưa có API lưu nháp, dùng nút Hiển thị để đăng sản phẩm");
            }
        });
    }

    private void setupEditMode() {
        btnPublishProduct.setText("Cập nhật");
        btnSaveDraft.setText("Hủy");
    }

    private void loadProductForEdit() {
        if (editProductId == null || editProductId <= 0) {
            showToast("Không tìm thấy sản phẩm cần sửa");
            finish();
            return;
        }

        showLoading();
        repository.loadProductForSeller(editProductId, new SellerProductRepository.ProductCallback<ProductResponse>() {
            @Override
            public void onSuccess(ProductResponse data, String message) {
                runOnUiThread(() -> {
                    hideLoading();
                    fillProductForEdit(data);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    hideLoading();
                    showLongToast(message == null ? "Không tải được sản phẩm cần sửa" : message);
                    finish();
                });
            }
        });
    }

    private void fillProductForEdit(ProductResponse product) {
        if (product == null) return;

        etProductName.setText(product.getName() == null ? "" : product.getName());
        etProductDesc.setText(product.getDescription() == null ? "" : product.getDescription());
        edtProductPrice.setText(product.getBasePrice() == null ? "" : product.getBasePrice().stripTrailingZeros().toPlainString());

        selectedCategoryId = product.getCategoryId();
        tvCategoryValue.setText(selectedCategoryId == null ? "Vui lòng chọn" : "Mã danh mục #" + selectedCategoryId);

        selectedBrand = product.getBrand();
        selectedCondition = product.getCondition() == null ? "NEW" : product.getCondition();
        weightText = product.getWeight() == null ? "" : product.getWeight().stripTrailingZeros().toPlainString();
        lengthText = product.getLength() == null ? "" : product.getLength().stripTrailingZeros().toPlainString();
        widthText = product.getWidth() == null ? "" : product.getWidth().stripTrailingZeros().toPlainString();
        heightText = product.getHeight() == null ? "" : product.getHeight().stripTrailingZeros().toPlainString();

        existingImageUrls.clear();
        if (product.getImages() != null) {
            for (String url : product.getImages()) {
                if (!TextUtils.isEmpty(url)) existingImageUrls.add(url.trim());
            }
        }

        selectedVariants.clear();
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            for (com.gr6.smartcart_android.seller.product.response.VariantResponse variant : product.getVariants()) {
                if (variant == null) continue;
                selectedVariants.add(new ProductVariantRequest(
                        variant.getSku(),
                        variant.getPrice(),
                        variant.getStockQuantity(),
                        variant.getImageUrl(),
                        variant.getAttributes()
                ));
            }
        } else {
            edtProductStock.setText("0");
        }

        renderSelectedState();
    }

    private void openBrandPicker() {
        if (selectedCategoryId == null || selectedCategoryId <= 0) {
            showToast("Vui lòng chọn danh mục trước để lọc thương hiệu phù hợp");
            return;
        }

        Intent intent = new Intent(this, BrandPickerActivity.class);
        intent.putExtra("category_id", selectedCategoryId);
        intent.putExtra("category_name", tvCategoryValue == null ? "" : tvCategoryValue.getText().toString());
        startActivityForResult(intent, REQ_PICK_BRAND);
    }

    private void openProductImagePicker() {
        if (productImageUris.size() + existingImageUrls.size() >= MAX_PRODUCT_IMAGES) {
            showToast("Tối đa " + MAX_PRODUCT_IMAGES + " ảnh sản phẩm");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQ_PICK_PRODUCT_IMAGES);
    }

    private void openVariantEditor() {
        Intent intent = new Intent(this, VariantEditorActivity.class);
        intent.putExtra("base_price", rawPriceText());
        intent.putExtra("variants_json", gson.toJson(selectedVariants));
        startActivityForResult(intent, REQ_EDIT_VARIANTS);
    }

    private void showConditionDialog() {
        String[] labels = {"Mới", "Đã sử dụng"};
        String[] values = {"NEW", "USED"};

        int checked = "USED".equals(selectedCondition) ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle("Chọn tình trạng sản phẩm")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    selectedCondition = values[which];
                    renderSelectedState();
                    dialog.dismiss();
                })
                .show();
    }

    private void submitProduct() {
        if (submitting) return;

        ProductRequest request = buildAndValidateRequest();
        if (request == null) return;

        submitting = true;
        setSubmitEnabled(false);
        showLoading();

        CloudinaryUploader.uploadMany(this, productImageUris, new CloudinaryUploader.UploadManyCallback() {
            @Override
            public void onProgress(int current, int total) {
                runOnUiThread(() -> {
                    if (total > 0) {
                        showToast("Upload ảnh sản phẩm " + current + "/" + total);
                    }
                });
            }

            @Override
            public void onSuccess(List<String> urls) {
                runOnUiThread(() -> {
                    List<String> finalImageUrls = new ArrayList<>();
                    finalImageUrls.addAll(existingImageUrls);
                    if (urls != null) finalImageUrls.addAll(urls);
                    request.setUploadImages(finalImageUrls);
                    submitProductToBackend(request);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    submitting = false;
                    setSubmitEnabled(true);
                    hideLoading();
                    showLongToast(message == null ? "Upload ảnh thất bại" : message);
                });
            }
        });
    }

    private ProductRequest buildAndValidateRequest() {
        String name = textOf(etProductName);
        String description = textOf(etProductDesc);

        if (productImageUris.isEmpty() && existingImageUrls.isEmpty()) {
            showToast("Vui lòng thêm ít nhất 1 ảnh sản phẩm");
            return null;
        }

        if (TextUtils.isEmpty(name)) {
            showToast("Vui lòng nhập tên sản phẩm");
            return null;
        }

        if (selectedCategoryId == null) {
            showToast("Vui lòng chọn danh mục");
            return null;
        }

        BigDecimal basePrice = parsePrice(rawPriceText());
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            showToast("Vui lòng nhập giá hợp lệ");
            return null;
        }

        Integer stockQuantity = null;
        if (selectedVariants.isEmpty()) {
            stockQuantity = parseStock(textOf(edtProductStock));
            if (stockQuantity == null || stockQuantity < 0) {
                showToast("Vui lòng nhập kho hàng hợp lệ");
                return null;
            }
        }

        BigDecimal weight = parseDecimal(weightText);
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0) {
            showToast("Vui lòng nhập phí vận chuyển/cân nặng");
            return null;
        }

        ProductRequest request = new ProductRequest();
        request.setCategoryId(selectedCategoryId);
        request.setName(name);
        request.setDescription(description);
        request.setBrand(selectedBrand);
        request.setCondition(selectedCondition);
        request.setBasePrice(basePrice);
        request.setWeight(weight);
        request.setLength(parseDecimal(lengthText));
        request.setWidth(parseDecimal(widthText));
        request.setHeight(parseDecimal(heightText));
        request.setStockQuantity(stockQuantity);
        request.setVariants(selectedVariants.isEmpty() ? null : new ArrayList<>(selectedVariants));

        return request;
    }

    private void submitProductToBackend(ProductRequest request) {
        Call<BaseResponse<ProductResponse>> call = editMode
                ? repository.updateProduct(editProductId, request)
                : repository.createProduct(request);

        call.enqueue(new Callback<BaseResponse<ProductResponse>>() {
            @Override
            public void onResponse(
                    Call<BaseResponse<ProductResponse>> call,
                    Response<BaseResponse<ProductResponse>> response
            ) {
                submitting = false;
                setSubmitEnabled(true);
                hideLoading();

                BaseResponse<ProductResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    showToast(editMode ? "Cập nhật sản phẩm thành công" : "Đăng sản phẩm thành công");
                    setResult(RESULT_OK);
                    finish();
                    return;
                }

                String message;
                if (body != null) {
                    message = body.getSafeMessage();
                } else {
                    message = ApiErrorUtils.extractErrorMessage(response);
                }
                showLongToast((editMode ? "Cập nhật sản phẩm thất bại: " : "Đăng sản phẩm thất bại: ") + message);
            }

            @Override
            public void onFailure(Call<BaseResponse<ProductResponse>> call, Throwable t) {
                submitting = false;
                setSubmitEnabled(true);
                hideLoading();
                showLongToast("Lỗi kết nối server: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == REQ_PICK_PRODUCT_IMAGES) {
            handlePickedProductImages(data);
            return;
        }

        if (requestCode == REQ_PICK_CATEGORY) {
            Long oldCategoryId = selectedCategoryId;
            selectedCategoryId = data.getLongExtra("category_id", -1L);
            if (selectedCategoryId <= 0) selectedCategoryId = null;
            tvCategoryValue.setText(data.getStringExtra("category_name"));

            if (oldCategoryId == null || !oldCategoryId.equals(selectedCategoryId)) {
                selectedBrand = null;
                tvBrandValue.setText("Vui lòng chọn thương hiệu");
            }

            renderSelectedState();
            return;
        }

        if (requestCode == REQ_PICK_BRAND) {
            selectedBrand = data.getStringExtra("brand");
            renderSelectedState();
            return;
        }

        if (requestCode == REQ_EDIT_VARIANTS) {
            String json = data.getStringExtra("variants_json");
            selectedVariants.clear();

            if (!TextUtils.isEmpty(json)) {
                Type type = new TypeToken<List<ProductVariantRequest>>() {}.getType();
                List<ProductVariantRequest> variants = gson.fromJson(json, type);
                if (variants != null) {
                    selectedVariants.addAll(variants);
                }
            }

            renderSelectedState();
            return;
        }

        if (requestCode == REQ_EDIT_SHIPPING) {
            weightText = data.getStringExtra("weight");
            lengthText = data.getStringExtra("length");
            widthText = data.getStringExtra("width");
            heightText = data.getStringExtra("height");
            renderSelectedState();
        }
    }

    private void handlePickedProductImages(Intent data) {
        int remaining = MAX_PRODUCT_IMAGES - productImageUris.size() - existingImageUrls.size();

        if (data.getClipData() != null) {
            int count = Math.min(data.getClipData().getItemCount(), remaining);
            for (int i = 0; i < count; i++) {
                addProductImageUri(data.getClipData().getItemAt(i).getUri(), data);
            }
        } else if (data.getData() != null && remaining > 0) {
            addProductImageUri(data.getData(), data);
        }

        imageAdapter.notifyDataSetChanged();
        updateImageCount();
    }

    private void addProductImageUri(Uri uri, Intent sourceIntent) {
        if (uri == null) return;
        if (productImageUris.contains(uri)) return;
        if (productImageUris.size() >= MAX_PRODUCT_IMAGES) return;

        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    sourceIntent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {
        }

        productImageUris.add(uri);
    }

    private void renderSelectedState() {
        tvCategoryValue.setText(selectedCategoryId == null ? "Vui lòng chọn" : tvCategoryValue.getText());

        if (TextUtils.isEmpty(selectedBrand)) {
            tvBrandValue.setText("No Brand");
        } else {
            tvBrandValue.setText(selectedBrand);
        }

        if (selectedVariants.isEmpty()) {
            tvVariantValue.setText("Chưa thiết lập");
            edtProductStock.setEnabled(true);
            edtProductStock.setHint("Nhập kho hàng");
        } else {
            tvVariantValue.setText(selectedVariants.size() + " biến thể");
            edtProductStock.setText("");
            edtProductStock.setEnabled(false);
            edtProductStock.setHint("Kho lấy theo từng biến thể");
        }

        if (TextUtils.isEmpty(weightText)) {
            tvShippingValue.setText("Thiết lập vận chuyển");
        } else {
            String size = "";
            if (!TextUtils.isEmpty(lengthText) || !TextUtils.isEmpty(widthText) || !TextUtils.isEmpty(heightText)) {
                size = " - " + safe(lengthText) + "x" + safe(widthText) + "x" + safe(heightText) + "cm";
            }
            tvShippingValue.setText(weightText + "g" + size);
        }

        tvConditionValue.setText("USED".equals(selectedCondition) ? "Đã sử dụng" : "Mới");
        updateImageCount();
    }

    private void updateImageCount() {
        if (tvImageCount != null) {
            int total = productImageUris.size() + existingImageUrls.size();
            tvImageCount.setText(total + "/" + MAX_PRODUCT_IMAGES + " ảnh");
        }
    }

    private void addCounter(EditText editText, TextView counter, int max) {
        counter.setText("0/" + max);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                counter.setText(s.length() + "/" + max);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setSubmitEnabled(boolean enabled) {
        btnPublishProduct.setEnabled(enabled);
        btnPublishProduct.setAlpha(enabled ? 1f : 0.55f);
    }

    private String textOf(EditText editText) {
        if (editText == null || editText.getText() == null) return "";
        return editText.getText().toString().trim();
    }

    private String rawPriceText() {
        return textOf(edtProductPrice);
    }

    private BigDecimal parsePrice(String raw) {
        if (TextUtils.isEmpty(raw)) return null;

        try {
            String cleaned = raw
                    .replace("đ", "")
                    .replace("₫", "")
                    .replace(".", "")
                    .replace(",", "")
                    .replace(" ", "")
                    .trim();

            if (TextUtils.isEmpty(cleaned)) return null;
            return new BigDecimal(cleaned);
        } catch (Exception ignored) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String raw) {
        if (TextUtils.isEmpty(raw)) return null;

        try {
            String cleaned = raw
                    .replace(",", ".")
                    .replaceAll("[^0-9.]", "")
                    .trim();

            if (TextUtils.isEmpty(cleaned)) return null;
            return new BigDecimal(cleaned);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer parseStock(String raw) {
        if (TextUtils.isEmpty(raw)) return null;

        try {
            String cleaned = raw.replaceAll("[^0-9]", "").trim();
            if (TextUtils.isEmpty(cleaned)) return null;
            return Integer.parseInt(cleaned);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String safe(String value) {
        return TextUtils.isEmpty(value) ? "0" : value;
    }
}