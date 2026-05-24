package com.gr6.smartcart_android.buyer.product;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.product.repository.ProductRepository;
import com.gr6.smartcart_android.buyer.product.response.ProductDetailResponse;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.util.List;
import java.util.Locale;

public class ProductReviewsActivity extends BaseActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    private ImageView imgBack;
    private TextView txtProductName;
    private TextView txtReviewSummary;
    private TextView txtReviewCount;
    private TextView txtEmpty;
    private LinearLayout layoutEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rcvReviews;

    private ProductRepository productRepository;
    private ProductReviewAdapter adapter;

    private Long productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_reviews);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        productRepository = new ProductRepository(this);

        readIntent();
        initViews();
        setupRecyclerView();
        initEvents();

        loadReviews(true);
    }

    private void readIntent() {
        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1L);

        if (productId == -1L) {
            productId = null;
        }
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        txtProductName = findViewById(R.id.txtProductName);
        txtReviewSummary = findViewById(R.id.txtReviewSummary);
        txtReviewCount = findViewById(R.id.txtReviewCount);
        txtEmpty = findViewById(R.id.txtEmpty);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rcvReviews = findViewById(R.id.rcvReviews);
    }

    private void setupRecyclerView() {
        adapter = new ProductReviewAdapter(this);

        rcvReviews.setLayoutManager(new LinearLayoutManager(this));
        rcvReviews.setAdapter(adapter);
    }

    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());

        swipeRefresh.setOnRefreshListener(() -> loadReviews(false));
    }

    private void loadReviews(boolean showFullLoading) {
        if (productId == null || productId <= 0) {
            showEmpty("Không tìm thấy sản phẩm");
            return;
        }

        if (showFullLoading) {
            showLoading();
        }

        productRepository.getProductDetail(productId, new ProductRepository.ProductDetailCallback() {
            @Override
            public void onSuccess(ProductDetailResponse data) {
                runOnUiThread(() -> {
                    hideLoading();
                    stopRefresh();

                    if (data == null) {
                        showEmpty("Không có dữ liệu đánh giá");
                        return;
                    }

                    bindData(data);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    hideLoading();
                    stopRefresh();
                    showEmpty(message == null ? "Không tải được đánh giá" : message);
                });
            }
        });
    }

    private void bindData(ProductDetailResponse detail) {
        txtProductName.setText(detail.getName());

        int reviewCount = detail.getReviewCount();
        double averageRating = detail.getAverageRating();

        txtReviewSummary.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f/5",
                        averageRating
                )
        );

        txtReviewCount.setText(reviewCount + " đánh giá");

        List<ProductDetailResponse.ReviewDTO> reviews = detail.getReviews();

        if (reviews == null || reviews.isEmpty()) {
            adapter.setData(null);
            showEmpty("Sản phẩm này chưa có đánh giá nào.");
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        rcvReviews.setVisibility(View.VISIBLE);

        adapter.setData(reviews);
    }

    private void showEmpty(String message) {
        rcvReviews.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);

        if (message == null || message.trim().isEmpty()) {
            message = "Không có đánh giá";
        }

        txtEmpty.setText(message);
    }

    private void stopRefresh() {
        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }
}