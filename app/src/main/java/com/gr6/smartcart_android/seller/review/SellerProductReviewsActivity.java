package com.gr6.smartcart_android.seller.review;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.seller.review.api.SellerReviewApiService;
import com.gr6.smartcart_android.seller.review.response.SellerReviewResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerProductReviewsActivity extends BaseActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    public static final String EXTRA_PRODUCT_NAME = "extra_product_name";

    private long productId;
    private String productName;

    private TextView btnBack;
    private TextView txtTitle;
    private TextView txtSummary;
    private ProgressBar progressBar;
    private LinearLayout reviewContainer;

    private SellerReviewApiService reviewApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_product_reviews);

        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1L);
        productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);

        if (productId <= 0) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        reviewApiService = ApiClient.createService(this, SellerReviewApiService.class);

        initViews();
        loadReviews();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        txtSummary = findViewById(R.id.txtSummary);
        progressBar = findViewById(R.id.progressBar);
        reviewContainer = findViewById(R.id.reviewContainer);

        btnBack.setOnClickListener(v -> finish());

        if (productName == null || productName.trim().isEmpty()) {
            txtTitle.setText("Đánh giá sản phẩm");
        } else {
            txtTitle.setText(productName.trim());
        }
    }

    private void loadReviews() {
        showLoading(true);

        reviewApiService.getShopReviews().enqueue(new Callback<BaseResponse<List<SellerReviewResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<SellerReviewResponse>>> call,
                    @NonNull Response<BaseResponse<List<SellerReviewResponse>>> response
            ) {
                showLoading(false);

                BaseResponse<List<SellerReviewResponse>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    Toast.makeText(
                            SellerProductReviewsActivity.this,
                            body == null ? "Không tải được đánh giá" : body.getSafeMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                    renderReviews(new ArrayList<>());
                    return;
                }

                renderReviews(filterProductReviews(body.getData()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<SellerReviewResponse>>> call,
                    @NonNull Throwable t
            ) {
                showLoading(false);
                Toast.makeText(
                        SellerProductReviewsActivity.this,
                        "Không kết nối được server: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                renderReviews(new ArrayList<>());
            }
        });
    }

    private List<SellerReviewResponse> filterProductReviews(List<SellerReviewResponse> allReviews) {
        List<SellerReviewResponse> result = new ArrayList<>();
        if (allReviews == null) {
            return result;
        }

        for (SellerReviewResponse review : allReviews) {
            if (review == null || review.getProductId() == null) {
                continue;
            }

            if (review.getProductId() == productId) {
                result.add(review);
            }
        }

        return result;
    }

    private void renderReviews(List<SellerReviewResponse> reviews) {
        reviewContainer.removeAllViews();

        if (reviews == null || reviews.isEmpty()) {
            txtSummary.setText("Chưa có đánh giá nào");

            TextView empty = new TextView(this);
            empty.setText("Sản phẩm này chưa có đánh giá từ khách hàng.");
            empty.setTextColor(color(R.color.text_secondary));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(16), dp(28), dp(16), dp(28));
            empty.setBackgroundResource(R.drawable.bg_seller_card);

            reviewContainer.addView(empty, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            return;
        }

        double average = 0.0;
        for (SellerReviewResponse review : reviews) {
            if (review != null) {
                average += review.getRating();
            }
        }
        average = average / reviews.size();

        txtSummary.setText(String.format(
                Locale.getDefault(),
                "%.1f ★ • %d đánh giá",
                average,
                reviews.size()
        ));

        for (SellerReviewResponse review : reviews) {
            if (review != null) {
                reviewContainer.addView(createReviewRow(review));
            }
        }
    }

    private View createReviewRow(SellerReviewResponse review) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackgroundResource(R.drawable.bg_seller_card);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(header);

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundResource(R.drawable.bg_image_placeholder);
        header.addView(image, new LinearLayout.LayoutParams(dp(44), dp(44)));
        ImageLoader.load(this, review.getProductImageUrl(), image);

        LinearLayout buyerBox = new LinearLayout(this);
        buyerBox.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams buyerParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        buyerParams.setMargins(dp(10), 0, dp(8), 0);
        header.addView(buyerBox, buyerParams);

        TextView buyerName = makeText(safeText(review.getBuyerName(), "Khách hàng"), 14, R.color.text_primary, true);
        buyerBox.addView(buyerName);

        TextView date = makeText(formatDate(review.getCreatedAt()), 12, R.color.text_secondary, false);
        buyerBox.addView(date);

        TextView rating = makeText(buildStarText(review.getRating()), 13, R.color.warning, true);
        rating.setGravity(Gravity.CENTER);
        rating.setBackgroundResource(R.drawable.bg_seller_chip_soft);
        rating.setPadding(dp(8), dp(4), dp(8), dp(4));
        header.addView(rating);

        TextView comment = makeText(safeText(review.getComment(), "Khách hàng không để lại bình luận."), 14, R.color.text_primary, false);
        comment.setLineSpacing(dp(3), 1f);

        LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        commentParams.setMargins(0, dp(10), 0, 0);
        card.addView(comment, commentParams);

        if (review.getSellerReply() != null && !review.getSellerReply().trim().isEmpty()) {
            TextView reply = makeText(
                    "Phản hồi của shop: " + review.getSellerReply().trim(),
                    13,
                    R.color.brand_primary,
                    false
            );
            reply.setPadding(dp(10), dp(8), dp(10), dp(8));
            reply.setBackgroundResource(R.drawable.bg_seller_button_blue_soft);

            LinearLayout.LayoutParams replyParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            replyParams.setMargins(0, dp(10), 0, 0);
            card.addView(reply, replyParams);
        }

        return card;
    }

    private TextView makeText(String text, int sp, int colorId, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text == null ? "" : text);
        view.setTextSize(sp);
        view.setTextColor(color(colorId));

        if (bold) {
            view.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        return view;
    }

    private String buildStarText(int rating) {
        int safeRating = Math.max(0, Math.min(5, rating));
        if (safeRating == 0) {
            return "Chưa chấm sao";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < safeRating; i++) {
            builder.append("★");
        }

        return builder.toString();
    }

    private String formatDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "Không rõ thời gian";
        }

        String value = raw.trim();
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };

        for (String pattern : patterns) {
            try {
                Date date = new SimpleDateFormat(pattern, Locale.US).parse(value);
                if (date != null) {
                    return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
                }
            } catch (ParseException ignored) {
            }
        }

        return value;
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private int color(int colorId) {
        return ContextCompat.getColor(this, colorId);
    }
}