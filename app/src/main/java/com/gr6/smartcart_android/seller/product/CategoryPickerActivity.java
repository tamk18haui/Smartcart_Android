package com.gr6.smartcart_android.seller.product;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.seller.model.CategoryResponse;
import com.gr6.smartcart_android.seller.repository.SellerProductRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryPickerActivity extends BaseActivity {

    private LinearLayout listContainer;
    private EditText edtSearch;
    private SellerProductRepository repository;
    private final List<CategoryResponse> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_category_picker);
        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        repository = new SellerProductRepository(this);
        listContainer = findViewById(R.id.categoryListContainer);
        edtSearch = findViewById(R.id.edtCategorySearch);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderList(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadCategories();
    }

    private void loadCategories() {
        showLoading();
        repository.getCategories().enqueue(new Callback<BaseResponse<List<CategoryResponse>>>() {
            @Override
            public void onResponse(
                    Call<BaseResponse<List<CategoryResponse>>> call,
                    Response<BaseResponse<List<CategoryResponse>>> response
            ) {
                hideLoading();
                BaseResponse<List<CategoryResponse>> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    categories.clear();
                    for (CategoryResponse category : body.getData()) {
                        if (category != null && category.isActive()) {
                            categories.add(category);
                        }
                    }
                    renderList("");
                    return;
                }

                showToast(body == null ? "Không tải được danh mục" : body.getSafeMessage());
            }

            @Override
            public void onFailure(Call<BaseResponse<List<CategoryResponse>>> call, Throwable t) {
                hideLoading();
                showToast("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void renderList(String keyword) {
        listContainer.removeAllViews();
        String lower = keyword == null ? "" : keyword.trim().toLowerCase();

        for (CategoryResponse category : categories) {
            String name = category.getCategoryName() == null ? "" : category.getCategoryName();
            if (!lower.isEmpty() && !name.toLowerCase().contains(lower)) {
                continue;
            }

            TextView row = new TextView(this);
            row.setText(name);
            row.setTextColor(ThemeColor.textPrimary(this));
            row.setTextSize(16);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(dp(18), 0, dp(18), 0);
            row.setBackgroundResource(R.drawable.bg_seller_row);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(56)
            );
            params.setMargins(0, 0, 0, dp(1));
            row.setLayoutParams(params);

            row.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra("category_id", category.getCategoryId());
                result.putExtra("category_name", category.getCategoryName());
                setResult(RESULT_OK, result);
                finish();
            });

            listContainer.addView(row);
        }
    }
}
