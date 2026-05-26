package com.gr6.smartcart_android.buyer.search;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.main.repository.BuyerHomeRepository;
import com.gr6.smartcart_android.buyer.main.request.SearchProductRequest;
import com.gr6.smartcart_android.buyer.main.response.HomeCategoryResponse;
import com.gr6.smartcart_android.buyer.main.response.HomeProductResponse;
import com.gr6.smartcart_android.buyer.main.response.ProductPageResponse;
import com.gr6.smartcart_android.buyer.product.ProductDetailActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import android.widget.ImageView;

public class SearchProductActivity extends BaseActivity {
    public static final String EXTRA_KEYWORD = "keyword";
    public static final String EXTRA_CATEGORY_ID = "category_id";

    private static final int PAGE_SIZE = 20;
    private static final long SEARCH_DELAY_MS = 450L;

    private EditText edtSearch;

    private ImageView btnImageSearch;
    private ImageView btnBack;
    private TextView btnClear;
    private ImageView btnFilter;

    private TextView tabRelevance;
    private TextView tabLatest;
    private TextView tabTopSales;
    private TextView tabPrice;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rcvSearchProducts;
    private View layoutEmpty;
    private TextView txtEmpty;

    private SearchProductAdapter adapter;
    private BuyerHomeRepository repository;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private final List<HomeCategoryResponse> categories = new ArrayList<>();

    private String keyword = "";
    private String sortBy = "relevance";

    private Long selectedCategoryId = null;
    private BigDecimal selectedMinPrice = null;
    private BigDecimal selectedMaxPrice = null;

    private int currentPage = 0;
    private boolean lastPage = false;
    private boolean loading = false;
    private int requestVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);

        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        repository = new BuyerHomeRepository(this);

        initViews();
        setupRecyclerView();
        initEvents();
        updateTabs();
        clearResultOnly();
        showInitialEmpty();
        loadCategoriesForFilter();

        edtSearch.requestFocus();
        edtSearch.postDelayed(this::showKeyboard, 250);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }

    private void initViews() {
        edtSearch = findViewById(R.id.edtSearch);
        btnBack = findViewById(R.id.btnBack);
        btnClear = findViewById(R.id.btnClear);
        btnFilter = findViewById(R.id.btnFilter);
        btnImageSearch = findViewById(R.id.btnImageSearch);

        tabRelevance = findViewById(R.id.tabRelevance);
        tabLatest = findViewById(R.id.tabLatest);
        tabTopSales = findViewById(R.id.tabTopSales);
        tabPrice = findViewById(R.id.tabPrice);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        rcvSearchProducts = findViewById(R.id.rcvSearchProducts);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        txtEmpty = findViewById(R.id.txtEmpty);
    }

    private void setupRecyclerView() {
        adapter = new SearchProductAdapter();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);

        rcvSearchProducts.setLayoutManager(layoutManager);
        rcvSearchProducts.setAdapter(adapter);

        adapter.setOnProductClickListener(product -> {
            if (product == null || product.getProductId() == null) {
                showToast("Không tìm thấy sản phẩm");
                return;
            }

            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getProductId());
            startActivity(intent);
        });

        rcvSearchProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(
                    RecyclerView recyclerView,
                    int dx,
                    int dy
            ) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy <= 0) return;
                if (loading || lastPage) return;
                if (keyword.trim().isEmpty()) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 4) {
                    loadProducts(false);
                }
            }
        });
    }

    private void initEvents() {
        btnBack.setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> {
            edtSearch.setText("");
            keyword = "";
            clearResultOnly();
            showInitialEmpty();
        });
        if (btnImageSearch != null) {
            btnImageSearch.setOnClickListener(v -> {
                Intent intent = new Intent(this, AiImageSearchActivity.class);
                startActivity(intent);
            });
        }

        btnFilter.setOnClickListener(v -> showFilterDialog());

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(() -> {
            if (keyword.trim().isEmpty()) {
                swipeRefresh.setRefreshing(false);
                showInitialEmpty();
                return;
            }

            loadProducts(true);
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                executeSearchNow();
                hideKeyboard();
                return true;
            }

            return false;
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
                scheduleSearch(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        tabRelevance.setOnClickListener(v -> {
            sortBy = "relevance";
            updateTabs();
            executeSearchNow();
        });

        tabLatest.setOnClickListener(v -> {
            sortBy = "newest";
            updateTabs();
            executeSearchNow();
        });

        tabTopSales.setOnClickListener(v -> {
            sortBy = "sold_desc";
            updateTabs();
            executeSearchNow();
        });

        tabPrice.setOnClickListener(v -> {
            if ("price_asc".equals(sortBy)) {
                sortBy = "price_desc";
            } else {
                sortBy = "price_asc";
            }

            updateTabs();
            executeSearchNow();
        });
    }

    private void loadCategoriesForFilter() {
        repository.getCategories(new BuyerHomeRepository.HomeCallback<List<HomeCategoryResponse>>() {
            @Override
            public void onSuccess(List<HomeCategoryResponse> data) {
                categories.clear();

                if (data != null) {
                    categories.addAll(data);
                }
            }

            @Override
            public void onError(String message) {
                categories.clear();
            }
        });
    }

    private void scheduleSearch(String value) {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        searchRunnable = () -> {
            keyword = value == null ? "" : value.trim();

            if (keyword.isEmpty()) {
                clearResultOnly();
                showInitialEmpty();
                return;
            }

            loadProducts(true);
        };

        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
    }

    private void executeSearchNow() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        keyword = edtSearch.getText() == null
                ? ""
                : edtSearch.getText().toString().trim();

        if (keyword.isEmpty()) {
            clearResultOnly();
            showInitialEmpty();
            return;
        }

        loadProducts(true);
    }

    private void loadProducts(boolean reset) {
        keyword = edtSearch.getText() == null
                ? ""
                : edtSearch.getText().toString().trim();

        if (keyword.isEmpty()) {
            clearResultOnly();
            showInitialEmpty();
            return;
        }

        if (!reset && (loading || lastPage)) {
            return;
        }

        loading = true;
        int version = ++requestVersion;

        if (reset) {
            currentPage = 0;
            lastPage = false;
            adapter.clear();
        }

        hideEmpty();
        swipeRefresh.setRefreshing(true);

        int pageToLoad = reset ? 0 : currentPage + 1;

        SearchProductRequest request = new SearchProductRequest(
                keyword,
                selectedCategoryId,
                selectedMinPrice,
                selectedMaxPrice,
                sortBy
        );

        repository.searchProducts(
                request,
                pageToLoad,
                PAGE_SIZE,
                new BuyerHomeRepository.HomeCallback<ProductPageResponse>() {
                    @Override
                    public void onSuccess(ProductPageResponse data) {
                        if (version != requestVersion) return;

                        loading = false;
                        swipeRefresh.setRefreshing(false);

                        if (data == null) {
                            updateEmptyAfterSearch();
                            return;
                        }

                        List<HomeProductResponse> products = data.getProducts();

                        if (products == null) {
                            products = new ArrayList<>();
                        }

                        if (reset) {
                            adapter.setData(products);
                        } else {
                            adapter.appendData(products);
                        }

                        currentPage = data.getPageIndexZeroBased();
                        lastPage = data.isLast();

                        updateEmptyAfterSearch();
                    }

                    @Override
                    public void onError(String message) {
                        if (version != requestVersion) return;

                        loading = false;
                        swipeRefresh.setRefreshing(false);
                        updateEmptyAfterSearch();

                        showLongToast(message == null
                                ? "Không tải được sản phẩm"
                                : message
                        );
                    }
                }
        );
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_search_filter, null, false);

        RecyclerView rcvFilterCategories = dialogView.findViewById(R.id.rcvFilterCategories);
        EditText edtMinPrice = dialogView.findViewById(R.id.edtMinPrice);
        EditText edtMaxPrice = dialogView.findViewById(R.id.edtMaxPrice);
        TextView btnCloseFilter = dialogView.findViewById(R.id.btnCloseFilter);
        TextView btnResetFilter = dialogView.findViewById(R.id.btnResetFilter);
        TextView btnApplyFilter = dialogView.findViewById(R.id.btnApplyFilter);

        final Long[] tempSelectedCategoryId = new Long[]{selectedCategoryId};

        SearchFilterCategoryAdapter categoryAdapter = new SearchFilterCategoryAdapter();
        categoryAdapter.setData(categories, selectedCategoryId);
        categoryAdapter.setOnCategorySelectedListener(categoryId ->
                tempSelectedCategoryId[0] = categoryId
        );

        rcvFilterCategories.setLayoutManager(new GridLayoutManager(this, 2));
        rcvFilterCategories.setAdapter(categoryAdapter);
        rcvFilterCategories.setNestedScrollingEnabled(false);

        bindCurrentPriceFilter(edtMinPrice, edtMaxPrice);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCloseFilter.setOnClickListener(v -> dialog.dismiss());

        btnResetFilter.setOnClickListener(v -> {
            selectedCategoryId = null;
            selectedMinPrice = null;
            selectedMaxPrice = null;

            updateFilterButtonState();
            dialog.dismiss();
            executeSearchNow();
        });

        btnApplyFilter.setOnClickListener(v -> {
            selectedCategoryId = tempSelectedCategoryId[0];

            try {
                selectedMinPrice = parsePrice(edtMinPrice.getText().toString());
                selectedMaxPrice = parsePrice(edtMaxPrice.getText().toString());
            } catch (Exception e) {
                showToast("Khoảng giá không hợp lệ");
                return;
            }

            if (selectedMinPrice != null
                    && selectedMaxPrice != null
                    && selectedMinPrice.compareTo(selectedMaxPrice) > 0) {
                BigDecimal temp = selectedMinPrice;
                selectedMinPrice = selectedMaxPrice;
                selectedMaxPrice = temp;
            }

            updateFilterButtonState();
            dialog.dismiss();
            executeSearchNow();
        });

        dialog.setOnShowListener(d -> {
            Window window = dialog.getWindow();

            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
                window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });

        dialog.show();
    }

    private void bindCurrentPriceFilter(
            EditText edtMinPrice,
            EditText edtMaxPrice
    ) {
        if (selectedMinPrice != null) {
            edtMinPrice.setText(selectedMinPrice.toPlainString());
        } else {
            edtMinPrice.setText("");
        }

        if (selectedMaxPrice != null) {
            edtMaxPrice.setText(selectedMaxPrice.toPlainString());
        } else {
            edtMaxPrice.setText("");
        }
    }

    private BigDecimal parsePrice(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String clean = raw.trim()
                .replace(".", "")
                .replace(",", "")
                .replace("đ", "")
                .replace("₫", "")
                .trim();

        if (clean.isEmpty()) {
            return null;
        }

        BigDecimal value = new BigDecimal(clean);

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Negative price");
        }

        return value;
    }

    private void clearResultOnly() {
        loading = false;
        lastPage = false;
        currentPage = 0;
        requestVersion++;
        swipeRefresh.setRefreshing(false);
        adapter.clear();
    }

    private void showInitialEmpty() {
        showEmpty("Nhập từ khóa để tìm kiếm sản phẩm");
    }

    private void updateEmptyAfterSearch() {
        if (adapter.getDataSize() == 0) {
            showEmpty("Không tìm thấy sản phẩm phù hợp");
        } else {
            hideEmpty();
        }
    }

    private void showEmpty(String message) {
        if (txtEmpty != null) {
            txtEmpty.setText(message);
        }

        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.VISIBLE);
        } else if (txtEmpty != null) {
            txtEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmpty() {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        } else if (txtEmpty != null) {
            txtEmpty.setVisibility(View.GONE);
        }
    }

    private void updateTabs() {
        setTabState(tabRelevance, "relevance".equals(sortBy));
        setTabState(tabLatest, "newest".equals(sortBy));
        setTabState(tabTopSales, "sold_desc".equals(sortBy));

        boolean priceActive = "price_asc".equals(sortBy)
                || "price_desc".equals(sortBy);

        setTabState(tabPrice, priceActive);

        if ("price_asc".equals(sortBy)) {
            tabPrice.setText("Giá ↑");
        } else if ("price_desc".equals(sortBy)) {
            tabPrice.setText("Giá ↓");
        } else {
            tabPrice.setText("Giá");
        }
    }

    private void setTabState(TextView tab, boolean active) {
        tab.setTextColor(ContextCompat.getColor(
                this,
                active ? R.color.brand_primary : R.color.text_secondary
        ));

        tab.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);

        tab.setBackgroundResource(active
                ? R.drawable.bg_search_tab_active
                : R.drawable.bg_search_tab_inactive
        );
    }

    private void updateFilterButtonState() {
        boolean hasFilter = selectedCategoryId != null
                || selectedMinPrice != null
                || selectedMaxPrice != null;

        // Thay vì dùng setText (gây lỗi do là ImageView), chúng ta sẽ nhuộm màu lại icon
        int tintColor = ContextCompat.getColor(
                this,
                hasFilter ? R.color.brand_primary : R.color.text_secondary
        );

        btnFilter.setColorFilter(tintColor);
    }

    private void showKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
        }
    }
}