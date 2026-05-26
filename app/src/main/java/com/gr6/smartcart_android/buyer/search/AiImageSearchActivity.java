package com.gr6.smartcart_android.buyer.search;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.category.CategoryProductAdapter;
import com.gr6.smartcart_android.buyer.main.repository.BuyerHomeRepository;
import com.gr6.smartcart_android.buyer.main.response.HomeProductResponse;
import com.gr6.smartcart_android.buyer.main.response.RecommendationPageResponse;
import com.gr6.smartcart_android.buyer.product.ProductDetailActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.util.ArrayList;
import java.util.List;

public class AiImageSearchActivity extends BaseActivity {

    private static final int PAGE_SIZE = 20;

    private ImageView imgBack;
    private ImageView imgPreview;
    private TextView btnChooseImage;
    private TextView btnSearchImage;
    private TextView txtEmpty;
    private View layoutImageHint;
    private View layoutEmpty;
    private TextView txtResultCount;
    private RecyclerView rcvProducts;

    private BuyerHomeRepository repository;
    private CategoryProductAdapter adapter;

    private Uri selectedImageUri;

    private int currentPage = 0;
    private boolean lastPage = false;
    private boolean loading = false;

    private final List<HomeProductResponse> products = new ArrayList<>();

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_image_search);

        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        repository = new BuyerHomeRepository(this);

        registerImagePicker();
        initViews();
        setupRecyclerView();
        initEvents();

        showEmpty("Chọn một ảnh sản phẩm để AI tìm sản phẩm tương tự");
    }

    private void registerImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;

                    selectedImageUri = uri;
                    imgPreview.setImageURI(uri);
                    if (layoutImageHint != null) {
                        layoutImageHint.setVisibility(View.GONE);
                    }
                    btnSearchImage.setEnabled(true);
                    btnSearchImage.setAlpha(1f);

                    products.clear();
                    adapter.clear();
                    updateResultCount();
                    showEmpty("Đã chọn ảnh. Bấm Tìm kiếm bằng ảnh để bắt đầu.");
                }
        );
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        imgPreview = findViewById(R.id.imgPreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSearchImage = findViewById(R.id.btnSearchImage);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtResultCount = findViewById(R.id.txtResultCount);
        rcvProducts = findViewById(R.id.rcvProducts);
        layoutImageHint = findViewById(R.id.layoutImageHint);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        btnSearchImage.setEnabled(false);
        btnSearchImage.setAlpha(0.45f);
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

                if (dy <= 0 || loading || lastPage || selectedImageUri == null) return;

                int visible = layoutManager.getChildCount();
                int total = layoutManager.getItemCount();
                int first = layoutManager.findFirstVisibleItemPosition();

                if (visible + first >= total - 4) {
                    searchByImage(false);
                }
            }
        });
    }

    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());

        btnChooseImage.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        btnSearchImage.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                showToast("Vui lòng chọn ảnh trước");
                return;
            }

            searchByImage(true);
        });
    }

    private void searchByImage(boolean refresh) {
        if (selectedImageUri == null || loading) return;

        if (refresh) {
            currentPage = 0;
            lastPage = false;
            products.clear();
            adapter.clear();
        }

        loading = true;
        showLoading();

        repository.searchByImage(
                selectedImageUri,
                currentPage,
                PAGE_SIZE,
                new BuyerHomeRepository.HomeCallback<RecommendationPageResponse>() {
                    @Override
                    public void onSuccess(RecommendationPageResponse data) {
                        runOnUiThread(() -> {
                            hideLoading();
                            loading = false;

                            if (data == null) {
                                showEmpty("Không có dữ liệu trả về");
                                return;
                            }

                            List<HomeProductResponse> newProducts = data.getProducts();

                            if (refresh) {
                                products.clear();
                                adapter.setData(newProducts);
                            } else {
                                adapter.appendData(newProducts);
                            }

                            products.addAll(newProducts);

                            currentPage = data.getPageIndexZeroBased();
                            lastPage = data.isLast();

                            if (!lastPage) {
                                currentPage++;
                            }

                            updateResultCount();

                            if (products.isEmpty()) {
                                showEmpty("AI chưa tìm thấy sản phẩm giống ảnh này");
                            } else {
                                if (layoutEmpty != null) {
                                    layoutEmpty.setVisibility(View.GONE);
                                }
                                rcvProducts.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            hideLoading();
                            loading = false;
                            showEmpty(message);
                            showLongToast(message);
                        });
                    }
                }
        );
    }

    private void showEmpty(String message) {
        rcvProducts.setVisibility(View.GONE);

        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.VISIBLE);
        }

        if (txtEmpty != null) {
            if (message == null || message.trim().isEmpty()) {
                message = "Không có sản phẩm";
            }

            txtEmpty.setText(message);
        }
    }
    private void updateResultCount() {
        txtResultCount.setText(products.size() + " sản phẩm");
    }
}