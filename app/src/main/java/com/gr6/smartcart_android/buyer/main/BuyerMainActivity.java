package com.gr6.smartcart_android.buyer.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.gr6.smartcart_android.buyer.cart.api.CartApiService;
import com.gr6.smartcart_android.buyer.cart.response.CartDetailResponse;
import com.gr6.smartcart_android.buyer.cart.CartActivity;
import com.gr6.smartcart_android.buyer.main.response.HomeCategoryResponse;
import com.gr6.smartcart_android.buyer.main.response.HomeProductResponse;
import com.gr6.smartcart_android.buyer.product.ProductDetailActivity;
import com.gr6.smartcart_android.buyer.search.AiImageSearchActivity;
import com.gr6.smartcart_android.buyer.search.SearchProductActivity;
import com.gr6.smartcart_android.chat.ChatListActivity;
import com.gr6.smartcart_android.chat.api.ChatApiService;
import com.gr6.smartcart_android.chat.response.ConversationResponse;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.AuthGuard;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.navigation.BuyerBottomNavHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuyerMainActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rcvCategories;
    private RecyclerView rcvProducts;

    private EditText edtSearch;
    private ImageView btnHomeCameraSearch;
    private ImageView imgCart;
    private ImageView imgMessage;

    private TextView txtMessageBadge;
    private TextView txtCartBadge;
    private TextView txtUserName;
    private TextView txtEmpty;
    private TextView txtProductCount;
    private NestedScrollView nestedHome;

    private BuyerHomeViewModel viewModel;
    private CategoryHomeAdapter categoryAdapter;
    private ProductHomeAdapter productAdapter;
    private CartApiService cartApiService;
    private ChatApiService chatApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_main);

        setupSystemBars();

        viewModel = new ViewModelProvider(this).get(BuyerHomeViewModel.class);
        cartApiService = ApiClient.createService(this, CartApiService.class);
        chatApiService = ApiClient.createService(this, ChatApiService.class);

        initViews();
        setupRecyclerViews();
        initEvents();
        observeHome();

        BuyerBottomNavHelper.setup(this, BuyerBottomNavHelper.TAB_HOME);

        viewModel.loadHome();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Không giữ text search ở trang chủ.
        // Search thật xử lý ở SearchProductActivity.
        if (edtSearch != null) {
            edtSearch.setText("");
            edtSearch.clearFocus();
        }

        loadHeaderBadges();
    }

    private void setupSystemBars() {
        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.brand_primary));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = window.getDecorView().getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }


    private void initViews() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rcvCategories = findViewById(R.id.rcvCategories);
        rcvProducts = findViewById(R.id.rcvProducts);

        btnHomeCameraSearch = findViewById(R.id.btnHomeCameraSearch);
        edtSearch = findViewById(R.id.edtSearch);
        imgCart = findViewById(R.id.imgCart);
        imgMessage = findViewById(R.id.imgMessage);
        txtUserName = findViewById(R.id.txtUserName);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtProductCount = findViewById(R.id.txtProductCount);
        nestedHome = findViewById(R.id.nestedHome);
        txtMessageBadge = findViewById(R.id.txtMessageBadge);
        txtCartBadge = findViewById(R.id.txtCartBadge);

        // Ô search ở trang chủ chỉ dùng để mở màn search riêng.
        edtSearch.setFocusable(false);
        edtSearch.setFocusableInTouchMode(false);
        edtSearch.setCursorVisible(false);

        bindUserGreeting();
    }

    private void bindUserGreeting() {
        String fullName = UserSession.getInstance(this).getFullName();

        if (fullName == null || fullName.trim().isEmpty()) {
            txtUserName.setText("Xin chào bạn 👋");
        } else {
            txtUserName.setText("Xin chào, " + fullName.trim());
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
        swipeRefresh.setOnRefreshListener(() -> {
            edtSearch.setText("");
            edtSearch.clearFocus();
            viewModel.refreshHome();
        });
        if (btnHomeCameraSearch != null) {
            btnHomeCameraSearch.setOnClickListener(v -> {
                Intent intent = new Intent(this, AiImageSearchActivity.class);
                startActivity(intent);
            });
        }

        imgCart.setOnClickListener(v -> {
            if (!AuthGuard.requireLogin(this, CartActivity.class)) return;

            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        imgMessage.setOnClickListener(v -> {
            if (!AuthGuard.requireLogin(this, ChatListActivity.class)) return;

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
            if (product == null || product.getProductId() == null) {
                showToast("Không tìm thấy sản phẩm");
                return;
            }

            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getProductId());
            startActivity(intent);
        });

        edtSearch.setOnClickListener(v -> openSearchScreen());

        setupLoadMoreScroll();
    }

    private void loadHeaderBadges() {
        if (!TokenManager.getInstance(this).hasToken()) {
            updateMessageBadge(0);
            updateCartBadge(0);
            return;
        }

        loadUnreadMessageBadge();
        loadCartBadge();
    }

    private void loadUnreadMessageBadge() {
        if (chatApiService == null) {
            updateMessageBadge(0);
            return;
        }

        chatApiService.getConversations().enqueue(new Callback<BaseResponse<List<ConversationResponse>>>() {
            @Override
            public void onResponse(
                    Call<BaseResponse<List<ConversationResponse>>> call,
                    Response<BaseResponse<List<ConversationResponse>>> response
            ) {
                int totalUnread = 0;

                if (response.body() != null && response.body().getData() != null) {
                    for (ConversationResponse conversation : response.body().getData()) {
                        if (conversation != null) {
                            totalUnread += Math.max(conversation.getUnreadCount(), 0);
                        }
                    }
                }

                updateMessageBadge(totalUnread);
            }

            @Override
            public void onFailure(
                    Call<BaseResponse<List<ConversationResponse>>> call,
                    Throwable t
            ) {
                updateMessageBadge(0);
            }
        });
    }

    private void loadCartBadge() {
        if (cartApiService == null) {
            updateCartBadge(0);
            return;
        }

        cartApiService.getCartItems().enqueue(new Callback<BaseResponse<CartDetailResponse>>() {
            @Override
            public void onResponse(
                    Call<BaseResponse<CartDetailResponse>> call,
                    Response<BaseResponse<CartDetailResponse>> response
            ) {
                int totalItems = 0;

                if (response.body() != null && response.body().getData() != null) {
                    Integer value = response.body().getData().getTotalItems();
                    totalItems = value == null ? 0 : Math.max(value, 0);
                }

                updateCartBadge(totalItems);
            }

            @Override
            public void onFailure(Call<BaseResponse<CartDetailResponse>> call, Throwable t) {
                updateCartBadge(0);
            }
        });
    }

    private void updateMessageBadge(int unread) {
        renderHeaderBadge(txtMessageBadge, unread);
    }

    private void updateCartBadge(int totalItems) {
        renderHeaderBadge(txtCartBadge, totalItems);
    }

    private void renderHeaderBadge(TextView badge, int count) {
        if (badge == null) return;

        if (count <= 0) {
            badge.setVisibility(View.GONE);
            return;
        }

        badge.setVisibility(View.VISIBLE);
        badge.setText(count > 99 ? "99+" : String.valueOf(count));
    }

    private void setupLoadMoreScroll() {
        if (nestedHome == null) return;

        nestedHome.setOnScrollChangeListener(
                (NestedScrollView.OnScrollChangeListener) (
                        v,
                        scrollX,
                        scrollY,
                        oldScrollX,
                        oldScrollY
                ) -> {
                    View child = v.getChildAt(0);

                    if (child == null) return;

                    int diff = child.getBottom() - (v.getHeight() + scrollY);

                    if (diff <= dp(120)) {
                        viewModel.loadMoreProducts();
                    }
                }
        );
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
                return;
            }

            txtEmpty.setVisibility(View.VISIBLE);
            txtEmpty.setText(
                    state.getMessage() == null
                            ? "Không tải được sản phẩm"
                            : state.getMessage()
            );

            updateProductCount(0);

            if (state.getMessage() != null) {
                showLongToast(state.getMessage());
            }
        });
    }

    private void bindHomeData(
            List<HomeCategoryResponse> categories,
            List<HomeProductResponse> products
    ) {
        categoryAdapter.setData(categories);
        productAdapter.setData(products);

        int count = products == null ? 0 : products.size();
        updateProductCount(count);

        if (count == 0) {
            txtEmpty.setVisibility(View.VISIBLE);
            txtEmpty.setText("Chưa có sản phẩm nào");
        } else {
            txtEmpty.setVisibility(View.GONE);
        }
    }

    private void openSearchScreen() {
        Intent intent = new Intent(this, SearchProductActivity.class);

        // Không truyền keyword sang màn search.
        // Đúng luồng: mở màn search trống -> người dùng nhập -> mới gọi API.
        startActivity(intent);

        edtSearch.clearFocus();
    }

    private void updateProductCount(int count) {
        txtProductCount.setText(count + " sản phẩm");
    }

}


