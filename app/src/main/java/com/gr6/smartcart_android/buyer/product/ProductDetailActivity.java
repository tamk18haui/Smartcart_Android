package com.gr6.smartcart_android.buyer.product;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.product.response.ProductDetailResponse;
import com.gr6.smartcart_android.chat.ChatRoomActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailActivity extends BaseActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    private static final int ACTION_ADD_TO_CART = 1;
    private static final int ACTION_BUY_NOW = 2;

    private ImageView imgBack;
    private ImageView imgCart;
    private ViewPager2 viewPagerImages;
    private TextView txtImageIndicator;

    private TextView txtProductName;
    private TextView txtPrice;
    private TextView txtStock;
    private TextView txtRating;
    private TextView txtSold;

    private ImageView imgShopAvatar;
    private TextView txtShopName;
    private TextView btnViewShop;

    private LinearLayout layoutVoucherCard;
    private LinearLayout layoutVouchers;
    private TextView txtVoucherMore;

    private TextView txtDescription;
    private TextView txtReviewSummary;
    private TextView txtReviewCount;
    private LinearLayout layoutReviews;

    private View btnMessage;
    private View btnAddCart;
    private TextView btnBuyNow;
    private TextView txtShopStatus;

    private ProductDetailViewModel viewModel;
    private ProductImageAdapter imageAdapter;

    private ProductDetailResponse productDetail;
    private ProductDetailResponse.VariantDTO selectedVariant;

    private final Map<String, String> selectedOptions = new LinkedHashMap<>();
    private final NumberFormat moneyFormat =
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private Long productId;
    private int pendingAction = ACTION_ADD_TO_CART;
    private int selectedQuantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1L);

        if (productId == -1L) {
            showToast("Không tìm thấy sản phẩm");
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);

        initViews();
        setupImages();
        initEvents();
        observeData();

        viewModel.loadProductDetail(productId);
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        imgCart = findViewById(R.id.imgCart);
        viewPagerImages = findViewById(R.id.viewPagerImages);
        txtImageIndicator = findViewById(R.id.txtImageIndicator);

        txtProductName = findViewById(R.id.txtProductName);
        txtPrice = findViewById(R.id.txtPrice);
        txtStock = findViewById(R.id.txtStock);
        txtRating = findViewById(R.id.txtRating);
        txtSold = findViewById(R.id.txtSold);

        imgShopAvatar = findViewById(R.id.imgShopAvatar);
        txtShopName = findViewById(R.id.txtShopName);
        btnViewShop = findViewById(R.id.btnViewShop);

        layoutVoucherCard = findViewById(R.id.layoutVoucherCard);
        layoutVouchers = findViewById(R.id.layoutVouchers);
        txtVoucherMore = findViewById(R.id.txtVoucherMore);

        txtDescription = findViewById(R.id.txtDescription);
        txtReviewSummary = findViewById(R.id.txtReviewSummary);
        txtReviewCount = findViewById(R.id.txtReviewCount);
        layoutReviews = findViewById(R.id.layoutReviews);

        btnMessage = findViewById(R.id.btnMessage);
        btnAddCart = findViewById(R.id.btnAddCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        txtShopStatus = findViewById(R.id.txtShopStatus);
    }

    private void setupImages() {
        imageAdapter = new ProductImageAdapter();
        viewPagerImages.setAdapter(imageAdapter);

        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateImageIndicator(position);
            }
        });
    }

    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());

        imgCart.setOnClickListener(v -> openCart());

        btnViewShop.setOnClickListener(v -> openShop());

        txtVoucherMore.setOnClickListener(v ->
                showToast("Mã giảm giá sẽ được chọn khi thanh toán")
        );

        btnMessage.setOnClickListener(v -> openChatWithShop());

        btnAddCart.setOnClickListener(v -> {
            pendingAction = ACTION_ADD_TO_CART;
            showVariantBottomSheet();
        });

        btnBuyNow.setOnClickListener(v -> {
            pendingAction = ACTION_BUY_NOW;
            showVariantBottomSheet();
        });
    }

    private void openChatWithShop() {
        if (productDetail == null) {
            showToast("Không tìm thấy thông tin sản phẩm");
            return;
        }

        if (productDetail.getShopOwnerId() == null || productDetail.getShopOwnerId() <= 0) {
            showToast("Không tìm thấy chủ shop để nhắn tin");
            return;
        }

        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_ID, productDetail.getShopOwnerId());
        intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_NAME, productDetail.getShopName());
        intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_AVATAR, productDetail.getShopImageUrl());
        startActivity(intent);
    }

    private void observeData() {
        viewModel.getDetailState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                productDetail = state.getData();
                bindProductDetail();

                if (productDetail != null && productDetail.getShopId() != null) {
                    viewModel.loadShopVouchers(productDetail.getShopId());
                }
            } else {
                showLongToast(state.getMessage());
                finish();
            }
        });

        viewModel.getVoucherState().observe(this, this::bindVouchers);

        viewModel.getActionState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                showToast("Đã thêm vào giỏ hàng");
            } else {
                showLongToast(state.getMessage());
            }
        });
    }

    private void bindProductDetail() {
        if (productDetail == null) return;

        setupDefaultVariant();

        txtProductName.setText(productDetail.getName());
        txtDescription.setText(productDetail.getDescription());
        txtShopName.setText(productDetail.getShopName());

        bindPriceAndStock();
        bindStats();
        bindImages();
        bindShop();
        bindReviews(productDetail);

        if (!productDetail.getVouchers().isEmpty()) {
            bindVouchers(productDetail.getVouchers());
        }
    }

    private void setupDefaultVariant() {
        List<ProductDetailResponse.VariantDTO> variants = productDetail.getVariants();

        selectedOptions.clear();

        if (!variants.isEmpty()) {
            selectedVariant = variants.get(0);

            if (selectedVariant.getAttributes() != null) {
                for (Map.Entry<String, String> entry : selectedVariant.getAttributes().entrySet()) {
                    putSelectedOption(entry.getKey(), entry.getValue());
                }
            }
        } else {
            selectedVariant = null;
        }
    }

    private void bindPriceAndStock() {
        BigDecimal price = productDetail.getBasePrice();
        int stock = productDetail.getTotalStock();

        if (selectedVariant != null) {
            price = selectedVariant.getPrice();
            stock = selectedVariant.getStockQuantity();
        }

        txtPrice.setText(formatMoney(price));
        txtStock.setText("Kho: " + stock);
    }

    private void bindStats() {
        double rating = productDetail.getDisplayRating();
        int sold = productDetail.getSoldQuantity();

        txtRating.setText(String.format(Locale.getDefault(), "★ %.1f", rating));
        txtSold.setText("Đã bán " + formatSold(sold));
    }

    private void bindImages() {
        List<String> displayImages = new ArrayList<>();

        if (selectedVariant != null
                && selectedVariant.getImageUrl() != null
                && !selectedVariant.getImageUrl().trim().isEmpty()) {
            displayImages.add(selectedVariant.getImageUrl());
        }

        for (String url : productDetail.getImageUrls()) {
            if (url != null && !url.trim().isEmpty() && !displayImages.contains(url)) {
                displayImages.add(url);
            }
        }

        imageAdapter.setImages(displayImages);
        updateImageIndicator(0);
    }

    private void bindShop() {
        String shopImageUrl = productDetail.getShopImageUrl();

        if (shopImageUrl == null || shopImageUrl.trim().isEmpty()) {
            imgShopAvatar.setImageResource(R.drawable.ic_shop);
        } else {
            ImageLoader.load(this, shopImageUrl, imgShopAvatar);
        }

        if (txtShopStatus != null) {
            txtShopStatus.setText(productDetail.getShopOnlineText());
            txtShopStatus.setTextColor(ContextCompat.getColor(
                    this,
                    productDetail.isShopOnline() ? R.color.success : R.color.text_secondary
            ));
        }
    }
    private void bindVouchers(List<ProductDetailResponse.ShopVoucherDTO> vouchers) {
        layoutVouchers.removeAllViews();

        if (vouchers == null || vouchers.isEmpty()) {
            layoutVoucherCard.setVisibility(View.GONE);
            return;
        }

        layoutVoucherCard.setVisibility(View.VISIBLE);

        int max = Math.min(vouchers.size(), 5);

        for (int i = 0; i < max; i++) {
            addVoucherChip(vouchers.get(i));
        }
    }

    private void addVoucherChip(ProductDetailResponse.ShopVoucherDTO voucher) {
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.VERTICAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setBackgroundResource(R.drawable.bg_voucher_chip);
        chip.setPadding(dp(12), dp(8), dp(12), dp(8));

        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                dp(150),
                dp(64)
        );
        chipParams.rightMargin = dp(10);
        layoutVouchers.addView(chip, chipParams);

        TextView code = createText(
                voucher.getDisplayTitle(),
                14,
                R.color.price_red,
                Typeface.BOLD
        );
        code.setSingleLine(true);
        code.setEllipsize(TextUtils.TruncateAt.END);
        chip.addView(code);

        TextView desc = createText(
                voucher.getDisplaySubtitle(),
                12,
                R.color.text_secondary,
                Typeface.NORMAL
        );
        desc.setSingleLine(true);
        desc.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.topMargin = dp(4);
        chip.addView(desc, descParams);

        chip.setOnClickListener(v ->
                showToast("Mã " + voucher.getCode() + " sẽ áp dụng ở Checkout")
        );
    }

    private void bindReviews(ProductDetailResponse detail) {
        layoutReviews.removeAllViews();

        List<ProductDetailResponse.ReviewDTO> reviews = detail.getReviews();
        int reviewCount = detail.getReviewCount();
        double averageRating = detail.getAverageRating();

        txtReviewSummary.setText("Đánh giá sản phẩm");

        txtReviewCount.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f/5 • %d đánh giá",
                        averageRating,
                        reviewCount
                )
        );

        if (reviews == null || reviews.isEmpty()) {
            TextView empty = createText(
                    "Sản phẩm này chưa có đánh giá nào.",
                    14,
                    R.color.text_secondary,
                    Typeface.NORMAL
            );

            LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            emptyParams.topMargin = dp(10);

            layoutReviews.addView(empty, emptyParams);
            return;
        }

        int limit = Math.min(reviews.size(), 2);

        for (int i = 0; i < limit; i++) {
            addReviewItem(reviews.get(i));
        }
        if (reviews.size() > 2) {
            TextView btnViewAll = createText(
                    "Xem tất cả đánh giá",
                    14,
                    R.color.brand_primary,
                    Typeface.BOLD
            );

            btnViewAll.setGravity(Gravity.CENTER);
            btnViewAll.setPadding(0, dp(12), 0, dp(6));
            btnViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProductReviewsActivity.class);
                intent.putExtra(ProductReviewsActivity.EXTRA_PRODUCT_ID, productDetail.getProductId());
                startActivity(intent);
            });

            layoutReviews.addView(btnViewAll);
        }
    }
    private void addReviewItem(ProductDetailResponse.ReviewDTO review) {
        if (review == null) return;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackgroundResource(R.drawable.bg_review_item);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.topMargin = dp(12);
        layoutReviews.addView(card, cardParams);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        card.addView(
                top,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        TextView name = createText(
                review.getUserName(),
                14,
                R.color.text_primary,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        top.addView(name, nameParams);

        TextView rating = createText(
                buildStars(review.getRating()) + "  " + review.getRating() + "/5",
                13,
                R.color.warning,
                Typeface.BOLD
        );
        top.addView(rating);

        TextView comment = createText(
                review.getComment(),
                14,
                R.color.text_secondary,
                Typeface.NORMAL
        );
        comment.setLineSpacing(dp(3), 1f);

        LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        commentParams.topMargin = dp(8);
        card.addView(comment, commentParams);

        addReviewImages(card, review.getImageUrls());
        addReviewVideo(card, review.getVideoUrl());
        addSellerReply(card, review.getSellerReply());
    }

    private String buildStars(int rating) {
        StringBuilder builder = new StringBuilder();

        int safeRating = Math.max(0, Math.min(5, rating));

        for (int i = 1; i <= 5; i++) {
            builder.append(i <= safeRating ? "★" : "☆");
        }

        return builder.toString();
    }

    private void addReviewImages(
            LinearLayout parent,
            List<String> imageUrls
    ) {
        if (imageUrls == null || imageUrls.isEmpty()) return;

        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        scrollView.setHorizontalScrollBarEnabled(false);

        LinearLayout imageRow = new LinearLayout(this);
        imageRow.setOrientation(LinearLayout.HORIZONTAL);

        scrollView.addView(
                imageRow,
                new HorizontalScrollView.LayoutParams(
                        HorizontalScrollView.LayoutParams.WRAP_CONTENT,
                        HorizontalScrollView.LayoutParams.WRAP_CONTENT
                )
        );

        for (String url : imageUrls) {
            if (url == null || url.trim().isEmpty()) continue;

            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundResource(R.drawable.bg_image_placeholder);

            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    dp(72),
                    dp(72)
            );
            imageParams.rightMargin = dp(8);

            imageRow.addView(imageView, imageParams);

            ImageLoader.load(this, url.trim(), imageView);
        }

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        scrollParams.topMargin = dp(10);

        parent.addView(scrollView, scrollParams);
    }

    private void addReviewVideo(
            LinearLayout parent,
            String videoUrl
    ) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) return;

        VideoView videoView = new VideoView(this);
        videoView.setVideoURI(Uri.parse(videoUrl.trim()));

        MediaController mediaController = new
                MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.setBackgroundResource(R.drawable.bg_image_placeholder);

        LinearLayout.LayoutParams videoParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(210)
        );
        videoParams.topMargin = dp(10);

        parent.addView(videoView, videoParams);

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            videoView.seekTo(100);
        });

        videoView.setOnClickListener(v -> videoView.start());
    }

    private void addSellerReply(
            LinearLayout parent,
            String sellerReply
    ) {
        if (sellerReply == null || sellerReply.trim().isEmpty()) return;

        TextView reply = createText(
                "Phản hồi của shop: " + sellerReply.trim(),
                13,
                R.color.text_secondary,
                Typeface.ITALIC
        );
        reply.setLineSpacing(dp(3), 1f);
        reply.setPadding(dp(10), dp(8), dp(10), dp(8));
        reply.setBackgroundResource(R.drawable.bg_review_reply);

        LinearLayout.LayoutParams replyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        replyParams.topMargin = dp(10);

        parent.addView(reply, replyParams);
    }
    private void showVariantBottomSheet() {
        if (productDetail == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);

        selectedQuantity = 1;

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(24));
        root.setBackgroundColor(ContextCompat.getColor(this, R.color.surface));

        scrollView.addView(root);

        LinearLayout headerContainer = new LinearLayout(this);
        headerContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(headerContainer);

        LinearLayout optionContainer = new LinearLayout(this);
        optionContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(optionContainer);

        LinearLayout quantityContainer = new LinearLayout(this);
        quantityContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(quantityContainer);

        final TextView[] txtSheetPrice = new TextView[1];
        final TextView[] txtSheetStatus = new TextView[1];
        final TextView[] txtSheetSelected = new TextView[1];
        final TextView[] txtQuantityValue = new TextView[1];
        final ImageView[] imgSheetProduct = new ImageView[1];
        final TextView[] btnConfirmSheet = new TextView[1];

        Runnable refreshHeader = () -> {
            ProductDetailResponse.VariantDTO current = findMatchedVariant();

            boolean selectedAll = hasSelectedAllOptionGroups();
            boolean notExists = selectedAll && current == null;
            boolean outOfStock = current != null && current.getStockQuantity() <= 0;
            boolean canSubmit = selectedAll && current != null && !outOfStock;

            BigDecimal displayPrice = current != null
                    ? current.getPrice()
                    : productDetail.getBasePrice();

            if (current != null && current.getStockQuantity() > 0 && selectedQuantity > current.getStockQuantity()) {
                selectedQuantity = current.getStockQuantity();
            }

            if (selectedQuantity < 1) {
                selectedQuantity = 1;
            }

            if (txtSheetPrice[0] != null) {
                txtSheetPrice[0].setText(formatMoney(displayPrice));
            }

            if (txtSheetStatus[0] != null) {
                if (!selectedAll) {
                    String missing = findMissingOptionName();
                    txtSheetStatus[0].setText(
                            missing == null || missing.trim().isEmpty()
                                    ? "Vui lòng chọn đầy đủ phân loại"
                                    : "Vui lòng chọn " + missing
                    );
                    txtSheetStatus[0].setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                } else if (notExists) {
                    txtSheetStatus[0].setText("Tổ hợp phân loại này không tồn tại");
                    txtSheetStatus[0].setTextColor(ContextCompat.getColor(this, R.color.price_red));
                } else if (outOfStock) {
                    txtSheetStatus[0].setText("Phân loại này đã hết hàng");
                    txtSheetStatus[0].setTextColor(ContextCompat.getColor(this, R.color.price_red));
                } else {
                    txtSheetStatus[0].setText("Có thể thêm vào giỏ hàng/mua ngay");
                    txtSheetStatus[0].setTextColor(ContextCompat.getColor(this, R.color.success));
                }
            }

            if (txtSheetSelected[0] != null) {
                if (!selectedAll) {
                    String missing = findMissingOptionName();
                    txtSheetSelected[0].setText(
                            missing == null || missing.trim().isEmpty()
                                    ? "Chọn đầy đủ phân loại"
                                    : "Chọn thêm: " + missing
                    );
                } else if (notExists) {
                    txtSheetSelected[0].setText("Không có biến thể phù hợp");
                } else if (outOfStock) {
                    txtSheetSelected[0].setText("Biến thể đã hết hàng");
                } else {
                    txtSheetSelected[0].setText("Đã chọn: " + current.getAttributeText());
                }
            }

            if (txtQuantityValue[0] != null) {
                txtQuantityValue[0].setText(String.valueOf(selectedQuantity));
            }

            String imageUrl = null;

            if (current != null
                    && current.getImageUrl() != null
                    && !current.getImageUrl().trim().isEmpty()) {
                imageUrl = current.getImageUrl();
            } else if (!productDetail.getImageUrls().isEmpty()) {
                imageUrl = productDetail.getImageUrls().get(0);
            }

            if (imgSheetProduct[0] != null) {
                ImageLoader.load(this, imageUrl, imgSheetProduct[0]);
            }

            if (btnConfirmSheet[0] != null) {
                btnConfirmSheet[0].setEnabled(canSubmit);
                btnConfirmSheet[0].setAlpha(canSubmit ? 1f : 0.45f);

                if (!selectedAll) {
                    btnConfirmSheet[0].setText("Chọn phân loại");
                } else if (notExists) {
                    btnConfirmSheet[0].setText("Không có phân loại này");
                } else if (outOfStock) {
                    btnConfirmSheet[0].setText("Hết hàng");
                } else {
                    btnConfirmSheet[0].setText(
                            pendingAction == ACTION_ADD_TO_CART
                                    ? "Thêm vào giỏ hàng"
                                    : "Mua ngay"
                    );
                }
            }
        };

        Runnable[] refreshOptions = new Runnable[1];

        refreshOptions[0] = () -> {
            optionContainer.removeAllViews();

            List<OptionGroupUi> groups = getOptionGroupsForUi();

            if (groups.isEmpty()) {
                TextView defaultText = createText(
                        "Sản phẩm có phân loại mặc định",
                        15,
                        R.color.text_secondary,
                        Typeface.NORMAL
                );

                LinearLayout.LayoutParams defaultParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                defaultParams.topMargin = dp(18);
                optionContainer.addView(defaultText, defaultParams);
                return;
            }

            for (OptionGroupUi group : groups) {
                addBottomSheetOptionGroup(
                        optionContainer,
                        group,
                        refreshHeader,
                        refreshOptions[0]
                );
            }
        };

        addBottomSheetHeader(
                headerContainer,
                imgSheetProduct,
                txtSheetPrice,
                txtSheetStatus,
                txtSheetSelected
        );

        refreshOptions[0].run();

        addQuantitySelector(
                quantityContainer,
                txtQuantityValue,
                refreshHeader
        );

        TextView confirm = new TextView(this);
        confirm.setGravity(Gravity.CENTER);
        confirm.setText(pendingAction == ACTION_ADD_TO_CART ? "Thêm vào giỏ hàng" : "Mua ngay");
        confirm.setTextColor(ContextCompat.getColor(this, R.color.white));
        confirm.setTextSize(17);
        confirm.setTypeface(null, Typeface.BOLD);
        confirm.setBackgroundResource(R.drawable.bg_buy_now_gradient);
        btnConfirmSheet[0] = confirm;

        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
        );
        confirmParams.topMargin = dp(26);
        root.addView(confirm, confirmParams);

        confirm.setOnClickListener(v -> {
            ProductDetailResponse.VariantDTO chosen = findMatchedVariant();

            if (!hasSelectedAllOptionGroups()) {
                String missing = findMissingOptionName();

                showToast(
                        missing == null || missing.trim().isEmpty()
                                ? "Vui lòng chọn đầy đủ phân loại sản phẩm"
                                : "Vui lòng chọn " + missing
                );
                return;
            }

            if (chosen == null || chosen.getVariantId() == null) {
                showToast("Tổ hợp phân loại này không tồn tại");
                return;
            }

            if (chosen.getStockQuantity() <= 0) {
                showToast("Phân loại này đã hết hàng");
                return;
            }

            if (selectedQuantity <= 0) {
                selectedQuantity = 1;
            }

            if (selectedQuantity > chosen.getStockQuantity()) {
                showToast("Số lượng vượt quá tồn kho");
                return;
            }

            selectedVariant = chosen;
            bindPriceAndStock();
            bindImages();
            dialog.dismiss();

            if (pendingAction == ACTION_ADD_TO_CART) {
                viewModel.addToCart(selectedVariant.getVariantId(), selectedQuantity);
            } else {
                openCheckout(selectedVariant.getVariantId(), selectedQuantity);
            }
        });

        refreshHeader.run();

        dialog.setContentView(scrollView);
        dialog.show();
    }

    private void addBottomSheetHeader(
            LinearLayout root,
            ImageView[] imgSheetProduct,
            TextView[] txtSheetPrice,
            TextView[] txtSheetStatus,
            TextView[] txtSheetSelected
    ) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        root.addView(header);

        ImageView image = new ImageView(this);
        image.setBackgroundResource(R.drawable.bg_product_detail_card);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setPadding(dp(4), dp(4), dp(4), dp(4));

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(92), dp(92));
        header.addView(image, imageParams);

        imgSheetProduct[0] = image;

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        infoParams.leftMargin = dp(14);
        header.addView(info, infoParams);

        TextView price = createText(
                "0đ",
                21,
                R.color.price_red,
                Typeface.BOLD
        );
        info.addView(price);
        txtSheetPrice[0] = price;

        TextView status = createText(
                "Chọn phân loại",
                13,
                R.color.text_secondary,
                Typeface.NORMAL
        );

        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        statusParams.topMargin = dp(6);
        info.addView(status, statusParams);
        txtSheetStatus[0] = status;

        TextView selected = createText(
                "Chọn phân loại",
                13,
                R.color.text_secondary,
                Typeface.NORMAL
        );
        selected.setMaxLines(2);
        selected.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams selectedParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        selectedParams.topMargin = dp(6);
        info.addView(selected, selectedParams);
        txtSheetSelected[0] = selected;
    }

    private void addBottomSheetOptionGroup(
            LinearLayout root,
            OptionGroupUi group,
            Runnable refreshHeader,
            Runnable refreshOptions
    ) {
        TextView title = createText(
                group.name,
                15,
                R.color.text_primary,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(22);
        root.addView(title, titleParams);

        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        for (String value : group.values) {
            if (value == null || value.trim().isEmpty()) continue;

            TextView chip = createOptionChip(
                    group.name,
                    value.trim(),
                    refreshHeader,
                    refreshOptions
            );
            row.addView(chip);
        }

        scrollView.addView(row);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.topMargin = dp(10);
        root.addView(scrollView, rowParams);
    }

    private TextView createOptionChip(
            String optionName,
            String value,
            Runnable refreshHeader,
            Runnable refreshOptions
    ) {
        TextView chip = new TextView(this);

        boolean selected = sameText(value, getSelectedOptionValue(optionName));

        chip.setText(value);
        chip.setSingleLine(true);
        chip.setEllipsize(TextUtils.TruncateAt.END);
        chip.setGravity(Gravity.CENTER);
        chip.setTextSize(14);
        chip.setMinWidth(dp(78));
        chip.setPadding(dp(14), 0, dp(14), 0);

        chip.setBackgroundResource(selected
                ? R.drawable.bg_chip_selected
                : R.drawable.bg_chip_unselected
        );

        chip.setTextColor(ContextCompat.getColor(
                this,
                selected ? R.color.brand_primary : R.color.text_primary
        ));

        chip.setAlpha(1f);
        chip.setEnabled(true);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(42)
        );
        params.setMargins(0, 0, dp(10), 0);
        chip.setLayoutParams(params);

        chip.setOnClickListener(v -> {
            putSelectedOption(optionName, value);

            selectedVariant = findMatchedVariant();
            selectedQuantity = 1;

            refreshOptions.run();
            refreshHeader.run();
        });

        return chip;
    }

    private void addQuantitySelector(
            LinearLayout root,
            TextView[] txtQuantityValue,
            Runnable refreshHeader
    ) {
        LinearLayout quantityRow = new LinearLayout(this);
        quantityRow.setOrientation(LinearLayout.HORIZONTAL);
        quantityRow.setGravity(Gravity.CENTER_VERTICAL);
        quantityRow.setPadding(0, dp(18), 0, 0);

        TextView label = createText(
                "Số lượng",
                15,
                R.color.text_primary,
                Typeface.BOLD
        );

        quantityRow.addView(label);

        View spacer = new View(this);
        quantityRow.addView(spacer, new LinearLayout.LayoutParams(
                0,
                dp(1),
                1
        ));

        LinearLayout quantityBox = new LinearLayout(this);
        quantityBox.setOrientation(LinearLayout.HORIZONTAL);
        quantityBox.setGravity(Gravity.CENTER);
        quantityBox.setBackgroundResource(R.drawable.bg_quantity_box);

        TextView minus = createQuantityButton("−", R.color.text_primary);
        TextView quantityText = createQuantityButton(String.valueOf(selectedQuantity), R.color.text_primary);
        TextView plus = createQuantityButton("+", R.color.brand_primary);

        txtQuantityValue[0] = quantityText;

        quantityBox.addView(minus, new LinearLayout.LayoutParams(dp(42), dp(42)));
        quantityBox.addView(quantityText, new LinearLayout.LayoutParams(dp(50), dp(42)));
        quantityBox.addView(plus, new LinearLayout.LayoutParams(dp(42), dp(42)));

        quantityRow.addView(quantityBox);

        root.addView(quantityRow, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        minus.setOnClickListener(v -> {
            if (selectedQuantity <= 1) return;

            selectedQuantity--;
            quantityText.setText(String.valueOf(selectedQuantity));
            refreshHeader.run();
        });

        plus.setOnClickListener(v -> {
            ProductDetailResponse.VariantDTO matched = findMatchedVariant();

            if (!hasSelectedAllOptionGroups()) {
                String missing = findMissingOptionName();

                showToast(
                        missing == null || missing.trim().isEmpty()
                                ? "Vui lòng chọn đầy đủ phân loại"
                                : "Vui lòng chọn " + missing
                );
                return;
            }

            if (matched == null) {
                showToast("Tổ hợp phân loại này không tồn tại");
                return;
            }

            int stock = matched.getStockQuantity();

            if (stock <= 0) {
                showToast("Phân loại này đã hết hàng");
                return;
            }

            if (selectedQuantity >= stock) {
                showToast("Số lượng đã đạt giới hạn tồn kho");
                return;
            }

            selectedQuantity++;
            quantityText.setText(String.valueOf(selectedQuantity));
            refreshHeader.run();
        });
    }

    private TextView createQuantityButton(String text, int colorRes) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(18);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(ContextCompat.getColor(this, colorRes));
        return textView;
    }

    private ProductDetailResponse.VariantDTO findMatchedVariant() {
        if (productDetail == null) return null;

        List<ProductDetailResponse.VariantDTO> variants = productDetail.getVariants();

        if (variants == null || variants.isEmpty()) {
            return null;
        }

        List<OptionGroupUi> groups = getOptionGroupsForUi();

        if (groups.isEmpty()) {
            return variants.get(0);
        }

        if (!hasSelectedAllOptionGroups()) {
            return null;
        }

        for (ProductDetailResponse.VariantDTO variant : variants) {
            if (variant == null) continue;

            Map<String, String> attrs = variant.getAttributes();

            if (attrs == null || attrs.isEmpty()) continue;

            boolean matched = true;

            for (OptionGroupUi group : groups) {
                String selectedValue = getSelectedOptionValue(group.name);
                String realValue = findAttributeValue(attrs, group.name);

                if (!sameText(realValue, selectedValue)) {
                    matched = false;
                    break;
                }
            }

            if (matched) {
                return variant;
            }
        }

        return null;
    }

    private List<OptionGroupUi> getOptionGroupsForUi() {
        List<OptionGroupUi> result = new ArrayList<>();

        if (productDetail == null) return result;

        List<ProductDetailResponse.OptionGroupDTO> groups = productDetail.getOptionGroups();

        if (groups != null) {
            for (ProductDetailResponse.OptionGroupDTO group : groups) {
                if (group == null) continue;

                String name = group.getName();

                if (name == null || name.trim().isEmpty()) continue;

                List<String> cleanValues = cleanStringList(group.getValues());

                if (cleanValues.isEmpty()) continue;

                result.add(new OptionGroupUi(name.trim(), cleanValues));
            }
        }

        if (!result.isEmpty()) return result;

        Map<String, List<String>> derived = new LinkedHashMap<>();

        for (ProductDetailResponse.VariantDTO variant : productDetail.getVariants()) {
            if (variant == null || variant.getAttributes() == null) continue;

            for (Map.Entry<String, String> entry : variant.getAttributes().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key == null || key.trim().isEmpty()) continue;
                if (value == null || value.trim().isEmpty()) continue;

                String safeKey = key.trim();
                String safeValue = value.trim();

                if (!derived.containsKey(safeKey)) {
                    derived.put(safeKey, new ArrayList<>());
                }

                List<String> values = derived.get(safeKey);

                if (!containsSameText(values, safeValue)) {
                    values.add(safeValue);
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : derived.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                result.add(new OptionGroupUi(entry.getKey(), entry.getValue()));
            }
        }

        return result;
    }

    private boolean hasSelectedAllOptionGroups() {
        List<OptionGroupUi> groups = getOptionGroupsForUi();

        if (groups.isEmpty()) return true;

        for (OptionGroupUi group : groups) {
            String selectedValue = getSelectedOptionValue(group.name);

            if (selectedValue == null || selectedValue.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private String findMissingOptionName() {
        List<OptionGroupUi> groups = getOptionGroupsForUi();

        for (OptionGroupUi group : groups) {
            String selectedValue = getSelectedOptionValue(group.name);

            if (selectedValue == null || selectedValue.trim().isEmpty()) {
                return group.name;
            }
        }

        return null;
    }

    private String getSelectedOptionValue(String optionName) {
        if (optionName == null) return null;

        for (Map.Entry<String, String> entry : selectedOptions.entrySet()) {
            if (sameText(entry.getKey(), optionName)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private void putSelectedOption(String optionName, String value) {
        if (optionName == null || value == null) return;

        String oldKey = null;

        for (String key : selectedOptions.keySet()) {
            if (sameText(key, optionName)) {
                oldKey = key;
                break;
            }
        }

        if (oldKey != null) {
            selectedOptions.remove(oldKey);
        }

        selectedOptions.put(optionName, value);
    }

    private String findAttributeValue(Map<String, String> attrs, String key) {
        if (attrs == null || key == null) return null;

        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            if (sameText(entry.getKey(), key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private List<String> cleanStringList(List<String> input) {
        List<String> result = new ArrayList<>();

        if (input == null) return result;

        for (String value : input) {
            if (value == null || value.trim().isEmpty()) continue;

            String safe = value.trim();

            if (!containsSameText(result, safe)) {
                result.add(safe);
            }
        }

        return result;
    }

    private boolean containsSameText(List<String> values, String target) {
        if (values == null || target == null) return false;

        for (String value : values) {
            if (sameText(value, target)) return true;
        }

        return false;
    }

    private boolean sameText(String a, String b) {
        return normalizeText(a).equals(normalizeText(b));
    }

    private String normalizeText(String value) {
        if (value == null) return "";

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replace("Đ", "D").replace("đ", "d");

        return normalized
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "")
                .replace("/", "")
                .replace("\\", "");
    }

    private void openCart() {
        try {
            Class<?> cartClass = Class.forName(
                    "com.gr6.smartcart_android.buyer.cart.CartActivity"
            );
            startActivity(new Intent(this, cartClass));
        } catch (Exception e) {
            showToast("Giỏ hàng sẽ làm ở bước sau");
        }
    }

    private void openShop() {
        if (productDetail == null || productDetail.getShopId() == null) {
            showToast("Không tìm thấy shop");
            return;
        }

        try {
            Class<?> shopClass = Class.forName(
                    "com.gr6.smartcart_android.buyer.shop.BuyerShopActivity"
            );

            Intent intent = new Intent(this, shopClass);
            intent.putExtra("shop_id", productDetail.getShopId());
            startActivity(intent);
        } catch (Exception e) {
            showToast("Màn hình shop sẽ làm ở bước sau");
        }
    }

    private void openCheckout(Long variantId, int quantity) {
        try {
            Intent intent = new Intent(
                    this,
                    com.gr6.smartcart_android.buyer.checkout.CheckoutActivity.class
            );

            intent.putExtra("checkout_source", com.gr6.smartcart_android.buyer.checkout.CheckoutActivity.SOURCE_BUY_NOW);
            intent.putExtra("product_id", productDetail.getProductId());
            intent.putExtra("variant_id", variantId);
            intent.putExtra("quantity", quantity);
            intent.putExtra("shop_id", productDetail.getShopId());

            startActivity(intent);
        } catch (Exception e) {
            showToast("Không mở được màn hình thanh toán");
        }
    }
    private void updateImageIndicator(int position) {
        int count = imageAdapter.getImageCount();

        if (count <= 0) {
            txtImageIndicator.setText("0/0");
            return;
        }

        txtImageIndicator.setText((position + 1) + "/" + count);
    }

    private TextView createText(
            String text,
            int sp,
            int colorRes,
            int typefaceStyle
    ) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(sp);
        textView.setTextColor(ContextCompat.getColor(this, colorRes));
        textView.setTypeface(null, typefaceStyle);
        return textView;
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0đ";
        return moneyFormat.format(value);
    }

    private String formatSold(int value) {
        if (value <= 0) return "0";

        if (value >= 1000) {
            double k = value / 1000.0;
            return String.format(Locale.getDefault(), "%.1fk", k);
        }

        return String.valueOf(value);
    }

    public int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class OptionGroupUi {
        private final String name;
        private final List<String> values;

        private OptionGroupUi(String name, List<String> values) {
            this.name = name;
            this.values = values;
        }
    }
}