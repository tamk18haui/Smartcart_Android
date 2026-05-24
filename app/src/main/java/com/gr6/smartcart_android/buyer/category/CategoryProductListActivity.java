package com.gr6.smartcart_android.buyer.category;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.main.repository.BuyerHomeRepository;
import com.gr6.smartcart_android.buyer.main.request.SearchProductRequest;
import com.gr6.smartcart_android.buyer.main.response.HomeProductResponse;
import com.gr6.smartcart_android.buyer.main.response.ProductPageResponse;
import com.gr6.smartcart_android.buyer.product.ProductDetailActivity;
import com.gr6.smartcart_android.buyer.search.SearchProductActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.navigation.BuyerBottomNavHelper;

import java.util.List;

public class CategoryProductListActivity extends BaseActivity {

    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_CATEGORY_NAME = "category_name";

    private static final int PAGE_SIZE = 20;

    private ImageView imgBack;
    private ImageView imgSearch;
    private ImageView imgFilter;

    private TextView txtCategoryTitle;
    private TextView txtSortLabel;
    private TextView txtProductCount;
    private TextView txtEmpty;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rcvProducts;
    private View layoutEmpty;

    private BuyerHomeRepository repository;
    private CategoryProductAdapter adapter;

    private Long categoryId;
    private String categoryName = "";

    private String sortBy = "relevance";

    private int currentPage = 0;
    private boolean lastPage = false;
    private boolean loading = false;
    private int requestVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_product_list);

        ThemeColor.applyWhiteNavigationBar(this);

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.surface));

        repository = new BuyerHomeRepository(this);

        readIntent();
        initViews();
        setupRecyclerView();
        initEvents();

        BuyerBottomNavHelper.setup(this, BuyerBottomNavHelper.TAB_CATEGORY);

        loadProducts(true);
    }

    private void readIntent() {
        Intent intent = getIntent();

        if (intent == null) return;

        long id = intent.getLongExtra(EXTRA_CATEGORY_ID, -1L);
        categoryId = id > 0 ? id : null;

        String name = intent.getStringExtra(EXTRA_CATEGORY_NAME);
        categoryName = name == null ? "" : name.trim();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        imgSearch = findViewById(R.id.imgSearch);
        imgFilter = findViewById(R.id.imgFilter);

        txtCategoryTitle = findViewById(R.id.txtCategoryTitle);
        txtSortLabel = findViewById(R.id.txtSortLabel);
        txtProductCount = findViewById(R.id.txtProductCount);
        txtEmpty = findViewById(R.id.txtEmpty);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        rcvProducts = findViewById(R.id.rcvProducts);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        if (categoryName == null || categoryName.trim().isEmpty()) {
            txtCategoryTitle.setText("Danh mục");
        } else {
            txtCategoryTitle.setText(categoryName);
        }

        updateSortText();
    }

    private void setupRecyclerView() {
        adapter = new CategoryProductAdapter();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rcvProducts.setLayoutManager(layoutManager);
        rcvProducts.setAdapter(adapter);
        rcvProducts.setHasFixedSize(true);

        adapter.setOnProductClickListener(product -> {
            if (product == null || product.getProductId() == null) {
                showToast("Không tìm thấy sản phẩm");
                return;
            }

            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getProductId());
            startActivity(intent);
        });

        rcvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(
                    RecyclerView recyclerView,
                    int dx,
                    int dy
            ) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy <= 0 || loading || lastPage) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount - 4) {
                    loadProducts(false);
                }
            }
        });
    }

    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());

        imgSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchProductActivity.class);
            intent.putExtra("keyword", categoryName);

            if (categoryId != null) {
                intent.putExtra("category_id", categoryId);
            }

            startActivity(intent);
        });

        imgFilter.setOnClickListener(v -> showSortDialog());

        View sortBar = findViewById(R.id.layoutSortBar);
        if (sortBar != null) {
            sortBar.setOnClickListener(v -> showSortDialog());
        }

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(() -> loadProducts(true));
    }

    private void showSortDialog() {
        String[] labels = {
                "Phổ biến",
                "Mới nhất",
                "Bán chạy",
                "Giá thấp đến cao",
                "Giá cao đến thấp"
        };

        String[] values = {
                "relevance",
                "newest",
                "sold_desc",
                "price_asc",
                "price_desc"
        };

        int checked = 0;

        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(sortBy)) {
                checked = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Sắp xếp sản phẩm")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    sortBy = values[which];
                    updateSortText();
                    dialog.dismiss();
                    loadProducts(true);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void updateSortText() {
        if (txtSortLabel == null) return;

        switch (sortBy) {
            case "newest":
                txtSortLabel.setText("Mới nhất");
                break;
            case "sold_desc":
                txtSortLabel.setText("Bán chạy");
                break;
            case "price_asc":
                txtSortLabel.setText("Giá thấp đến cao");
                break;
            case "price_desc":
                txtSortLabel.setText("Giá cao đến thấp");
                break;
            case "relevance":
            default:
                txtSortLabel.setText("Phổ biến");
                break;
        }
    }

    private void loadProducts(boolean refresh) {
        if (loading) return;

        if (categoryId == null || categoryId <= 0) {
            updateEmpty("Danh mục không hợp lệ");
            return;
        }

        if (refresh) {
            currentPage = 0;
            lastPage = false;
            adapter.clear();
            requestVersion++;
        }

        int version = requestVersion;
        loading = true;

        if (refresh) {
            swipeRefresh.setRefreshing(true);
            layoutEmpty.setVisibility(View.GONE);
        }

        SearchProductRequest request = new SearchProductRequest(
                "",
                categoryId,
                null,
                null,
                sortBy
        );

        repository.searchProducts(
                request,
                currentPage,
                PAGE_SIZE,
                new BuyerHomeRepository.HomeCallback<ProductPageResponse>() {
                    @Override
                    public void onSuccess(ProductPageResponse data) {
                        if (version != requestVersion) return;

                        loading = false;
                        swipeRefresh.setRefreshing(false);

                        if (data == null) {
                            updateEmpty("Không có dữ liệu sản phẩm");
                            return;
                        }

                        List<HomeProductResponse> products = data.getProducts();

                        if (currentPage == 0) {
                            adapter.setData(products);
                        } else {
                            adapter.appendData(products);
                        }

                        long total = data.getTotalElements();
                        if (total <= 0) {
                            total = adapter.getItemCount();
                        }

                        txtProductCount.setText(total + " sản phẩm");

                        lastPage = data.isLast();

                        if (!lastPage) {
                            currentPage++;
                        }

                        updateEmpty("Danh mục này chưa có sản phẩm nào");
                    }

                    @Override
                    public void onError(String message) {
                        if (version != requestVersion) return;

                        loading = false;
                        swipeRefresh.setRefreshing(false);

                        updateEmpty(message == null ? "Không tải được sản phẩm" : message);
                        showLongToast(message == null ? "Không tải được sản phẩm" : message);
                    }
                }
        );
    }

    private void updateEmpty(String message) {
        boolean empty = adapter == null || adapter.getItemCount() == 0;

        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rcvProducts.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (message == null || message.trim().isEmpty()) {
            message = "Không có sản phẩm";
        }

        txtEmpty.setText(message);
    }
}