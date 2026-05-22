package com.gr6.smartcart_android.seller.product;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.seller.cloudinary.CloudinaryUploader;
import com.gr6.smartcart_android.seller.model.ProductVariantRequest;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class VariantEditorActivity extends BaseActivity {

    private static final int REQ_PICK_VARIANT_IMAGE = 2201;

    private EditText edtGroupName;
    private EditText edtGroupValues;
    private LinearLayout groupContainer;
    private LinearLayout variantContainer;

    private final List<VariantGroup> groups = new ArrayList<>();
    private final List<VariantRow> rows = new ArrayList<>();
    private final List<EditText> priceInputs = new ArrayList<>();
    private final List<EditText> stockInputs = new ArrayList<>();

    private int pendingImageRowIndex = -1;
    private String basePrice = "";
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_variant_editor);
        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        basePrice = getIntent().getStringExtra("base_price");
        bindViews();
        bindActions();
        loadOldVariants();
        renderAll();
    }

    private void bindViews() {
        edtGroupName = findViewById(R.id.edtVariantGroupName);
        edtGroupValues = findViewById(R.id.edtVariantGroupValues);
        groupContainer = findViewById(R.id.variantGroupContainer);
        variantContainer = findViewById(R.id.variantRowContainer);
    }

    private void bindActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddVariantGroup).setOnClickListener(v -> addGroup());
        findViewById(R.id.btnGenerateVariants).setOnClickListener(v -> generateRows());
        findViewById(R.id.btnDoneVariants).setOnClickListener(v -> finishWithVariants());
    }

    private void loadOldVariants() {
        String json = getIntent().getStringExtra("variants_json");
        if (TextUtils.isEmpty(json)) return;

        Type type = new TypeToken<List<ProductVariantRequest>>() {}.getType();
        List<ProductVariantRequest> old = gson.fromJson(json, type);
        if (old == null) return;

        for (ProductVariantRequest request : old) {
            VariantRow row = new VariantRow();
            row.attributes.putAll(request.getAttributes() == null ? new LinkedHashMap<>() : request.getAttributes());
            row.priceText = request.getPrice() == null ? basePrice : request.getPrice().toPlainString();
            row.stockText = request.getStockQuantity() == null ? "0" : String.valueOf(request.getStockQuantity());
            row.imageUrl = request.getImageUrl();
            rows.add(row);
        }
    }

    private void addGroup() {
        String name = textOf(edtGroupName);
        String valuesRaw = textOf(edtGroupValues);

        if (TextUtils.isEmpty(name)) {
            showToast("Nhập tên nhóm phân loại, ví dụ: Màu sắc");
            return;
        }

        List<String> values = splitValues(valuesRaw);
        if (values.isEmpty()) {
            showToast("Nhập giá trị, ví dụ: Đen, Trắng, M");
            return;
        }

        VariantGroup group = new VariantGroup();
        group.name = name;
        group.values.addAll(values);
        groups.add(group);

        edtGroupName.setText("");
        edtGroupValues.setText("");

        renderAll();
    }

    private void generateRows() {
        if (groups.isEmpty()) {
            showToast("Hãy thêm ít nhất một nhóm phân loại");
            return;
        }

        rows.clear();
        buildCombinations(0, new LinkedHashMap<>());
        renderAll();
    }

    private void buildCombinations(int groupIndex, LinkedHashMap<String, String> current) {
        if (groupIndex >= groups.size()) {
            VariantRow row = new VariantRow();
            row.attributes.putAll(current);
            row.priceText = TextUtils.isEmpty(basePrice) ? "" : basePrice;
            row.stockText = "";
            rows.add(row);
            return;
        }

        VariantGroup group = groups.get(groupIndex);
        for (String value : group.values) {
            current.put(group.name, value);
            buildCombinations(groupIndex + 1, current);
            current.remove(group.name);
        }
    }

    private void renderAll() {
        renderGroups();
        renderRows();
    }

    private void renderGroups() {
        groupContainer.removeAllViews();

        for (int i = 0; i < groups.size(); i++) {
            VariantGroup group = groups.get(i);
            TextView view = new TextView(this);
            view.setText(group.name + ": " + TextUtils.join(", ", group.values));
            view.setTextColor(ThemeColor.textPrimary(this));
            view.setTextSize(15);
            view.setPadding(dp(14), dp(10), dp(14), dp(10));
            view.setBackgroundResource(R.drawable.bg_seller_chip_soft);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, dp(8));
            view.setLayoutParams(params);

            final int index = i;
            view.setOnLongClickListener(v -> {
                groups.remove(index);
                renderAll();
                return true;
            });

            groupContainer.addView(view);
        }
    }

    private void renderRows() {
        variantContainer.removeAllViews();
        priceInputs.clear();
        stockInputs.clear();

        for (int i = 0; i < rows.size(); i++) {
            VariantRow row = rows.get(i);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setBackgroundResource(R.drawable.bg_seller_card);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, dp(10));
            card.setLayoutParams(cardParams);

            TextView title = new TextView(this);
            title.setText(formatAttributes(row.attributes));
            title.setTextSize(15);
            title.setTextColor(ThemeColor.textPrimary(this));
            title.setPadding(0, 0, 0, dp(8));
            card.addView(title);

            EditText price = makeInput("Giá biến thể");
            price.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            price.setText(row.priceText);
            card.addView(price);
            priceInputs.add(price);

            EditText stock = makeInput("Kho hàng");
            stock.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            stock.setText(row.stockText);
            card.addView(stock);
            stockInputs.add(stock);

            TextView imageButton = new TextView(this);
            imageButton.setText(row.imageUri == null && TextUtils.isEmpty(row.imageUrl) ? "+ Thêm ảnh biến thể" : "Đã chọn ảnh biến thể");
            imageButton.setTextColor(ThemeColor.primary(this));
            imageButton.setTextSize(14);
            imageButton.setPadding(0, dp(8), 0, 0);
            final int rowIndex = i;
            imageButton.setOnClickListener(v -> openImagePicker(rowIndex));
            card.addView(imageButton);

            variantContainer.addView(card);
        }
    }

    private EditText makeInput(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setSingleLine(true);
        editText.setTextSize(14);
        editText.setPadding(dp(12), 0, dp(12), 0);
        editText.setBackgroundResource(R.drawable.bg_seller_input);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
        );
        params.setMargins(0, 0, 0, dp(8));
        editText.setLayoutParams(params);
        return editText;
    }

    private void openImagePicker(int rowIndex) {
        pendingImageRowIndex = rowIndex;

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQ_PICK_VARIANT_IMAGE);
    }

    private void finishWithVariants() {
        if (rows.isEmpty()) {
            Intent result = new Intent();
            result.putExtra("variants_json", "[]");
            setResult(RESULT_OK, result);
            finish();
            return;
        }

        if (!collectAndValidateRows()) {
            return;
        }

        List<Uri> imageUris = new ArrayList<>();
        List<VariantRow> imageRows = new ArrayList<>();

        for (VariantRow row : rows) {
            if (row.imageUri != null) {
                imageUris.add(row.imageUri);
                imageRows.add(row);
            }
        }

        showLoading();
        CloudinaryUploader.uploadMany(this, imageUris, new CloudinaryUploader.UploadManyCallback() {
            @Override
            public void onProgress(int current, int total) {
                runOnUiThread(() -> {
                    if (total > 0) showToast("Upload ảnh biến thể " + current + "/" + total);
                });
            }

            @Override
            public void onSuccess(List<String> urls) {
                runOnUiThread(() -> {
                    for (int i = 0; i < urls.size() && i < imageRows.size(); i++) {
                        imageRows.get(i).imageUrl = urls.get(i);
                    }
                    hideLoading();
                    returnResult();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    hideLoading();
                    showLongToast(message);
                });
            }
        });
    }

    private boolean collectAndValidateRows() {
        for (int i = 0; i < rows.size(); i++) {
            VariantRow row = rows.get(i);
            row.priceText = priceInputs.get(i).getText().toString().trim();
            row.stockText = stockInputs.get(i).getText().toString().trim();

            if (TextUtils.isEmpty(row.priceText)) {
                showToast("Nhập giá cho biến thể " + (i + 1));
                return false;
            }

            if (TextUtils.isEmpty(row.stockText)) {
                showToast("Nhập kho cho biến thể " + (i + 1));
                return false;
            }

            try {
                new BigDecimal(row.priceText);
                Integer.parseInt(row.stockText);
            } catch (Exception e) {
                showToast("Giá/kho biến thể không hợp lệ");
                return false;
            }
        }

        return true;
    }

    private void returnResult() {
        List<ProductVariantRequest> requests = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            VariantRow row = rows.get(i);
            ProductVariantRequest request = new ProductVariantRequest();
            request.setSku("SKU-" + System.currentTimeMillis() + "-" + (i + 1));
            request.setPrice(new BigDecimal(row.priceText));
            request.setStockQuantity(Integer.parseInt(row.stockText));
            request.setImageUrl(row.imageUrl);
            request.setAttributes(row.attributes);
            requests.add(request);
        }

        Intent result = new Intent();
        result.putExtra("variants_json", gson.toJson(requests));
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQ_PICK_VARIANT_IMAGE || resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        if (pendingImageRowIndex < 0 || pendingImageRowIndex >= rows.size()) {
            return;
        }

        Uri uri = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {
        }

        rows.get(pendingImageRowIndex).imageUri = uri;
        renderRows();
    }

    private List<String> splitValues(String raw) {
        List<String> values = new ArrayList<>();
        if (TextUtils.isEmpty(raw)) return values;

        String[] parts = raw.split(",");
        for (String part : parts) {
            String value = part.trim();
            if (!value.isEmpty() && !values.contains(value)) {
                values.add(value);
            }
        }
        return values;
    }

    private String formatAttributes(Map<String, String> attributes) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            parts.add(entry.getKey() + ": " + entry.getValue());
        }
        return TextUtils.join(" / ", parts);
    }

    private String textOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private static class VariantGroup {
        String name;
        List<String> values = new ArrayList<>();
    }

    private static class VariantRow {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        String priceText;
        String stockText;
        Uri imageUri;
        String imageUrl;
    }
}
