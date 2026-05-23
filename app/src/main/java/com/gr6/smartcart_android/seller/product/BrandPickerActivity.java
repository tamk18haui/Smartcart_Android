package com.gr6.smartcart_android.seller.product;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.seller.product.repository.SellerProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrandPickerActivity extends BaseActivity {

    private EditText edtSearch;
    private LinearLayout listContainer;
    private SellerProductRepository repository;

    private Long categoryId;
    private String categoryName;

    private final List<String> brands = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_brand_picker);
        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        repository = new SellerProductRepository(this);

        categoryId = getIntent().hasExtra("category_id")
                ? getIntent().getLongExtra("category_id", -1L)
                : null;

        if (categoryId != null && categoryId <= 0) {
            categoryId = null;
        }

        categoryName = getIntent().getStringExtra("category_name");

        edtSearch = findViewById(R.id.edtBrandSearch);
        listContainer = findViewById(R.id.brandListContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSearchBrand).setOnClickListener(v -> loadBrands());
        findViewById(R.id.btnUseCustomBrand).setOnClickListener(v -> {
            String customBrand = edtSearch.getText() == null
                    ? ""
                    : edtSearch.getText().toString().trim();

            if (customBrand.isEmpty()) {
                showToast("Vui lòng nhập tên thương hiệu");
                return;
            }

            returnBrand(customBrand);
        });

        loadBrands();
    }

    private void loadBrands() {
        String keyword = edtSearch.getText() == null
                ? ""
                : edtSearch.getText().toString().trim();

        showLoading();

        repository.getBrandSuggestions(keyword, categoryId).enqueue(new Callback<BaseResponse<List<String>>>() {
            @Override
            public void onResponse(
                    Call<BaseResponse<List<String>>> call,
                    Response<BaseResponse<List<String>>> response
            ) {
                hideLoading();

                BaseResponse<List<String>> body = response.body();
                brands.clear();

                if (response.isSuccessful()
                        && body != null
                        && body.isSuccess()
                        && body.getData() != null) {
                    for (String brand : body.getData()) {
                        addBrandIfValid(brand);
                    }
                }

                addFallbackBrands(keyword);
                renderList();
            }

            @Override
            public void onFailure(Call<BaseResponse<List<String>>> call, Throwable t) {
                hideLoading();
                brands.clear();

                String keyword = edtSearch.getText() == null
                        ? ""
                        : edtSearch.getText().toString().trim();

                addFallbackBrands(keyword);
                renderList();

                showToast("Không tải được thương hiệu từ server, đang dùng gợi ý mặc định");
            }
        });
    }

    private void renderList() {
        listContainer.removeAllViews();

        TextView noBrand = makeRow("No Brand");
        noBrand.setOnClickListener(v -> returnBrand(null));
        listContainer.addView(noBrand);

        if (brands.isEmpty()) {
            String message = categoryName == null || categoryName.trim().isEmpty()
                    ? "Chưa có thương hiệu gợi ý, bạn có thể nhập thương hiệu riêng ở ô tìm kiếm"
                    : "Chưa có thương hiệu gợi ý cho danh mục " + categoryName + ", bạn có thể nhập thương hiệu riêng ở ô tìm kiếm";

            TextView empty = makeRow(message);
            empty.setTextSize(14);
            empty.setTextColor(ThemeColor.textSecondary(this));
            listContainer.addView(empty);
            return;
        }

        for (String brand : brands) {
            if (brand == null || brand.trim().isEmpty()) {
                continue;
            }

            String cleanBrand = brand.trim();
            TextView row = makeRow(cleanBrand);
            row.setOnClickListener(v -> returnBrand(cleanBrand));
            listContainer.addView(row);
        }
    }

    private void addFallbackBrands(String keyword) {
        List<String> fallbackBrands = getDefaultBrandsByCategory();

        String search = keyword == null
                ? ""
                : keyword.trim().toLowerCase(Locale.US);

        for (String brand : fallbackBrands) {
            if (TextUtils.isEmpty(search)
                    || brand.toLowerCase(Locale.US).contains(search)) {
                addBrandIfValid(brand);
            }
        }
    }

    private List<String> getDefaultBrandsByCategory() {
        List<String> result = new ArrayList<>();

        String name = categoryName == null
                ? ""
                : categoryName.trim().toLowerCase(Locale.US);

        if (name.contains("điện thoại")
                || name.contains("dien thoai")
                || name.contains("phone")
                || name.contains("mobile")) {
            addAll(result,
                    "Apple",
                    "Samsung",
                    "Xiaomi",
                    "OPPO",
                    "vivo",
                    "realme",
                    "Nokia",
                    "Huawei",
                    "Honor",
                    "Tecno",
                    "Infinix"
            );
            return result;
        }

        if (name.contains("laptop")
                || name.contains("máy tính")
                || name.contains("may tinh")
                || name.contains("computer")
                || name.contains("pc")) {
            addAll(result,
                    "Apple",
                    "Dell",
                    "HP",
                    "Asus",
                    "Acer",
                    "Lenovo",
                    "MSI",
                    "Gigabyte",
                    "Microsoft",
                    "LG"
            );
            return result;
        }

        if (name.contains("điện tử")
                || name.contains("dien tu")
                || name.contains("phụ kiện")
                || name.contains("phu kien")
                || name.contains("electronics")
                || name.contains("accessory")) {
            addAll(result,
                    "Samsung",
                    "Sony",
                    "LG",
                    "Panasonic",
                    "Anker",
                    "Baseus",
                    "Ugreen",
                    "JBL",
                    "Logitech",
                    "Xiaomi"
            );
            return result;
        }

        if (name.contains("thời trang")
                || name.contains("thoi trang")
                || name.contains("quần áo")
                || name.contains("quan ao")
                || name.contains("fashion")
                || name.contains("clothes")
                || name.contains("áo")
                || name.contains("giày")
                || name.contains("giay")) {
            addAll(result,
                    "Nike",
                    "Adidas",
                    "Puma",
                    "Converse",
                    "Vans",
                    "Uniqlo",
                    "Zara",
                    "H&M",
                    "MLB",
                    "Local Brand"
            );
            return result;
        }

        if (name.contains("mỹ phẩm")
                || name.contains("my pham")
                || name.contains("làm đẹp")
                || name.contains("lam dep")
                || name.contains("beauty")
                || name.contains("cosmetic")) {
            addAll(result,
                    "L'Oréal",
                    "Maybelline",
                    "Innisfree",
                    "The Ordinary",
                    "La Roche-Posay",
                    "CeraVe",
                    "Simple",
                    "Cocoon",
                    "3CE",
                    "Romand"
            );
            return result;
        }

        if (name.contains("gia dụng")
                || name.contains("gia dung")
                || name.contains("nhà cửa")
                || name.contains("nha cua")
                || name.contains("home")
                || name.contains("kitchen")) {
            addAll(result,
                    "Lock&Lock",
                    "Sunhouse",
                    "Kangaroo",
                    "Philips",
                    "Bluestone",
                    "Panasonic",
                    "Tefal",
                    "Sharp",
                    "Electrolux",
                    "Xiaomi"
            );
            return result;
        }

        if (name.contains("sách")
                || name.contains("sach")
                || name.contains("book")
                || name.contains("văn phòng")
                || name.contains("van phong")
                || name.contains("stationery")) {
            addAll(result,
                    "Kim Đồng",
                    "Nhã Nam",
                    "Alpha Books",
                    "First News",
                    "Fahasa",
                    "Thiên Long",
                    "Hồng Hà",
                    "Deli",
                    "Campus",
                    "Plus"
            );
            return result;
        }

        if (name.contains("mẹ")
                || name.contains("bé")
                || name.contains("me")
                || name.contains("be")
                || name.contains("baby")
                || name.contains("mom")) {
            addAll(result,
                    "Bobby",
                    "Huggies",
                    "Pampers",
                    "Moony",
                    "Merries",
                    "Pigeon",
                    "Chicco",
                    "Comotomo",
                    "Meiji",
                    "Abbott"
            );
            return result;
        }

        if (name.contains("thực phẩm")
                || name.contains("thuc pham")
                || name.contains("đồ ăn")
                || name.contains("do an")
                || name.contains("food")
                || name.contains("drink")) {
            addAll(result,
                    "Vinamilk",
                    "TH True Milk",
                    "Nestlé",
                    "Milo",
                    "Oreo",
                    "Kinh Đô",
                    "Acecook",
                    "Omachi",
                    "Pepsi",
                    "Coca-Cola"
            );
            return result;
        }

        addAll(result,
                "Apple",
                "Samsung",
                "Xiaomi",
                "Sony",
                "LG",
                "Nike",
                "Adidas",
                "Panasonic",
                "Philips",
                "Local Brand"
        );

        return result;
    }

    private void addAll(List<String> target, String... values) {
        if (target == null || values == null) {
            return;
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty() && !target.contains(value.trim())) {
                target.add(value.trim());
            }
        }
    }

    private void addBrandIfValid(String brand) {
        if (brand == null) {
            return;
        }

        String cleanBrand = brand.trim();

        if (cleanBrand.isEmpty()) {
            return;
        }

        for (String existing : brands) {
            if (existing != null && existing.equalsIgnoreCase(cleanBrand)) {
                return;
            }
        }

        brands.add(cleanBrand);
    }

    private TextView makeRow(String value) {
        TextView row = new TextView(this);
        row.setText(value);
        row.setTextColor(ThemeColor.textPrimary(this));
        row.setTextSize(16);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), 0, dp(18), 0);
        row.setBackgroundResource(R.drawable.bg_seller_row);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
        ));
        return row;
    }

    private void returnBrand(String brand) {
        Intent result = new Intent();
        result.putExtra("brand", brand);
        setResult(RESULT_OK, result);
        finish();
    }
}