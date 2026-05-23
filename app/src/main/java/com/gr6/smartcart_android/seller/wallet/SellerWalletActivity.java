package com.gr6.smartcart_android.seller.wallet;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.base.PageResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.seller.order.api.SellerOrderApiService;
import com.gr6.smartcart_android.seller.order.response.OrderListResponse;
import com.gr6.smartcart_android.seller.wallet.api.SellerWalletApiService;
import com.gr6.smartcart_android.seller.wallet.request.AnalyticsDateFilterRequest;
import com.gr6.smartcart_android.seller.wallet.request.WithdrawCreateRequest;
import com.gr6.smartcart_android.seller.wallet.response.DailyRevenueResponse;
import com.gr6.smartcart_android.seller.wallet.response.RevenueReportResponse;
import com.gr6.smartcart_android.seller.wallet.response.SellerSettlementResponse;
import com.gr6.smartcart_android.seller.wallet.response.WalletSummaryResponse;
import com.gr6.smartcart_android.seller.wallet.response.WalletTransactionResponse;
import com.gr6.smartcart_android.seller.wallet.response.WithdrawResponse;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerWalletActivity extends BaseActivity {

    private TextView txtWalletBalance;
    private TextView txtTodayRevenue;
    private TextView txtRevenueOrderCount;
    private TextView txtTransactionStatus;
    private TextView txtWithdrawRequestCount;
    private TextView txtSettlementCount;
    private LinearLayout layoutTransactions;
    private LinearLayout layoutWithdrawRequests;
    private LinearLayout layoutSettlements;
    private SellerRevenueChartView chartRevenue;

    private SellerWalletApiService walletApiService;
    private SellerOrderApiService orderApiService;
    private WalletSummaryResponse currentWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_wallet);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        walletApiService = ApiClient.createService(this, SellerWalletApiService.class);
        orderApiService = ApiClient.createService(this, SellerOrderApiService.class);

        bindViews();
        bindEvents();
        loadAllData(true);
    }

    private void bindViews() {
        txtWalletBalance = findViewById(R.id.txtWalletBalance);
        txtTodayRevenue = findViewById(R.id.txtTodayRevenue);
        txtRevenueOrderCount = findViewById(R.id.txtRevenueOrderCount);
        txtTransactionStatus = findViewById(R.id.txtTransactionStatus);
        txtWithdrawRequestCount = findViewById(R.id.txtWithdrawRequestCount);
        txtSettlementCount = findViewById(R.id.txtSettlementCount);
        layoutTransactions = findViewById(R.id.layoutTransactions);
        layoutWithdrawRequests = findViewById(R.id.layoutWithdrawRequests);
        layoutSettlements = findViewById(R.id.layoutSettlements);
        chartRevenue = findViewById(R.id.chartRevenue);
    }

    private void bindEvents() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnReloadWallet).setOnClickListener(v -> loadAllData(true));
        findViewById(R.id.btnWithdraw).setOnClickListener(v -> showWithdrawDialog());
        findViewById(R.id.btnWalletSetting).setOnClickListener(v ->
                showToast("Thông tin ngân hàng sẽ được nhập khi tạo yêu cầu rút tiền")
        );
    }

    private void loadAllData(boolean showLoadingDialog) {
        if (showLoadingDialog) showLoading();
        loadWallet();
        loadRevenue();
        loadTransactions();
        loadWithdrawRequests();
        loadSettlements();
    }

    private void loadWallet() {
        walletApiService.getMyWallet().enqueue(new Callback<BaseResponse<WalletSummaryResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<WalletSummaryResponse>> call,
                    @NonNull Response<BaseResponse<WalletSummaryResponse>> response
            ) {
                hideLoading();
                BaseResponse<WalletSummaryResponse> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    String message = body == null ? "Không lấy được ví người bán" : body.getSafeMessage();
                    showToast(message);
                    txtWalletBalance.setText("0đ");
                    return;
                }

                currentWallet = body.getData();
                txtWalletBalance.setText(formatMoney(currentWallet.getBalance()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<WalletSummaryResponse>> call,
                    @NonNull Throwable t
            ) {
                hideLoading();
                txtWalletBalance.setText("0đ");
                showToast("Không kết nối được ví: " + t.getMessage());
            }
        });
    }

    private void loadRevenue() {
        if (orderApiService == null) {
            loadRevenueFromAnalyticsOnly();
            return;
        }

        orderApiService.getOrders(null).enqueue(new Callback<BaseResponse<List<OrderListResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<OrderListResponse>>> call,
                    @NonNull Response<BaseResponse<List<OrderListResponse>>> response
            ) {
                BaseResponse<List<OrderListResponse>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    loadRevenueFromAnalyticsOnly();
                    return;
                }

                renderRevenueFromOrders(body.getData());
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<OrderListResponse>>> call,
                    @NonNull Throwable t
            ) {
                loadRevenueFromAnalyticsOnly();
            }
        });
    }

    private void loadRevenueFromAnalyticsOnly() {
        String endDate = formatApiDate(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -6);
        String startDate = formatApiDate(calendar.getTime());

        walletApiService.getRevenueReport(new AnalyticsDateFilterRequest(startDate, endDate))
                .enqueue(new Callback<BaseResponse<RevenueReportResponse>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<BaseResponse<RevenueReportResponse>> call,
                            @NonNull Response<BaseResponse<RevenueReportResponse>> response
                    ) {
                        BaseResponse<RevenueReportResponse> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                            txtTodayRevenue.setText("+0đ");
                            txtRevenueOrderCount.setText("0 đơn");
                            chartRevenue.setData(new ArrayList<>());
                            return;
                        }

                        renderRevenueFromAnalytics(body.getData(), endDate);
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<BaseResponse<RevenueReportResponse>> call,
                            @NonNull Throwable t
                    ) {
                        txtTodayRevenue.setText("+0đ");
                        txtRevenueOrderCount.setText("0 đơn");
                        chartRevenue.setData(new ArrayList<>());
                    }
                });
    }

    private void renderRevenueFromAnalytics(RevenueReportResponse report, String today) {
        List<DailyRevenueResponse> days = report == null ? new ArrayList<>() : report.getDailyDetails();
        long todayRevenue = 0L;
        long todayOrders = 0L;

        for (DailyRevenueResponse item : days) {
            if (item != null && today.equals(item.getDate())) {
                todayRevenue = item.getRevenue();
                todayOrders = item.getOrderCount();
                break;
            }
        }

        txtTodayRevenue.setText("+" + formatMoney(todayRevenue));
        txtRevenueOrderCount.setText(todayOrders + " đơn");
        chartRevenue.setData(days);
    }

    private void renderRevenueFromOrders(List<OrderListResponse> orders) {
        List<DailyRevenueResponse> days = createEmptySevenDayRevenue();
        String today = formatApiDate(new Date());
        long todayRevenue = 0L;
        long todayOrders = 0L;

        if (orders != null) {
            for (OrderListResponse order : orders) {
                if (order == null || !isRevenueOrderStatus(order.getStatus())) continue;

                String day = extractDate(order.getCreatedAt());
                if (day.isEmpty()) continue;

                long amount = toLong(order.getTotalAmount());
                DailyRevenueResponse target = findDay(days, day);
                if (target != null) {
                    target.setRevenue(target.getRevenue() + amount);
                    target.setOrderCount(target.getOrderCount() + 1);
                }

                if (today.equals(day)) {
                    todayRevenue += amount;
                    todayOrders++;
                }
            }
        }

        txtTodayRevenue.setText("+" + formatMoney(todayRevenue));
        txtRevenueOrderCount.setText(todayOrders + " đơn");
        chartRevenue.setData(days);
    }

    private List<DailyRevenueResponse> createEmptySevenDayRevenue() {
        List<DailyRevenueResponse> result = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -6);

        for (int i = 0; i < 7; i++) {
            result.add(new DailyRevenueResponse(formatApiDate(calendar.getTime()), 0L, 0L));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return result;
    }

    private DailyRevenueResponse findDay(List<DailyRevenueResponse> days, String date) {
        if (days == null || date == null) return null;

        for (DailyRevenueResponse item : days) {
            if (item != null && date.equals(item.getDate())) {
                return item;
            }
        }

        return null;
    }

    private String extractDate(String raw) {
        if (raw == null) return "";
        String value = raw.trim();
        if (value.length() >= 10) {
            return value.substring(0, 10);
        }
        return "";
    }

    private boolean isRevenueOrderStatus(String status) {
        String s = status == null ? "" : status.trim().toUpperCase(Locale.US);
        return "PENDING".equals(s)
                || "CONFIRMED".equals(s)
                || "PREPARING".equals(s)
                || "SHIPPING".equals(s)
                || "DELIVERED".equals(s)
                || "COMPLETED".equals(s);
    }

    private long toLong(BigDecimal value) {
        return value == null ? 0L : value.longValue();
    }

    private void loadTransactions() {
        walletApiService.getMyWalletTransactions(1, 10)
                .enqueue(new Callback<BaseResponse<PageResponse<WalletTransactionResponse>>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<BaseResponse<PageResponse<WalletTransactionResponse>>> call,
                            @NonNull Response<BaseResponse<PageResponse<WalletTransactionResponse>>> response
                    ) {
                        BaseResponse<PageResponse<WalletTransactionResponse>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                            txtTransactionStatus.setText("Không tải được");
                            renderEmpty(layoutTransactions, "Chưa có giao dịch ví");
                            return;
                        }

                        txtTransactionStatus.setText(body.getData().getTotalElements() + " giao dịch");
                        renderTransactions(body.getData().getData());
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<BaseResponse<PageResponse<WalletTransactionResponse>>> call,
                            @NonNull Throwable t
                    ) {
                        txtTransactionStatus.setText("Lỗi kết nối");
                        renderEmpty(layoutTransactions, "Không kết nối được server ví");
                    }
                });
    }

    private void loadWithdrawRequests() {
        walletApiService.getMyWithdrawRequests(1, 5)
                .enqueue(new Callback<BaseResponse<PageResponse<WithdrawResponse>>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<BaseResponse<PageResponse<WithdrawResponse>>> call,
                            @NonNull Response<BaseResponse<PageResponse<WithdrawResponse>>> response
                    ) {
                        BaseResponse<PageResponse<WithdrawResponse>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                            txtWithdrawRequestCount.setText("0 yêu cầu");
                            renderEmpty(layoutWithdrawRequests, "Chưa có yêu cầu rút tiền");
                            return;
                        }

                        txtWithdrawRequestCount.setText(body.getData().getTotalElements() + " yêu cầu");
                        renderWithdrawRequests(body.getData().getData());
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<BaseResponse<PageResponse<WithdrawResponse>>> call,
                            @NonNull Throwable t
                    ) {
                        txtWithdrawRequestCount.setText("0 yêu cầu");
                        renderEmpty(layoutWithdrawRequests, "Không tải được yêu cầu rút tiền");
                    }
                });
    }

    private void loadSettlements() {
        walletApiService.getMySettlements(1, 5)
                .enqueue(new Callback<BaseResponse<PageResponse<SellerSettlementResponse>>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<BaseResponse<PageResponse<SellerSettlementResponse>>> call,
                            @NonNull Response<BaseResponse<PageResponse<SellerSettlementResponse>>> response
                    ) {
                        BaseResponse<PageResponse<SellerSettlementResponse>> body = response.body();
                        if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                            txtSettlementCount.setText("0 bản ghi");
                            renderEmpty(layoutSettlements, "Chưa có đối soát đơn hàng");
                            return;
                        }

                        txtSettlementCount.setText(body.getData().getTotalElements() + " bản ghi");
                        renderSettlements(body.getData().getData());
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<BaseResponse<PageResponse<SellerSettlementResponse>>> call,
                            @NonNull Throwable t
                    ) {
                        txtSettlementCount.setText("0 bản ghi");
                        renderEmpty(layoutSettlements, "Không tải được dữ liệu đối soát");
                    }
                });
    }

    private void renderTransactions(List<WalletTransactionResponse> transactions) {
        layoutTransactions.removeAllViews();
        if (transactions == null || transactions.isEmpty()) {
            renderEmpty(layoutTransactions, "Chưa có giao dịch ví");
            return;
        }

        for (WalletTransactionResponse tx : transactions) {
            if (tx != null) layoutTransactions.addView(createTransactionRow(tx));
        }
    }

    private View createTransactionRow(WalletTransactionResponse tx) {
        LinearLayout row = createCardRow();

        TextView icon = new TextView(this);
        icon.setGravity(Gravity.CENTER);
        icon.setText(tx.isIncome() ? "+" : "−");
        icon.setTextSize(24);
        icon.setTypeface(null, android.graphics.Typeface.BOLD);
        icon.setTextColor(ContextCompat.getColor(this, tx.isIncome() ? R.color.success : R.color.seller_primary));
        icon.setBackgroundResource(tx.isIncome() ? R.drawable.bg_wallet_icon_income : R.drawable.bg_wallet_icon_withdraw);
        row.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout info = createVerticalInfo();
        TextView title = createTitle(tx.getDescription());
        TextView time = createSubTitle(shortDateTime(tx.getCreatedAt()));
        info.addView(title);
        info.addView(time);
        row.addView(info, weightedParams());

        TextView amount = new TextView(this);
        amount.setText((tx.isIncome() ? "+" : "-") + formatMoney(Math.abs(tx.getAmount())));
        amount.setTextColor(ContextCompat.getColor(this, tx.isIncome() ? R.color.success : R.color.text_primary));
        amount.setTextSize(17);
        amount.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(amount);

        return row;
    }

    private void renderWithdrawRequests(List<WithdrawResponse> requests) {
        layoutWithdrawRequests.removeAllViews();
        if (requests == null || requests.isEmpty()) {
            renderEmpty(layoutWithdrawRequests, "Chưa có yêu cầu rút tiền");
            return;
        }

        for (WithdrawResponse request : requests) {
            if (request != null) layoutWithdrawRequests.addView(createWithdrawRow(request));
        }
    }

    private View createWithdrawRow(WithdrawResponse request) {
        LinearLayout row = createCardRow();
        TextView icon = createSmallIcon("💳");
        row.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout info = createVerticalInfo();
        info.addView(createTitle("Rút tiền về " + request.getBankName()));
        info.addView(createSubTitle(shortDateTime(request.getCreatedAt()) + " • " + statusLabel(request.getStatus())));
        row.addView(info, weightedParams());

        TextView amount = createAmount("-" + formatMoney(request.getAmount()), R.color.text_primary);
        row.addView(amount);
        return row;
    }

    private void renderSettlements(List<SellerSettlementResponse> settlements) {
        layoutSettlements.removeAllViews();
        if (settlements == null || settlements.isEmpty()) {
            renderEmpty(layoutSettlements, "Chưa có đối soát đơn hàng");
            return;
        }

        for (SellerSettlementResponse settlement : settlements) {
            if (settlement != null) layoutSettlements.addView(createSettlementRow(settlement));
        }
    }

    private View createSettlementRow(SellerSettlementResponse settlement) {
        LinearLayout row = createCardRow();
        TextView icon = createSmallIcon("✓");
        icon.setTextColor(ContextCompat.getColor(this, R.color.success));
        row.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout info = createVerticalInfo();
        String code = settlement.getShopOrderId() == null ? "--" : String.valueOf(settlement.getShopOrderId());
        info.addView(createTitle("Thanh toán đơn #" + code));
        info.addView(createSubTitle(shortDateTime(settlement.getCreatedAt()) + " • " + statusLabel(settlement.getStatus())));
        row.addView(info, weightedParams());

        TextView amount = createAmount("+" + formatMoney(settlement.getNetAmount()), R.color.success);
        row.addView(amount);
        return row;
    }

    private LinearLayout createCardRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));
        row.setBackgroundResource(R.drawable.bg_seller_card);
        row.setElevation(dp(2));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));
        row.setLayoutParams(params);
        return row;
    }

    private TextView createSmallIcon(String value) {
        TextView icon = new TextView(this);
        icon.setGravity(Gravity.CENTER);
        icon.setText(value);
        icon.setTextSize(22);
        icon.setBackgroundResource(R.drawable.bg_wallet_icon_withdraw);
        return icon;
    }

    private LinearLayout createVerticalInfo() {
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setGravity(Gravity.CENTER_VERTICAL);
        info.setPadding(dp(12), 0, dp(10), 0);
        return info;
    }

    private LinearLayout.LayoutParams weightedParams() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    }

    private TextView createTitle(String value) {
        TextView textView = new TextView(this);
        textView.setText(value == null ? "" : value);
        textView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        textView.setTextSize(16);
        textView.setMaxLines(2);
        return textView;
    }

    private TextView createSubTitle(String value) {
        TextView textView = new TextView(this);
        textView.setText(value == null ? "" : value);
        textView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        textView.setTextSize(14);
        textView.setMaxLines(1);
        return textView;
    }

    private TextView createAmount(String value, int colorRes) {
        TextView textView = new TextView(this);
        textView.setText(value == null ? "" : value);
        textView.setTextColor(ContextCompat.getColor(this, colorRes));
        textView.setTextSize(17);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        return textView;
    }

    private void renderEmpty(LinearLayout container, String message) {
        container.removeAllViews();
        TextView empty = new TextView(this);
        empty.setText(message);
        empty.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        empty.setTextSize(14);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(dp(16), dp(18), dp(16), dp(18));
        empty.setBackgroundResource(R.drawable.bg_seller_card);
        container.addView(empty, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
    }

    private void showWithdrawDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_seller_withdraw, null, false);
        EditText edtAmount = view.findViewById(R.id.edtWithdrawAmount);
        EditText edtBankName = view.findViewById(R.id.edtBankName);
        EditText edtBankAccountNumber = view.findViewById(R.id.edtBankAccountNumber);
        EditText edtBankAccountHolder = view.findViewById(R.id.edtBankAccountHolder);
        EditText edtSellerNote = view.findViewById(R.id.edtSellerNote);

        edtAmount.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Gửi yêu cầu", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Long amount = parseLong(edtAmount.getText().toString());
            String bankName = edtBankName.getText().toString().trim();
            String accountNumber = edtBankAccountNumber.getText().toString().trim();
            String accountHolder = edtBankAccountHolder.getText().toString().trim();
            String note = edtSellerNote.getText().toString().trim();

            if (amount == null || amount < 10000) {
                edtAmount.requestFocus();
                edtAmount.setError("Số tiền rút tối thiểu là 10.000đ");
                return;
            }

            if (currentWallet != null && amount > currentWallet.getBalance()) {
                edtAmount.requestFocus();
                edtAmount.setError("Số dư ví không đủ");
                return;
            }

            if (bankName.isEmpty()) {
                edtBankName.requestFocus();
                edtBankName.setError("Vui lòng nhập tên ngân hàng");
                return;
            }

            if (accountNumber.isEmpty()) {
                edtBankAccountNumber.requestFocus();
                edtBankAccountNumber.setError("Vui lòng nhập số tài khoản");
                return;
            }

            if (accountHolder.isEmpty()) {
                edtBankAccountHolder.requestFocus();
                edtBankAccountHolder.setError("Vui lòng nhập tên chủ tài khoản");
                return;
            }

            createWithdrawRequest(dialog, new WithdrawCreateRequest(
                    amount,
                    bankName,
                    accountNumber,
                    accountHolder,
                    note
            ));
        }));

        dialog.show();
    }

    private void createWithdrawRequest(AlertDialog dialog, WithdrawCreateRequest request) {
        showLoading();
        walletApiService.createWithdrawRequest(request).enqueue(new Callback<BaseResponse<WithdrawResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<WithdrawResponse>> call,
                    @NonNull Response<BaseResponse<WithdrawResponse>> response
            ) {
                hideLoading();
                BaseResponse<WithdrawResponse> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    showToast(body == null ? "Tạo yêu cầu rút tiền thất bại" : body.getSafeMessage());
                    return;
                }

                dialog.dismiss();
                showToast(body.getSafeMessage());
                loadAllData(false);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<WithdrawResponse>> call,
                    @NonNull Throwable t
            ) {
                hideLoading();
                showToast("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    private Long parseLong(String raw) {
        if (raw == null) return null;
        try {
            String normalized = raw.replace(".", "").replace(",", "").trim();
            if (normalized.isEmpty()) return null;
            return Long.parseLong(normalized);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatMoney(long value) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(value) + "đ";
    }

    private String formatApiDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
    }

    private String shortDateTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "--";
        String value = raw.trim().replace("T", " ");
        if (value.length() >= 16) return value.substring(0, 16);
        return value;
    }

    private String statusLabel(String raw) {
        String status = raw == null ? "" : raw.trim().toUpperCase(Locale.US);
        if ("PENDING".equals(status)) return "Chờ duyệt";
        if ("APPROVED".equals(status)) return "Hoàn thành";
        if ("REJECTED".equals(status)) return "Bị từ chối";
        if ("SETTLED".equals(status)) return "Đã đối soát";
        if ("COMPLETED".equals(status)) return "Hoàn thành";
        return status.isEmpty() ? "Không rõ" : status;
    }
}


