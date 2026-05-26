package com.gr6.smartcart_android.buyer.search;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "AI_IMAGE_UI";
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
                    if (uri == null) {
                        Log.e(TAG, "Không chọn được ảnh: uri null");
                        showToast("Không chọn được ảnh");
                        return;
                    }

                    selectedImageUri = uri;

                    Log.d(TAG, "Đã chọn ảnh uri = " + uri);

                    imgPreview.setImageURI(uri);

                    if (layoutImageHint != null) {
                        layoutImageHint.setVisibility(View.GONE);
                    }

                    btnSearchImage.setEnabled(true);
                    btnSearchImage.setAlpha(1f);

                    products.clear();
                    adapter.clear();
                    updateResultCount();

                    showEmpty("Đang gửi ảnh để AI tìm sản phẩm...");

                    // Chọn ảnh xong gửi luôn
                    searchByImage(true);
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

                if (dy <= 0) return;
                if (loading) return;
                if (lastPage) return;
                if (selectedImageUri == null) return;

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

        btnChooseImage.setOnClickListener(v -> {
            Log.d(TAG, "Mở chọn ảnh");
            imagePickerLauncher.launch("image/*");
        });

        btnSearchImage.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                showToast("Vui lòng chọn ảnh trước");
                return;
            }

            searchByImage(true);
        });
    }

    private void searchByImage(boolean reset) {
        if (selectedImageUri == null) {
            showToast("Vui lòng chọn ảnh trước");
            return;
        }

        if (loading) {
            Log.d(TAG, "Đang loading, bỏ qua request lặp");
            return;
        }

        if (reset) {
            currentPage = 0;
            lastPage = false;
            products.clear();
            adapter.clear();
            updateResultCount();
        }

        loading = true;
        setSearchButtonLoading(true);

        if (reset) {
            showEmpty("Đang gửi ảnh để AI tìm sản phẩm...");
        }

        Log.d(TAG, "Gửi ảnh searchByImage: uri = " + selectedImageUri
                + ", page = " + currentPage
                + ", size = " + PAGE_SIZE);

        repository.searchByImage(
                selectedImageUri,
                currentPage,
                PAGE_SIZE,
                new BuyerHomeRepository.HomeCallback<RecommendationPageResponse>() {
                    @Override
                    public void onSuccess(RecommendationPageResponse data) {
                        loading = false;
                        setSearchButtonLoading(false);

                        if (data == null) {
                            Log.e(TAG, "Response null");
                            if (products.isEmpty()) {
                                showEmpty("Server không trả dữ liệu tìm kiếm ảnh");
                            }
                            return;
                        }

                        List<HomeProductResponse> newProducts = data.getProducts();

                        Log.d(TAG, "AI image search success, products = "
                                + (newProducts == null ? 0 : newProducts.size())
                                + ", page = " + data.getPage()
                                + ", hasMore = " + data.isHasMore()
                                + ", totalElements = " + data.getTotalElements());

                        if (reset) {
                            products.clear();
                            adapter.clear();
                        }

                        if (newProducts != null && !newProducts.isEmpty()) {
                            products.addAll(newProducts);

                            if (reset) {
                                adapter.setData(products);
                            } else {
                                adapter.appendData(newProducts);
                            }
                        }

                        updateResultCount();

                        lastPage = !data.isHasMore();

                        if (newProducts == null || newProducts.isEmpty()) {
                            lastPage = true;
                        }

                        if (products.isEmpty()) {
                            showEmpty("Không tìm thấy sản phẩm phù hợp với ảnh này");
                        } else {
                            showProducts();
                            currentPage++;
                        }
                    }

                    @Override
                    public void onError(String message) {
                        loading = false;
                        setSearchButtonLoading(false);

                        Log.e(TAG, "AI image search error = " + message);

                        if (products.isEmpty()) {
                            showEmpty(message == null || message.trim().isEmpty()
                                    ? "Không tìm kiếm được bằng hình ảnh"
                                    : message);
                        } else {
                            showToast(message == null || message.trim().isEmpty()
                                    ? "Không tải thêm được sản phẩm"
                                    : message);
                        }
                    }
                }
        );
    }

    private void showProducts() {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }

        rcvProducts.setVisibility(View.VISIBLE);
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
        if (txtResultCount != null) {
            txtResultCount.setText(products.size() + " sản phẩm");
        }
    }

    private void setSearchButtonLoading(boolean isLoading) {
        if (btnSearchImage == null) return;

        btnSearchImage.setEnabled(!isLoading && selectedImageUri != null);
        btnSearchImage.setAlpha(isLoading ? 0.65f : 1f);
        btnSearchImage.setText(isLoading ? "Đang tìm..." : "Tìm kiếm bằng ảnh");
    }
}