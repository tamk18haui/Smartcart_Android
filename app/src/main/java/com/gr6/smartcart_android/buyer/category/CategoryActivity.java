package com.gr6.smartcart_android.buyer.category;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.main.BuyerHomeViewModel;
import com.gr6.smartcart_android.buyer.main.response.HomeCategoryResponse;
import com.gr6.smartcart_android.buyer.search.SearchProductActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.navigation.BuyerBottomNavHelper;

import java.util.List;

public class CategoryActivity extends BaseActivity {

    private ImageView imgMenu;
    private ImageView imgSearch;
    private SwipeRefreshLayout swipeCategory;
    private RecyclerView rcvCategories;
    private TextView txtEmpty;

    private BuyerHomeViewModel viewModel;
    private CategoryGridAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        ThemeColor.applyWhiteNavigationBar(this);

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.surface_soft));

        viewModel = new ViewModelProvider(this).get(BuyerHomeViewModel.class);

        initViews();
        setupRecyclerView();
        initEvents();
        observeData();

        BuyerBottomNavHelper.setup(this, BuyerBottomNavHelper.TAB_CATEGORY);

        viewModel.loadHome();
    }

    private void initViews() {
        imgMenu = findViewById(R.id.imgMenu);
        imgSearch = findViewById(R.id.imgSearch);
        swipeCategory = findViewById(R.id.swipeCategory);
        rcvCategories = findViewById(R.id.rcvCategories);
        txtEmpty = findViewById(R.id.txtEmpty);
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryGridAdapter();

        rcvCategories.setLayoutManager(new GridLayoutManager(this, 2));
        rcvCategories.setAdapter(categoryAdapter);
        rcvCategories.setHasFixedSize(true);

        categoryAdapter.setOnCategoryClickListener(this::openCategoryProducts);
    }

    private void initEvents() {
        swipeCategory.setColorSchemeResources(R.color.brand_primary);

        swipeCategory.setOnRefreshListener(() -> {
            if (viewModel != null) {
                viewModel.loadHome();
            }
        });

        imgMenu.setOnClickListener(v -> showToast("Danh mục SmartCart"));

        imgSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchProductActivity.class);
            startActivity(intent);
        });
    }

    private void observeData() {
        viewModel.getHomeState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                swipeCategory.setRefreshing(true);
                txtEmpty.setVisibility(View.GONE);
                return;
            }

            swipeCategory.setRefreshing(false);

            if (state.isSuccess()) {
                bindCategories(state.getCategories());
            } else {
                showEmpty(state.getMessage());
            }
        });
    }

    private void bindCategories(List<HomeCategoryResponse> categories) {
        categoryAdapter.setData(categories);

        if (categories == null || categories.isEmpty()) {
            showEmpty("Chưa có danh mục nào");
        } else {
            txtEmpty.setVisibility(View.GONE);
            rcvCategories.setVisibility(View.VISIBLE);
        }
    }

    private void showEmpty(String message) {
        rcvCategories.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.VISIBLE);

        if (message == null || message.trim().isEmpty()) {
            message = "Không tải được danh mục";
        }

        txtEmpty.setText(message);
    }

    private void openCategoryProducts(HomeCategoryResponse category) {
        if (category == null) return;

        Long categoryId = category.getCategoryId();

        if (categoryId == null || categoryId <= 0) {
            showToast("Danh mục không hợp lệ");
            return;
        }

        Intent intent = new Intent(this, CategoryProductListActivity.class);
        intent.putExtra(CategoryProductListActivity.EXTRA_CATEGORY_ID, categoryId);
        intent.putExtra(CategoryProductListActivity.EXTRA_CATEGORY_NAME, category.getCategoryName());
        startActivity(intent);
    }
}