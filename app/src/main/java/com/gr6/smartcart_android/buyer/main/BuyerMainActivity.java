package com.gr6.smartcart_android.buyer.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.main.response.HomeCategoryResponse;
import com.gr6.smartcart_android.buyer.main.response.HomeProductResponse;
import com.gr6.smartcart_android.chat.ChatListActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.gr6.smartcart_android.navigation.BuyerBottomNavHelper;

public class BuyerMainActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rcvCategories;
    private RecyclerView rcvProducts;

    private EditText edtSearch;
    private ImageView imgCart;
    private ImageView imgMessage;
    private TextView txtUserName;
    private TextView txtEmpty;
    private TextView txtProductCount;
    private NestedScrollView nestedHome;
    private BuyerHomeViewModel viewModel;
    private CategoryHomeAdapter categoryAdapter;
    private ProductHomeAdapter productAdapter;
    private final List<HomeProductResponse> originalProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_main);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);
        Window window = getWindow();

// Tô màu thanh pin/sóng giống màu header
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.brand_primary));

// Nếu nền status bar là màu đậm thì icon pin/sóng phải màu trắng
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = window.getDecorView().getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            window.getDecorView().setSystemUiVisibility(flags);
        }

        viewModel = new ViewModelProvider(this).get(BuyerHomeViewModel.class);

        initViews();
        setupRecyclerViews();
        initEvents();
        BuyerBottomNavHelper.setup(this, BuyerBottomNavHelper.TAB_HOME);
        observeHome();

        viewModel.loadHome();
    }
    private void initViews() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rcvCategories = findViewById(R.id.rcvCategories);
        rcvProducts = findViewById(R.id.rcvProducts);

        edtSearch = findViewById(R.id.edtSearch);
        imgCart = findViewById(R.id.imgCart);
        imgMessage = findViewById(R.id.imgMessage);
        txtUserName = findViewById(R.id.txtUserName);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtProductCount = findViewById(R.id.txtProductCount);
        nestedHome = findViewById(R.id.nestedHome);

        String fullName = UserSession.getInstance(this).getFullName();
        if (fullName == null || fullName.trim().isEmpty()) {
            txtUserName.setText("Xin chào bạn 👋");
        } else {
            txtUserName.setText("Xin chào, " + fullName);
        }
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryHomeAdapter();
        productAdapter = new ProductHomeAdapter();

        rcvCategories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        rcvCategories.setAdapter(categoryAdapter);
        rcvCategories.setNestedScrollingEnabled(false);

        rcvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rcvProducts.setAdapter(productAdapter);
        rcvProducts.setNestedScrollingEnabled(false);
    }

    private void initEvents() {
        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(() -> viewModel.refreshHome());

        imgCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.gr6.smartcart_android.buyer.cart.CartActivity.class);
            startActivity(intent);
        });

        imgMessage.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatListActivity.class);
            startActivity(intent);
        });

        categoryAdapter.setOnCategoryClickListener(category -> {
            if (category == null || category.getCategoryId() == null) {
                viewModel.clearCategoryFilter();
                return;
            }

            viewModel.filterByCategory(category.getCategoryId());
        });

        productAdapter.setOnProductClickListener(product -> {
            if (product.getProductId() == null) {
                showToast("Không tìm thấy sản phẩm");
                return;
            }

            Intent intent = new Intent(this, com.gr6.smartcart_android.buyer.product.ProductDetailActivity.class);
            intent.putExtra(
                    com.gr6.smartcart_android.buyer.product.ProductDetailActivity.EXTRA_PRODUCT_ID,
                    product.getProductId()
            );
            startActivity(intent);
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                viewModel.searchProducts(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setupLoadMoreScroll();
    }
    private void setupLoadMoreScroll() {
        int nestedHomeId = getResources().getIdentifier(
                "nestedHome",
                "id",
                getPackageName()
        );

        if (nestedHomeId == 0) {
            return;
        }

        View nestedHome = findViewById(nestedHomeId);

        if (nestedHome == null) {
            return;
        }

        nestedHome.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!v.canScrollVertically(1)) {
                viewModel.loadMoreProducts();
            }
        });
    }
    private void observeHome() {
        viewModel.getHomeState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                swipeRefresh.setRefreshing(true);
                txtEmpty.setVisibility(View.GONE);
                return;
            }

            swipeRefresh.setRefreshing(false);

            if (state.isSuccess()) {
                bindHomeData(state.getCategories(), state.getProducts());
            } else {
                txtEmpty.setVisibility(View.VISIBLE);
                txtEmpty.setText(state.getMessage());
                showLongToast(state.getMessage());
            }
        });
    }

    private void bindHomeData(
            List<HomeCategoryResponse> categories,
            List<HomeProductResponse> products
    ) {
        categoryAdapter.setData(categories);

        originalProducts.clear();

        if (products != null) {
            originalProducts.addAll(products);
        }

        productAdapter.setData(originalProducts);
        updateProductCount(originalProducts.size());

        if (originalProducts.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            txtEmpty.setText("Chưa có sản phẩm nào");
        } else {
            txtEmpty.setVisibility(View.GONE);
        }
    }

    private void filterByKeyword(String keyword) {
        String query = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        if (query.isEmpty()) {
            productAdapter.setData(originalProducts);
            updateProductCount(originalProducts.size());
            txtEmpty.setVisibility(originalProducts.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }

        List<HomeProductResponse> filtered = new ArrayList<>();

        for (HomeProductResponse product : originalProducts) {
            String name = product.getProductName().toLowerCase(Locale.ROOT);
            String shopName = product.getShopName().toLowerCase(Locale.ROOT);

            if (name.contains(query) || shopName.contains(query)) {
                filtered.add(product);
            }
        }

        productAdapter.setData(filtered);
        updateProductCount(filtered.size());

        txtEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        txtEmpty.setText("Không tìm thấy sản phẩm phù hợp");
    }

    private void filterByCategory(HomeCategoryResponse category) {
        if (category == null || category.getCategoryId() == null) {
            return;
        }

        List<HomeProductResponse> filtered = new ArrayList<>();

        for (HomeProductResponse product : originalProducts) {
            if (category.getCategoryId().equals(product.getCategoryId())) {
                filtered.add(product);
            }
        }

        productAdapter.setData(filtered);
        updateProductCount(filtered.size());

        if (filtered.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            txtEmpty.setText("Danh mục này chưa có sản phẩm");
        } else {
            txtEmpty.setVisibility(View.GONE);
        }

        showToast(category.getCategoryName());
    }

    private void updateProductCount(int count) {
        txtProductCount.setText(count + " sản phẩm");
    }
}