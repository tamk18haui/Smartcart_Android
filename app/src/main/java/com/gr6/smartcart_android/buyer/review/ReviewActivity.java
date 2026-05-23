package com.gr6.smartcart_android.buyer.review;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.cloudinary.CloudinaryRepository;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends BaseActivity {

    public static final String EXTRA_ORDER_ITEM_ID = "order_item_id";
    public static final String EXTRA_PRODUCT_NAME = "product_name";
    public static final String EXTRA_PRODUCT_IMAGE = "product_image";
    public static final String EXTRA_REVIEW_ITEMS_JSON = "review_items_json";

    private LinearLayout layoutReviewProductPicker;
    private androidx.recyclerview.widget.RecyclerView rcvReviewProducts;
    private ReviewProductPickerAdapter productPickerAdapter;

    private final List<ReviewItemArg> reviewItems = new ArrayList<>();

    private ImageView imgBack;
    private ImageView imgProduct;

    private TextView txtProductName;
    private TextView star1;
    private TextView star2;
    private TextView star3;
    private TextView star4;
    private TextView star5;

    private EditText edtComment;

    private LinearLayout layoutSelectedImages;
    private TextView btnChooseImages;
    private TextView btnChooseVideo;
    private TextView txtVideoStatus;
    private TextView btnRemoveVideo;
    private TextView btnSubmitReview;

    private ReviewViewModel viewModel;
    private CloudinaryRepository cloudinaryRepository;

    private ActivityResultLauncher<String> pickImagesLauncher;
    private ActivityResultLauncher<String> pickVideoLauncher;

    private final List<Uri> selectedImageUris = new ArrayList<>();
    private final List<String> uploadedImageUrls = new ArrayList<>();

    private Uri selectedVideoUri;
    private String uploadedVideoUrl;

    private Long orderItemId;
    private String productName;
    private String productImage;

    private int selectedRating = 5;
    private boolean uploading = false;
    private int currentUploadIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        viewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        cloudinaryRepository = new CloudinaryRepository();

        setupMediaPickers();

        // Phải initViews trước readIntent
        // Vì readIntent có thể gọi selectFirstUnreviewedItem -> resetReviewForm -> updateStars
        initViews();

        readIntent();
        bindData();
        initEvents();
        observeData();

        updateStars();
        renderSelectedImages();
        renderSelectedVideo();
        updateSubmitState();
    }

    private void setupMediaPickers() {
        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris == null || uris.isEmpty()) return;

                    selectedImageUris.clear();
                    uploadedImageUrls.clear();

                    for (Uri uri : uris) {
                        if (uri == null) continue;

                        selectedImageUris.add(uri);

                        if (selectedImageUris.size() == 4) {
                            break;
                        }
                    }

                    renderSelectedImages();
                    uploadImagesSequentially(0);
                }
        );

        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;

                    selectedVideoUri = uri;
                    uploadedVideoUrl = null;

                    renderSelectedVideo();
                    uploadVideo(uri);
                }
        );
    }

    private void readIntent() {
        String itemsJson = getIntent().getStringExtra(EXTRA_REVIEW_ITEMS_JSON);

        if (itemsJson != null && !itemsJson.trim().isEmpty()) {
            try {
                java.lang.reflect.Type type =
                        new com.google.gson.reflect.TypeToken<List<ReviewItemArg>>() {}.getType();

                List<ReviewItemArg> data = new com.google.gson.Gson().fromJson(itemsJson, type);

                reviewItems.clear();

                if (data != null) {
                    for (ReviewItemArg item : data) {
                        if (item != null
                                && item.getOrderItemId() != null
                                && item.getOrderItemId() > 0) {
                            reviewItems.add(item);
                        }
                    }
                }

            } catch (Exception e) {
                reviewItems.clear();
            }
        }

        if (reviewItems.isEmpty()) {
            long id = getIntent().getLongExtra(EXTRA_ORDER_ITEM_ID, -1L);

            orderItemId = id <= 0 ? null : id;
            productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
            productImage = getIntent().getStringExtra(EXTRA_PRODUCT_IMAGE);

            if (orderItemId != null) {
                reviewItems.add(new ReviewItemArg(
                        orderItemId,
                        productName,
                        productImage,
                        "",
                        false
                ));
            }

            return;
        }

        selectFirstUnreviewedItem();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        imgProduct = findViewById(R.id.imgProduct);

        txtProductName = findViewById(R.id.txtProductName);

        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);

        edtComment = findViewById(R.id.edtComment);

        layoutSelectedImages = findViewById(R.id.layoutSelectedImages);
        btnChooseImages = findViewById(R.id.btnChooseImages);
        btnChooseVideo = findViewById(R.id.btnChooseVideo);
        txtVideoStatus = findViewById(R.id.txtVideoStatus);
        btnRemoveVideo = findViewById(R.id.btnRemoveVideo);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        layoutReviewProductPicker = findViewById(R.id.layoutReviewProductPicker);
        rcvReviewProducts = findViewById(R.id.rcvReviewProducts);
    }

    private void bindData() {
        renderSelectedProduct();
        setupReviewProductPicker();
    }

    private void renderSelectedProduct() {
        if (txtProductName != null) {
            txtProductName.setText(
                    productName == null || productName.trim().isEmpty()
                            ? "Sản phẩm"
                            : productName.trim()
            );
        }

        if (imgProduct != null) {
            if (productImage == null || productImage.trim().isEmpty()) {
                imgProduct.setImageResource(R.drawable.ic_cart);
            } else {
                ImageLoader.load(this, productImage, imgProduct);
            }
        }
    }

    private void setupReviewProductPicker() {
        if (layoutReviewProductPicker == null || rcvReviewProducts == null) {
            return;
        }

        if (reviewItems.size() <= 1) {
            layoutReviewProductPicker.setVisibility(View.GONE);
            return;
        }

        layoutReviewProductPicker.setVisibility(View.VISIBLE);

        if (productPickerAdapter == null) {
            productPickerAdapter = new ReviewProductPickerAdapter();

            rcvReviewProducts.setLayoutManager(
                    new androidx.recyclerview.widget.LinearLayoutManager(
                            this,
                            androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                            false
                    )
            );

            rcvReviewProducts.setAdapter(productPickerAdapter);

            productPickerAdapter.setOnItemClickListener(item -> {
                if (item == null || item.getOrderItemId() == null) return;

                selectReviewItem(item, true);
            });
        }

        productPickerAdapter.setData(reviewItems, orderItemId);
    }

    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());

        star1.setOnClickListener(v -> setRating(1));
        star2.setOnClickListener(v -> setRating(2));
        star3.setOnClickListener(v -> setRating(3));
        star4.setOnClickListener(v -> setRating(4));
        star5.setOnClickListener(v -> setRating(5));

        btnChooseImages.setOnClickListener(v -> {
            if (uploading) {
                showToast("Đang upload, vui lòng chờ");
                return;
            }

            pickImagesLauncher.launch("image/*");
        });

        btnChooseVideo.setOnClickListener(v -> {
            if (uploading) {
                showToast("Đang upload, vui lòng chờ");
                return;
            }

            pickVideoLauncher.launch("video/*");
        });

        btnRemoveVideo.setOnClickListener(v -> {
            if (uploading) {
                showToast("Đang upload, vui lòng chờ");
                return;
            }

            selectedVideoUri = null;
            uploadedVideoUrl = null;
            renderSelectedVideo();
        });

        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void observeData() {
        viewModel.getReviewState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                markCurrentItemReviewed();
                setResult(RESULT_OK);

                if (hasUnreviewedItem()) {
                    showToast("Đánh giá thành công. Bạn có thể chọn sản phẩm khác.");
                    selectFirstUnreviewedItem();
                    setupReviewProductPicker();
                } else {
                    showToast("Đã đánh giá xong tất cả sản phẩm");
                    finish();
                }
            } else {
                showLongToast(state.getMessage());
            }
        });
    }

    private void selectReviewItem(
            ReviewItemArg item,
            boolean resetForm
    ) {
        if (item == null) return;

        if (item.isReviewed()) {
            showToast("Sản phẩm này đã được đánh giá");
            return;
        }

        orderItemId = item.getOrderItemId();
        productName = item.getProductName();
        productImage = item.getProductImage();

        if (resetForm) {
            resetReviewForm();
        }

        renderSelectedProduct();

        if (productPickerAdapter != null) {
            productPickerAdapter.setSelectedOrderItemId(orderItemId);
        }
    }

    private void selectFirstUnreviewedItem() {
        for (ReviewItemArg item : reviewItems) {
            if (item != null && !item.isReviewed()) {
                selectReviewItem(item, true);
                return;
            }
        }
    }

    private boolean hasUnreviewedItem() {
        for (ReviewItemArg item : reviewItems) {
            if (item != null && !item.isReviewed()) {
                return true;
            }
        }

        return false;
    }

    private void markCurrentItemReviewed() {
        for (ReviewItemArg item : reviewItems) {
            if (item != null
                    && item.getOrderItemId() != null
                    && item.getOrderItemId().equals(orderItemId)) {
                item.setReviewed(true);
                break;
            }
        }

        if (productPickerAdapter != null) {
            productPickerAdapter.notifyDataSetChanged();
        }
    }

    private void resetReviewForm() {
        selectedRating = 5;
        currentUploadIndex = 0;
        uploading = false;

        selectedImageUris.clear();
        uploadedImageUrls.clear();

        selectedVideoUri = null;
        uploadedVideoUrl = null;

        if (edtComment != null) {
            edtComment.setText("");
        }

        updateStars();
        renderSelectedImages();
        renderSelectedVideo();
        updateSubmitState();
    }

    private void setRating(int rating) {
        selectedRating = rating;
        updateStars();
    }

    private void updateStars() {
        star1.setText(selectedRating >= 1 ? "★" : "☆");
        star2.setText(selectedRating >= 2 ? "★" : "☆");
        star3.setText(selectedRating >= 3 ? "★" : "☆");
        star4.setText(selectedRating >= 4 ? "★" : "☆");
        star5.setText(selectedRating >= 5 ? "★" : "☆");
    }

    private void uploadImagesSequentially(int index) {
        if (index >= selectedImageUris.size()) {
            uploading = false;
            currentUploadIndex = 0;
            hideLoading();
            updateSubmitState();
            showToast("Upload ảnh đánh giá thành công");
            return;
        }

        uploading = true;
        currentUploadIndex = index + 1;
        showLoading();
        updateSubmitState();
        renderSelectedImages();

        Uri uri = selectedImageUris.get(index);

        cloudinaryRepository.uploadImage(this, uri, new CloudinaryRepository.UploadCallback() {
            @Override
            public void onSuccess(String mediaUrl) {
                runOnUiThread(() -> {
                    if (mediaUrl != null && !mediaUrl.trim().isEmpty()) {
                        uploadedImageUrls.add(mediaUrl.trim());
                    }

                    uploadImagesSequentially(index + 1);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    uploading = false;
                    currentUploadIndex = 0;
                    hideLoading();
                    updateSubmitState();
                    showLongToast(message == null ? "Upload ảnh thất bại" : message);
                });
            }
        });
    }

    private void uploadVideo(Uri uri) {
        uploading = true;
        showLoading();
        updateSubmitState();
        renderSelectedVideo();

        cloudinaryRepository.uploadVideo(this, uri, new CloudinaryRepository.UploadCallback() {
            @Override
            public void onSuccess(String mediaUrl) {
                runOnUiThread(() -> {
                    uploading = false;
                    hideLoading();

                    uploadedVideoUrl = mediaUrl;

                    renderSelectedVideo();
                    updateSubmitState();
                    showToast("Upload video đánh giá thành công");
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    uploading = false;
                    hideLoading();

                    selectedVideoUri = null;
                    uploadedVideoUrl = null;

                    renderSelectedVideo();
                    updateSubmitState();
                    showLongToast(message == null ? "Upload video thất bại" : message);
                });
            }
        });
    }

    private void renderSelectedImages() {
        layoutSelectedImages.removeAllViews();

        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri uri = selectedImageUris.get(i);

            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageURI(uri);
            imageView.setBackgroundResource(R.drawable.bg_review_image_box);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(72),
                    dp(72)
            );
            params.setMargins(0, 0, dp(8), 0);

            layoutSelectedImages.addView(imageView, params);
        }

        if (uploading && !selectedImageUris.isEmpty()) {
            btnChooseImages.setText(
                    "Đang upload ảnh "
                            + currentUploadIndex
                            + "/"
                            + selectedImageUris.size()
            );
        } else {
            btnChooseImages.setText("Chọn tối đa 4 ảnh (" + uploadedImageUrls.size() + "/4)");
        }
    }

    private void renderSelectedVideo() {
        boolean hasSelectedVideo = selectedVideoUri != null;
        boolean hasUploadedVideo = uploadedVideoUrl != null && !uploadedVideoUrl.trim().isEmpty();

        if (!hasSelectedVideo) {
            txtVideoStatus.setText("Chưa chọn video");
            btnRemoveVideo.setVisibility(View.GONE);
            return;
        }

        btnRemoveVideo.setVisibility(View.VISIBLE);

        if (hasUploadedVideo) {
            txtVideoStatus.setText("Đã upload video đánh giá");
        } else {
            txtVideoStatus.setText("Đã chọn video, đang upload...");
        }
    }

    private void submitReview() {
        if (uploading) {
            showToast("Ảnh hoặc video đang upload, vui lòng chờ");
            return;
        }

        if (orderItemId == null || orderItemId <= 0) {
            showToast("Không tìm thấy sản phẩm trong đơn hàng");
            return;
        }

        if (selectedRating < 1 || selectedRating > 5) {
            showToast("Vui lòng chọn số sao đánh giá");
            return;
        }

        if (!selectedImageUris.isEmpty() && uploadedImageUrls.size() < selectedImageUris.size()) {
            showToast("Ảnh chưa upload xong, vui lòng thử lại");
            return;
        }

        if (selectedVideoUri != null
                && (uploadedVideoUrl == null || uploadedVideoUrl.trim().isEmpty())) {
            showToast("Video chưa upload xong, vui lòng thử lại");
            return;
        }

        String comment = edtComment.getText().toString().trim();

        if (comment.length() > 2000) {
            showToast("Nội dung đánh giá không được vượt quá 2000 ký tự");
            return;
        }

        viewModel.submitReview(
                orderItemId,
                selectedRating,
                comment,
                uploadedImageUrls,
                uploadedVideoUrl
        );
    }

    private void updateSubmitState() {
        boolean enabled = !uploading;

        btnSubmitReview.setEnabled(enabled);
        btnSubmitReview.setAlpha(enabled ? 1f : 0.55f);

        btnChooseImages.setEnabled(enabled);
        btnChooseVideo.setEnabled(enabled);
        btnRemoveVideo.setEnabled(enabled);
    }

    public static class ReviewItemArg {

        private Long orderItemId;
        private String productName;
        private String productImage;
        private String variantSku;
        private boolean reviewed;

        public ReviewItemArg() {
        }

        public ReviewItemArg(
                Long orderItemId,
                String productName,
                String productImage,
                String variantSku,
                boolean reviewed
        ) {
            this.orderItemId = orderItemId;
            this.productName = productName;
            this.productImage = productImage;
            this.variantSku = variantSku;
            this.reviewed = reviewed;
        }

        public Long getOrderItemId() {
            return orderItemId;
        }

        public String getProductName() {
            if (productName == null || productName.trim().isEmpty()) {
                return "Sản phẩm";
            }

            return productName.trim();
        }

        public String getProductImage() {
            return productImage == null ? "" : productImage;
        }

        public String getVariantSku() {
            return variantSku == null ? "" : variantSku;
        }

        public boolean isReviewed() {
            return reviewed;
        }

        public void setReviewed(boolean reviewed) {
            this.reviewed = reviewed;
        }
    }
}