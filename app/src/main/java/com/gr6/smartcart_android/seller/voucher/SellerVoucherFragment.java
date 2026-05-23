package com.gr6.smartcart_android.seller.voucher;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.seller.utils.ApiErrorUtils;
import com.gr6.smartcart_android.seller.voucher.api.SellerVoucherApiService;
import com.gr6.smartcart_android.seller.voucher.response.VoucherResponse;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerVoucherFragment extends Fragment {

    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_ONGOING = "ONGOING";
    private static final String FILTER_UPCOMING = "UPCOMING";
    private static final String FILTER_ENDED = "ENDED";

    private static final int COLOR_PRIMARY = Color.rgb(37, 99, 235);          // #2563EB
    private static final int COLOR_PRIMARY_DARK = Color.rgb(29, 78, 216);     // #1D4ED8
    private static final int COLOR_PRIMARY_LIGHT = Color.rgb(219, 234, 254);  // #DBEAFE
    private static final int COLOR_TEXT_PRIMARY = Color.rgb(15, 23, 42);      // #0F172A
    private static final int COLOR_TEXT_SECONDARY = Color.rgb(100, 116, 139); // #64748B
    private static final int COLOR_BORDER = Color.rgb(226, 232, 240);         // #E2E8F0

    private LinearLayout voucherContainer;
    private LinearLayout emptyLayout;
    private TextView txtTotalVoucher;
    private TextView txtTotalUsed;
    private TextView tabAll;
    private TextView tabOngoing;
    private TextView tabUpcoming;
    private TextView tabEnded;
    private ProgressBar progressBar;

    private SellerVoucherApiService apiService;
    private Call<BaseResponse<List<VoucherResponse>>> listCall;
    private final List<VoucherResponse> allVouchers = new ArrayList<>();
    private String currentFilter = FILTER_ALL;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_seller_vouchers, container, false);

        apiService = ApiClient.createService(requireContext(), SellerVoucherApiService.class);

        txtTotalVoucher = view.findViewById(R.id.txtTotalVoucher);
        txtTotalUsed = view.findViewById(R.id.txtTotalUsed);
        voucherContainer = view.findViewById(R.id.voucherContainer);
        emptyLayout = view.findViewById(R.id.emptyLayout);
        progressBar = view.findViewById(R.id.progressBar);

        tabAll = view.findViewById(R.id.tabAll);
        tabOngoing = view.findViewById(R.id.tabOngoing);
        tabUpcoming = view.findViewById(R.id.tabUpcoming);
        tabEnded = view.findViewById(R.id.tabEnded);

        Button btnCreateVoucher = view.findViewById(R.id.btnCreateVoucher);
        btnCreateVoucher.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateVoucherActivity.class);
            startActivity(intent);
        });

        tabAll.setOnClickListener(v -> applyFilter(FILTER_ALL));
        tabOngoing.setOnClickListener(v -> applyFilter(FILTER_ONGOING));
        tabUpcoming.setOnClickListener(v -> applyFilter(FILTER_UPCOMING));
        tabEnded.setOnClickListener(v -> applyFilter(FILTER_ENDED));

        applySelectedTabStyle();
        loadVouchers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVouchers();
    }

    @Override
    public void onDestroyView() {
        if (listCall != null) {
            listCall.cancel();
        }
        super.onDestroyView();
    }

    private void loadVouchers() {
        if (!isAdded()) return;

        progressBar.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);

        if (listCall != null) {
            listCall.cancel();
        }

        listCall = apiService.getMyVouchers();
        listCall.enqueue(new Callback<BaseResponse<List<VoucherResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<VoucherResponse>>> call,
                    @NonNull Response<BaseResponse<List<VoucherResponse>>> response
            ) {
                if (!isAdded()) return;

                progressBar.setVisibility(View.GONE);

                if (!response.isSuccessful()) {
                    showToast("Không tải được voucher: " + ApiErrorUtils.extractErrorMessage(response));
                    renderEmpty();
                    return;
                }

                BaseResponse<List<VoucherResponse>> body = response.body();
                if (body == null) {
                    showToast("Server không trả dữ liệu voucher");
                    renderEmpty();
                    return;
                }

                if (!body.isSuccess()) {
                    showToast(body.getSafeMessage());
                    renderEmpty();
                    return;
                }

                allVouchers.clear();
                if (body.getData() != null) {
                    allVouchers.addAll(body.getData());
                }

                renderStats();
                renderList();
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<VoucherResponse>>> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded() || call.isCanceled()) return;
                progressBar.setVisibility(View.GONE);
                showToast("Lỗi kết nối voucher: " + t.getMessage());
                renderEmpty();
            }
        });
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        applySelectedTabStyle();
        renderList();
    }

    private void applySelectedTabStyle() {
        styleTab(tabAll, FILTER_ALL.equals(currentFilter));
        styleTab(tabOngoing, FILTER_ONGOING.equals(currentFilter));
        styleTab(tabUpcoming, FILTER_UPCOMING.equals(currentFilter));
        styleTab(tabEnded, FILTER_ENDED.equals(currentFilter));
    }

    private void styleTab(TextView tab, boolean selected) {
        tab.setTextColor(selected ? COLOR_PRIMARY : COLOR_TEXT_PRIMARY);
        tab.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
        tab.setBackgroundColor(Color.TRANSPARENT);
    }

    private void renderStats() {
        txtTotalVoucher.setText(String.valueOf(allVouchers.size()));

        int used = 0;
        for (VoucherResponse voucher : allVouchers) {
            used += voucher.safeUsedCount();
        }
        txtTotalUsed.setText(String.valueOf(used));
    }

    private void renderList() {
        voucherContainer.removeAllViews();

        List<VoucherResponse> filtered = new ArrayList<>();
        for (VoucherResponse voucher : allVouchers) {
            if (matchFilter(voucher)) {
                filtered.add(voucher);
            }
        }

        if (filtered.isEmpty()) {
            renderEmpty();
            return;
        }

        emptyLayout.setVisibility(View.GONE);

        for (VoucherResponse voucher : filtered) {
            voucherContainer.addView(createVoucherCard(voucher));
            voucherContainer.addView(createSpace(14));
        }
    }

    private boolean matchFilter(VoucherResponse voucher) {
        if (FILTER_ALL.equals(currentFilter)) return true;

        VoucherDisplayStatus status = getDisplayStatus(voucher);

        if (FILTER_ONGOING.equals(currentFilter)) return status == VoucherDisplayStatus.ONGOING;
        if (FILTER_UPCOMING.equals(currentFilter)) return status == VoucherDisplayStatus.UPCOMING;
        if (FILTER_ENDED.equals(currentFilter)) return status == VoucherDisplayStatus.ENDED;

        return true;
    }

    private View createVoucherCard(VoucherResponse voucher) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(0, 0, dp(12), 0);
        root.setBackground(cardBackground(Color.WHITE, 18, COLOR_BORDER));
        root.setElevation(dp(3));

        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(118)
        );
        rootParams.setMargins(dp(16), 0, dp(16), 0);
        root.setLayoutParams(rootParams);

        LinearLayout left = new LinearLayout(requireContext());
        left.setOrientation(LinearLayout.VERTICAL);
        left.setGravity(Gravity.CENTER);
        left.setPadding(dp(8), dp(8), dp(8), dp(8));
        left.setBackground(leftVoucherBackground(COLOR_PRIMARY));
        root.addView(left, new LinearLayout.LayoutParams(dp(140), ViewGroup.LayoutParams.MATCH_PARENT));

        TextView leftIcon = new TextView(requireContext());
        leftIcon.setText(isPercent(voucher) ? "%" : "🎟");
        leftIcon.setTextColor(Color.WHITE);
        leftIcon.setTextSize(isPercent(voucher) ? 42 : 34);
        leftIcon.setGravity(Gravity.CENTER);
        leftIcon.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        left.addView(leftIcon);

        TextView leftLabel = new TextView(requireContext());
        leftLabel.setText("SMARTCART");
        leftLabel.setTextColor(Color.WHITE);
        leftLabel.setTextSize(15);
        leftLabel.setGravity(Gravity.CENTER);
        leftLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        left.addView(leftLabel);

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(dp(18), dp(8), 0, dp(8));
        root.addView(content, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        LinearLayout rowTitle = new LinearLayout(requireContext());
        rowTitle.setOrientation(LinearLayout.HORIZONTAL);
        rowTitle.setGravity(Gravity.CENTER_VERTICAL);
        content.addView(rowTitle, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView txtCode = new TextView(requireContext());
        txtCode.setText(emptyToDefault(voucher.getCode(), "VOUCHER"));
        txtCode.setTextColor(Color.BLACK);
        txtCode.setTextSize(20);
        txtCode.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        txtCode.setSingleLine(true);
        rowTitle.addView(txtCode, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView txtStatus = new TextView(requireContext());
        txtStatus.setText(getStatusLabel(voucher));
        txtStatus.setTextSize(12);
        txtStatus.setGravity(Gravity.CENTER);
        txtStatus.setPadding(dp(12), dp(5), dp(12), dp(5));
        VoucherDisplayStatus displayStatus = getDisplayStatus(voucher);
        txtStatus.setTextColor(displayStatus == VoucherDisplayStatus.ONGOING ? COLOR_PRIMARY_DARK : COLOR_TEXT_SECONDARY);
        txtStatus.setBackground(cardBackground(
                displayStatus == VoucherDisplayStatus.ONGOING ? COLOR_PRIMARY_LIGHT : Color.rgb(241, 245, 249),
                28,
                Color.TRANSPARENT
        ));
        rowTitle.addView(txtStatus);

        TextView txtDesc = new TextView(requireContext());
        txtDesc.setText(getVoucherDescription(voucher));
        txtDesc.setTextColor(COLOR_TEXT_PRIMARY);
        txtDesc.setTextSize(15);
        txtDesc.setMaxLines(2);
        content.addView(txtDesc);

        ProgressBar usageProgress = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        usageProgress.setMax(voucher.safeUsageLimit());
        usageProgress.setProgress(Math.min(voucher.safeUsedCount(), voucher.safeUsageLimit()));
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(8)
        );
        progressParams.setMargins(0, dp(8), 0, dp(4));
        content.addView(usageProgress, progressParams);

        LinearLayout bottomRow = new LinearLayout(requireContext());
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        content.addView(bottomRow);

        TextView txtUsed = new TextView(requireContext());
        txtUsed.setText("Đã dùng: " + voucher.safeUsedCount() + "/" + voucher.safeUsageLimit());
        txtUsed.setTextColor(COLOR_TEXT_SECONDARY);
        txtUsed.setTextSize(14);
        bottomRow.addView(txtUsed, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView btnMore = new TextView(requireContext());
        btnMore.setText("⋮");
        btnMore.setTextSize(26);
        btnMore.setGravity(Gravity.CENTER);
        btnMore.setTextColor(COLOR_TEXT_SECONDARY);
        bottomRow.addView(btnMore, new LinearLayout.LayoutParams(dp(42), dp(42)));

        root.setOnClickListener(v -> openEditVoucher(voucher));
        btnMore.setOnClickListener(v -> showVoucherActions(voucher));

        return root;
    }

    private void showVoucherActions(VoucherResponse voucher) {
        String[] actions = {"Sửa voucher", "Ngừng voucher"};
        new AlertDialog.Builder(requireContext())
                .setTitle(emptyToDefault(voucher.getCode(), "Voucher"))
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        openEditVoucher(voucher);
                    } else {
                        confirmDeactivate(voucher);
                    }
                })
                .show();
    }

    private void confirmDeactivate(VoucherResponse voucher) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Ngừng voucher")
                .setMessage("Bạn có chắc muốn ngừng voucher " + emptyToDefault(voucher.getCode(), "") + " không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Ngừng", (dialog, which) -> deactivateVoucher(voucher))
                .show();
    }

    private void deactivateVoucher(VoucherResponse voucher) {
        if (voucher.getVoucherId() == null) {
            showToast("Thiếu mã voucher");
            return;
        }

        apiService.deactivateVoucher(voucher.getVoucherId()).enqueue(new Callback<BaseResponse<Object>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<Object>> call,
                    @NonNull Response<BaseResponse<Object>> response
            ) {
                if (!isAdded()) return;

                if (!response.isSuccessful()) {
                    showToast("Ngừng voucher thất bại. Mã lỗi: " + response.code());
                    return;
                }

                BaseResponse<Object> body = response.body();
                if (body == null || !body.isSuccess()) {
                    showToast(body == null ? "Server không trả dữ liệu" : body.getSafeMessage());
                    return;
                }

                showToast("Đã ngừng voucher");
                loadVouchers();
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<Object>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showToast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void openEditVoucher(VoucherResponse voucher) {
        Intent intent = new Intent(requireContext(), CreateVoucherActivity.class);
        intent.putExtra(CreateVoucherActivity.EXTRA_VOUCHER_ID, voucher.getVoucherId());
        intent.putExtra(CreateVoucherActivity.EXTRA_CODE, voucher.getCode());
        intent.putExtra(CreateVoucherActivity.EXTRA_DISCOUNT_TYPE, voucher.getDiscountType());
        intent.putExtra(CreateVoucherActivity.EXTRA_DISCOUNT_VALUE, voucher.safeDiscountValue());
        intent.putExtra(CreateVoucherActivity.EXTRA_MIN_ORDER_VALUE, voucher.safeMinOrderValue());
        intent.putExtra(CreateVoucherActivity.EXTRA_MAX_DISCOUNT_AMOUNT, voucher.safeMaxDiscountAmount());
        intent.putExtra(CreateVoucherActivity.EXTRA_USAGE_LIMIT, voucher.safeUsageLimit());
        intent.putExtra(CreateVoucherActivity.EXTRA_USED_COUNT, voucher.safeUsedCount());
        intent.putExtra(CreateVoucherActivity.EXTRA_START_DATE, voucher.getStartDate());
        intent.putExtra(CreateVoucherActivity.EXTRA_END_DATE, voucher.getEndDate());
        startActivity(intent);
    }

    private void renderEmpty() {
        voucherContainer.removeAllViews();
        emptyLayout.setVisibility(View.VISIBLE);
        renderStats();
    }

    private String getVoucherDescription(VoucherResponse voucher) {
        if (isPercent(voucher)) {
            String desc = "Giảm " + voucher.safeDiscountValue() + "%";
            if (voucher.safeMaxDiscountAmount() > 0) {
                desc += " tối đa " + formatMoney(voucher.safeMaxDiscountAmount());
            }
            if (voucher.safeMinOrderValue() > 0) {
                desc += " cho đơn từ " + formatShortMoney(voucher.safeMinOrderValue());
            }
            return desc;
        }

        String desc = "Giảm " + formatMoney(voucher.safeDiscountValue());
        if (voucher.safeMinOrderValue() > 0) {
            desc += " cho đơn từ " + formatShortMoney(voucher.safeMinOrderValue());
        }
        return desc;
    }

    private boolean isPercent(VoucherResponse voucher) {
        return "PERCENT".equalsIgnoreCase(voucher.getDiscountType());
    }

    private String getStatusLabel(VoucherResponse voucher) {
        VoucherDisplayStatus status = getDisplayStatus(voucher);
        if (status == VoucherDisplayStatus.ONGOING) return "Đang diễn\nra";
        if (status == VoucherDisplayStatus.UPCOMING) return "Sắp\ntới";
        return "Kết\nthúc";
    }

    private VoucherDisplayStatus getDisplayStatus(VoucherResponse voucher) {
        String backendStatus = voucher.getStatus();
        if (backendStatus != null) {
            if ("INACTIVE".equalsIgnoreCase(backendStatus)
                    || "HIDDEN".equalsIgnoreCase(backendStatus)
                    || "DELETED".equalsIgnoreCase(backendStatus)
                    || "EXPIRED".equalsIgnoreCase(backendStatus)) {
                return VoucherDisplayStatus.ENDED;
            }
        }

        Date now = new Date();
        Date startDate = parseIsoDate(voucher.getStartDate());
        Date endDate = parseIsoDate(voucher.getEndDate());

        if (startDate != null && now.before(startDate)) {
            return VoucherDisplayStatus.UPCOMING;
        }

        if (endDate != null && now.after(endDate)) {
            return VoucherDisplayStatus.ENDED;
        }

        return VoucherDisplayStatus.ONGOING;
    }

    private Date parseIsoDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;

        String normalized = value.trim();
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };

        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.US).parse(normalized);
            } catch (ParseException ignored) {
            }
        }

        return null;
    }

    private String formatMoney(long amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + "đ";
    }

    private String formatShortMoney(long amount) {
        if (amount >= 1_000_000) {
            long value = amount / 1_000_000;
            return value + "tr";
        }
        if (amount >= 1_000) {
            long value = amount / 1_000;
            return value + "k";
        }
        return String.valueOf(amount);
    }

    private Space createSpace(int heightDp) {
        Space space = new Space(requireContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(heightDp)
        ));
        return space;
    }

    private GradientDrawable cardBackground(int color, int radiusDp, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeColor != Color.TRANSPARENT) {
            drawable.setStroke(dp(1), strokeColor);
        }
        return drawable;
    }

    private GradientDrawable leftVoucherBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);

        float radius = dp(18);
        drawable.setCornerRadii(new float[]{
                radius, radius,
                0, 0,
                0, 0,
                radius, radius
        });
        return drawable;
    }

    private String emptyToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) return defaultValue;
        return value.trim();
    }

    private void showToast(String message) {
        if (message == null || message.trim().isEmpty()) return;
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private enum VoucherDisplayStatus {
        ONGOING,
        UPCOMING,
        ENDED
    }
}






