package com.gr6.smartcart_android.seller.product;

import android.content.Intent;
import android.os.Bundle;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrandPickerActivity extends BaseActivity {

    private EditText edtSearch;
    private LinearLayout listContainer;
    private SellerProductRepository repository;
    private final List<String> brands = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_brand_picker);
        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        repository = new SellerProductRepository(this);
        edtSearch = findViewById(R.id.edtBrandSearch);
        listContainer = findViewById(R.id.brandListContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSearchBrand).setOnClickListener(v -> loadBrands());
        findViewById(R.id.btnUseCustomBrand).setOnClickListener(v -> returnBrand(edtSearch.getText().toString().trim()));

        loadBrands();
    }

    private void loadBrands() {
        String keyword = edtSearch.getText() == null ? "" : edtSearch.getText().toString().trim();
        showLoading();

        repository.getBrandSuggestions(keyword).enqueue(new Callback<BaseResponse<List<String>>>() {
            @Override
            public void onResponse(
                    Call<BaseResponse<List<String>>> call,
                    Response<BaseResponse<List<String>>> response
            ) {
                hideLoading();
                BaseResponse<List<String>> body = response.body();
                brands.clear();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    brands.addAll(body.getData());
                }
                renderList();
            }

            @Override
            public void onFailure(Call<BaseResponse<List<String>>> call, Throwable t) {
                hideLoading();
                showToast("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void renderList() {
        listContainer.removeAllViews();

        TextView noBrand = makeRow("No Brand");
        noBrand.setOnClickListener(v -> returnBrand(null));
        listContainer.addView(noBrand);

        for (String brand : brands) {
            if (brand == null || brand.trim().isEmpty()) continue;

            TextView row = makeRow(brand.trim());
            row.setOnClickListener(v -> returnBrand(brand.trim()));
            listContainer.addView(row);
        }
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


