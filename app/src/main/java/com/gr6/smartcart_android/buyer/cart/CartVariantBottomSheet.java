package com.gr6.smartcart_android.buyer.cart;

import android.app.Activity;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.cart.response.CartDetailResponse;
import com.gr6.smartcart_android.buyer.product.repository.ProductRepository;
import com.gr6.smartcart_android.buyer.product.response.ProductDetailResponse;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartVariantBottomSheet {

    private final Activity activity;
    private final CartDetailResponse.CartItem cartItem;
    private final OnVariantSelectedListener listener;

    private BottomSheetDialog dialog;
    private ProductDetailResponse productDetail;
    private ProductDetailResponse.VariantDTO selectedVariant;

    private final Map<String, String> selectedOptions = new LinkedHashMap<>();

    private ImageView imgProduct;
    private TextView txtPrice;
    private TextView txtStock;
    private TextView txtSelected;
    private TextView btnConfirm;
    private LinearLayout layoutOptions;

    public CartVariantBottomSheet(
            Activity activity,
            CartDetailResponse.CartItem cartItem,
            OnVariantSelectedListener listener
    ) {
        this.activity = activity;
        this.cartItem = cartItem;
        this.listener = listener;
    }

    public void show() {
        if (activity == null || activity.isFinishing()) return;

        if (cartItem == null || cartItem.getProductId() == null || cartItem.getProductId() <= 0) {
            sendMessage("Thiếu productId để đổi phân loại");
            return;
        }

        dialog = new BottomSheetDialog(activity);
        dialog.setContentView(createLoadingView());
        dialog.show();

        loadProductDetail();
    }

    private View createLoadingView() {
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(44), dp(24), dp(44));
        root.setBackgroundColor(color(R.color.surface));

        ProgressBar progressBar = new ProgressBar(activity);
        root.addView(progressBar, new LinearLayout.LayoutParams(dp(42), dp(42)));

        TextView text = createText("Đang tải phân loại...", 15, R.color.text_secondary, Typeface.NORMAL);
        text.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = dp(14);
        root.addView(text, textParams);

        return root;
    }

    private void loadProductDetail() {
        ProductRepository repository = new ProductRepository(activity);

        repository.getProductDetail(cartItem.getProductId(), new ProductRepository.ProductDetailCallback() {
            @Override
            public void onSuccess(ProductDetailResponse data) {
                productDetail = data;

                if (activity == null || activity.isFinishing()) return;

                activity.runOnUiThread(() -> {
                    initCurrentSelection();

                    if (dialog != null) {
                        dialog.setContentView(createContentView());
                    }

                    refreshAll();
                });
            }

            @Override
            public void onError(String message) {
                if (activity == null || activity.isFinishing()) return;

                activity.runOnUiThread(() -> {
                    if (dialog != null) dialog.dismiss();
                    sendMessage(message);
                });
            }
        });
    }

    private View createContentView() {
        ScrollView scrollView = new ScrollView(activity);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scrollView.setFillViewport(false);

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(14), dp(18), dp(22));
        root.setBackgroundColor(color(R.color.surface));

        scrollView.addView(root);

        View handle = new View(activity);
        handle.setBackgroundResource(R.drawable.bg_bottom_sheet_handle);

        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(46), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(handle, handleParams);

        TextView title = createText("Đổi phân loại", 20, R.color.text_primary, Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(18);
        root.addView(title, titleParams);

        TextView subtitle = createText(
                "Chọn phân loại giống như khi thêm vào giỏ hàng",
                13,
                R.color.text_secondary,
                Typeface.NORMAL
        );
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(4);
        root.addView(subtitle, subtitleParams);

        addHeader(root);

        layoutOptions = new LinearLayout(activity);
        layoutOptions.setOrientation(LinearLayout.VERTICAL);
        root.addView(layoutOptions);

        btnConfirm = new TextView(activity);
        btnConfirm.setGravity(Gravity.CENTER);
        btnConfirm.setTextColor(color(R.color.white));
        btnConfirm.setTextSize(17);
        btnConfirm.setTypeface(null, Typeface.BOLD);
        btnConfirm.setBackgroundResource(R.drawable.bg_cart_checkout);

        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
        );
        confirmParams.topMargin = dp(26);
        root.addView(btnConfirm, confirmParams);

        btnConfirm.setOnClickListener(v -> confirmChangeVariant());

        renderOptionGroups();

        return scrollView;
    }

    private void addHeader(LinearLayout root) {
        LinearLayout header = new LinearLayout(activity);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(12), dp(12), dp(12), dp(12));
        header.setBackgroundResource(R.drawable.bg_cart_variant_sheet_header);

        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        headerParams.topMargin = dp(16);
        root.addView(header, headerParams);

        imgProduct = new ImageView(activity);
        imgProduct.setBackgroundResource(R.drawable.bg_cart_image);
        imgProduct.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgProduct.setPadding(dp(5), dp(5), dp(5), dp(5));

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(92), dp(92));
        header.addView(imgProduct, imageParams);

        LinearLayout info = new LinearLayout(activity);
        info.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        infoParams.leftMargin = dp(14);
        header.addView(info, infoParams);

        txtPrice = createText("0đ", 21, R.color.price_red, Typeface.BOLD);
        info.addView(txtPrice);

        txtStock = createText("Kho: 0", 14, R.color.text_secondary, Typeface.NORMAL);
        LinearLayout.LayoutParams stockParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        stockParams.topMargin = dp(6);
        info.addView(txtStock, stockParams);

        txtSelected = createText("Chọn phân loại", 13, R.color.text_secondary, Typeface.NORMAL);
        txtSelected.setMaxLines(2);
        txtSelected.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams selectedParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        selectedParams.topMargin = dp(6);
        info.addView(txtSelected, selectedParams);
    }

    private void renderOptionGroups() {
        if (layoutOptions == null) return;

        layoutOptions.removeAllViews();

        List<OptionGroupUi> groups = getOptionGroupsForUi();

        if (groups.isEmpty()) {
            TextView empty = createText(
                    "Sản phẩm này chỉ có một phân loại.",
                    15,
                    R.color.text_secondary,
                    Typeface.NORMAL
            );

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = dp(20);
            layoutOptions.addView(empty, params);
            return;
        }

        for (OptionGroupUi group : groups) {
            addOptionGroup(group);
        }
    }

    private void addOptionGroup(OptionGroupUi group) {
        TextView title = createText(
                group.name,
                15,
                R.color.text_primary,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(22);
        layoutOptions.addView(title, titleParams);

        HorizontalScrollView scrollView = new HorizontalScrollView(activity);
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);

        for (String value : group.values) {
            TextView chip = createOptionChip(group.name, value);
            row.addView(chip);
        }

        scrollView.addView(row);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.topMargin = dp(10);
        layoutOptions.addView(scrollView, rowParams);
    }

    private TextView createOptionChip(String optionName, String value) {
        TextView chip = new TextView(activity);

        boolean selected = sameText(selectedOptions.get(optionName), value);

        chip.setText(value);
        chip.setSingleLine(true);
        chip.setEllipsize(TextUtils.TruncateAt.END);
        chip.setGravity(Gravity.CENTER);
        chip.setTextSize(14);
        chip.setMinWidth(dp(78));
        chip.setPadding(dp(14), 0, dp(14), 0);

        chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chip.setTextColor(color(selected ? R.color.brand_primary : R.color.text_primary));
        chip.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(42)
        );
        params.setMargins(0, 0, dp(10), 0);
        chip.setLayoutParams(params);

        chip.setOnClickListener(v -> {
            selectedOptions.put(optionName, value);

            ProductDetailResponse.VariantDTO matched = findMatchedVariant();

            if (matched != null) {
                selectedVariant = matched;
            }

            renderOptionGroups();
            refreshAll();
        });

        return chip;
    }

    private void initCurrentSelection() {
        selectedOptions.clear();
        selectedVariant = null;

        if (productDetail == null) return;

        List<ProductDetailResponse.VariantDTO> variants = productDetail.getVariants();

        if (variants == null || variants.isEmpty()) return;

        if (cartItem != null && cartItem.getVariantId() != null) {
            for (ProductDetailResponse.VariantDTO variant : variants) {
                if (variant == null || variant.getVariantId() == null) continue;

                if (variant.getVariantId().equals(cartItem.getVariantId())) {
                    selectedVariant = variant;
                    putVariantAttributesToSelectedOptions(variant);
                    break;
                }
            }
        }

        if (selectedVariant == null) {
            selectedVariant = variants.get(0);
            putVariantAttributesToSelectedOptions(selectedVariant);
        }

        for (OptionGroupUi group : getOptionGroupsForUi()) {
            if (selectedOptions.containsKey(group.name)) continue;

            if (group.values.size() == 1) {
                selectedOptions.put(group.name, group.values.get(0));
            }
        }
    }

    private void putVariantAttributesToSelectedOptions(ProductDetailResponse.VariantDTO variant) {
        if (variant == null || variant.getAttributes() == null || variant.getAttributes().isEmpty()) return;

        for (Map.Entry<String, String> entry : variant.getAttributes().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (isEmpty(key) || isEmpty(value)) continue;

            String groupName = findRealGroupName(key);
            selectedOptions.put(groupName, value.trim());
        }
    }

    private String findRealGroupName(String attributeKey) {
        if (isEmpty(attributeKey)) return "Phân loại";

        List<OptionGroupUi> groups = getOptionGroupsForUi();

        for (OptionGroupUi group : groups) {
            if (sameText(group.name, attributeKey)) {
                return group.name;
            }
        }

        return attributeKey.trim();
    }

    private List<OptionGroupUi> getOptionGroupsForUi() {
        List<OptionGroupUi> result = new ArrayList<>();

        if (productDetail == null) return result;

        List<ProductDetailResponse.OptionGroupDTO> groups = productDetail.getOptionGroups();

        if (groups != null) {
            for (ProductDetailResponse.OptionGroupDTO group : groups) {
                if (group == null) continue;

                String name = group.getName();

                if (isEmpty(name)) continue;

                List<String> cleanValues = cleanStringList(group.getValues());

                if (cleanValues.isEmpty()) continue;

                result.add(new OptionGroupUi(name.trim(), cleanValues));
            }
        }

        if (!result.isEmpty()) return result;

        Map<String, List<String>> derived = new LinkedHashMap<>();

        for (ProductDetailResponse.VariantDTO variant : productDetail.getVariants()) {
            if (variant == null || variant.getAttributes() == null) continue;

            for (Map.Entry<String, String> entry : variant.getAttributes().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (isEmpty(key) || isEmpty(value)) continue;

                String safeKey = key.trim();
                String safeValue = value.trim();

                if (!derived.containsKey(safeKey)) {
                    derived.put(safeKey, new ArrayList<>());
                }

                List<String> values = derived.get(safeKey);

                if (!containsSameText(values, safeValue)) {
                    values.add(safeValue);
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : derived.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                result.add(new OptionGroupUi(entry.getKey(), entry.getValue()));
            }
        }

        return result;
    }

    private ProductDetailResponse.VariantDTO findMatchedVariant() {
        if (productDetail == null) return null;

        List<ProductDetailResponse.VariantDTO> variants = productDetail.getVariants();

        if (variants == null || variants.isEmpty()) return null;

        List<OptionGroupUi> groups = getOptionGroupsForUi();

        if (groups.isEmpty()) {
            if (selectedVariant != null) return selectedVariant;
            return variants.get(0);
        }

        if (!hasSelectedAllOptionGroups()) return null;

        for (ProductDetailResponse.VariantDTO variant : variants) {
            if (variant == null) continue;

            Map<String, String> attrs = variant.getAttributes();
            if (attrs == null || attrs.isEmpty()) continue;

            boolean matched = true;

            for (OptionGroupUi group : groups) {
                String selectedValue = selectedOptions.get(group.name);
                String realValue = findAttributeValue(attrs, group.name);

                if (!sameText(realValue, selectedValue)) {
                    matched = false;
                    break;
                }
            }

            if (matched) return variant;
        }

        return null;
    }

    private boolean hasSelectedAllOptionGroups() {
        List<OptionGroupUi> groups = getOptionGroupsForUi();

        if (groups.isEmpty()) return true;

        for (OptionGroupUi group : groups) {
            String selected = selectedOptions.get(group.name);

            if (isEmpty(selected)) {
                return false;
            }
        }

        return true;
    }

    private String findMissingOptionName() {
        for (OptionGroupUi group : getOptionGroupsForUi()) {
            String selected = selectedOptions.get(group.name);

            if (isEmpty(selected)) {
                return group.name;
            }
        }

        return null;
    }

    private String findAttributeValue(Map<String, String> attrs, String key) {
        if (attrs == null || key == null) return null;

        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            if (sameText(entry.getKey(), key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private void refreshAll() {
        ProductDetailResponse.VariantDTO matched = findMatchedVariant();

        if (matched != null) {
            selectedVariant = matched;
        }

        ProductDetailResponse.VariantDTO displayVariant =
                matched != null ? matched : selectedVariant;

        BigDecimal displayPrice = BigDecimal.ZERO;
        int stock = 0;
        String imageUrl = null;

        if (displayVariant != null) {
            displayPrice = displayVariant.getPrice();
            stock = displayVariant.getStockQuantity();
            imageUrl = displayVariant.getImageUrl();
        } else if (productDetail != null) {
            displayPrice = productDetail.getBasePrice();
            stock = productDetail.getTotalStock();
        }

        if (isEmpty(imageUrl)) {
            if (productDetail != null && !productDetail.getImageUrls().isEmpty()) {
                imageUrl = productDetail.getImageUrls().get(0);
            } else if (cartItem != null) {
                imageUrl = cartItem.getImageUrl();
            }
        }

        if (imgProduct != null) {
            ImageLoader.load(activity, imageUrl, imgProduct);
        }

        if (txtPrice != null) {
            txtPrice.setText(formatMoney(displayPrice));
        }

        if (txtStock != null) {
            txtStock.setText("Kho: " + stock);
        }

        if (txtSelected != null) {
            if (!hasSelectedAllOptionGroups()) {
                String missing = findMissingOptionName();
                txtSelected.setText(isEmpty(missing)
                        ? "Vui lòng chọn đầy đủ phân loại"
                        : "Vui lòng chọn " + missing
                );
            } else if (matched == null) {
                txtSelected.setText("Không tìm thấy phân loại phù hợp");
            } else {
                txtSelected.setText("Đã chọn: " + matched.getAttributeText());
            }
        }

        updateConfirmButton(matched);
    }

    private void updateConfirmButton(ProductDetailResponse.VariantDTO matched) {
        if (btnConfirm == null) return;

        if (!hasSelectedAllOptionGroups()) {
            btnConfirm.setText("Vui lòng chọn phân loại");
            btnConfirm.setEnabled(false);
            btnConfirm.setAlpha(0.45f);
            return;
        }

        if (matched == null || matched.getVariantId() == null) {
            btnConfirm.setText("Không có phân loại phù hợp");
            btnConfirm.setEnabled(false);
            btnConfirm.setAlpha(0.45f);
            return;
        }

        if (matched.getStockQuantity() <= 0) {
            btnConfirm.setText("Hết hàng");
            btnConfirm.setEnabled(false);
            btnConfirm.setAlpha(0.45f);
            return;
        }

        if (cartItem != null
                && cartItem.getVariantId() != null
                && cartItem.getVariantId().equals(matched.getVariantId())) {
            btnConfirm.setText("Đang chọn phân loại này");
            btnConfirm.setEnabled(false);
            btnConfirm.setAlpha(0.45f);
            return;
        }

        btnConfirm.setText("Xác nhận thay đổi");
        btnConfirm.setEnabled(true);
        btnConfirm.setAlpha(1f);
    }

    private void confirmChangeVariant() {
        ProductDetailResponse.VariantDTO chosen = findMatchedVariant();

        if (!hasSelectedAllOptionGroups()) {
            String missing = findMissingOptionName();

            sendMessage(isEmpty(missing)
                    ? "Vui lòng chọn đầy đủ phân loại"
                    : "Vui lòng chọn " + missing
            );
            return;
        }

        if (chosen == null || chosen.getVariantId() == null) {
            sendMessage("Không tìm thấy phân loại phù hợp. Bạn thử chọn lại nhé");
            return;
        }

        if (chosen.getStockQuantity() <= 0) {
            sendMessage("Phân loại này đã hết hàng");
            return;
        }

        if (cartItem != null
                && cartItem.getVariantId() != null
                && cartItem.getVariantId().equals(chosen.getVariantId())) {
            sendMessage("Bạn đang chọn phân loại này");
            return;
        }

        if (listener != null) {
            listener.onVariantSelected(cartItem, chosen);
        }

        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private List<String> cleanStringList(List<String> input) {
        List<String> result = new ArrayList<>();

        if (input == null) return result;

        for (String value : input) {
            if (isEmpty(value)) continue;

            String safe = value.trim();

            if (!containsSameText(result, safe)) {
                result.add(safe);
            }
        }

        return result;
    }

    private boolean containsSameText(List<String> values, String target) {
        if (values == null || target == null) return false;

        for (String value : values) {
            if (sameText(value, target)) return true;
        }

        return false;
    }

    private TextView createText(String text, int sp, int colorRes, int style) {
        TextView textView = new TextView(activity);
        textView.setText(text);
        textView.setTextSize(sp);
        textView.setTextColor(color(colorRes));
        textView.setTypeface(null, style);
        return textView;
    }

    private boolean sameText(String a, String b) {
        return normalizeText(a).equals(normalizeText(b));
    }

    private String normalizeText(String value) {
        if (value == null) return "";

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replace("Đ", "D").replace("đ", "d");

        return normalized
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "")
                .replace("/", "")
                .replace("\\", "");
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(value) + "đ";
    }

    private int color(int colorRes) {
        return ContextCompat.getColor(activity, colorRes);
    }

    private int dp(int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }

    private void sendMessage(String message) {
        if (listener != null) {
            listener.onMessage(message);
        }
    }

    private static class OptionGroupUi {
        private final String name;
        private final List<String> values;

        private OptionGroupUi(String name, List<String> values) {
            this.name = name;
            this.values = values;
        }
    }

    public interface OnVariantSelectedListener {
        void onVariantSelected(
                CartDetailResponse.CartItem oldItem,
                ProductDetailResponse.VariantDTO selectedVariant
        );

        void onMessage(String message);
    }
}