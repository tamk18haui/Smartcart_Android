package com.gr6.smartcart_android.seller.wallet;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.seller.wallet.api.SellerWalletApiService;
import com.gr6.smartcart_android.seller.wallet.request.AnalyticsDateFilterRequest;
import com.gr6.smartcart_android.seller.wallet.response.DailyRevenueResponse;
import com.gr6.smartcart_android.seller.wallet.response.RevenueReportResponse;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RevenueStatisticActivity extends BaseActivity {

    public static final String EXTRA_MODE = "extra_revenue_statistic_mode";
    public static final String MODE_WEEK = "WEEK";
    public static final String MODE_MONTH = "MONTH";

    private SellerWalletApiService apiService;

    private TextView txtTitle;
    private TextView txtRange;
    private TextView txtTotalRevenue;
    private TextView txtTotalOrders;
    private SellerRevenueStatisticChartView chartView;
    private LinearLayout layoutStatisticRows;

    private String mode = MODE_WEEK;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat dayLabelFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
    private final SimpleDateFormat monthLabelFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_revenue_statistic);

        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        apiService = ApiClient.createService(this, SellerWalletApiService.class);
        mode = readMode();

        bindViews();
        setupEvents();
        setupTitle();
        loadRevenueStatistic();
    }

    private void bindViews() {
        txtTitle = findViewById(R.id.txtTitle);
        txtRange = findViewById(R.id.txtRange);
        txtTotalRevenue = findViewById(R.id.txtTotalRevenue);
        txtTotalOrders = findViewById(R.id.txtTotalOrders);
        chartView = findViewById(R.id.chartView);
        layoutStatisticRows = findViewById(R.id.layoutStatisticRows);
    }

    private void setupEvents() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private String readMode() {
        String raw = getIntent() == null ? null : getIntent().getStringExtra(EXTRA_MODE);
        if (MODE_MONTH.equalsIgnoreCase(raw)) {
            return MODE_MONTH;
        }
        return MODE_WEEK;
    }

    private void setupTitle() {
        if (MODE_MONTH.equals(mode)) {
            txtTitle.setText("Doanh thu theo tháng");
        } else {
            txtTitle.setText("Doanh thu theo tuần");
        }
    }

    private void loadRevenueStatistic() {
        Calendar end = Calendar.getInstance();
        Calendar start = Calendar.getInstance();

        if (MODE_MONTH.equals(mode)) {
            start.set(Calendar.DAY_OF_MONTH, 1);
            start.add(Calendar.MONTH, -5);
        } else {
            moveToMonday(start);
            start.add(Calendar.WEEK_OF_YEAR, -7);
        }

        clearTime(start);
        setEndOfDay(end);

        String startText = apiDateFormat.format(start.getTime());
        String endText = apiDateFormat.format(end.getTime());

        txtRange.setText("Khoảng thời gian: " + startText + " đến " + endText);
        showLoading();

        apiService.getRevenueReport(new AnalyticsDateFilterRequest(startText, endText))
                .enqueue(new Callback<BaseResponse<RevenueReportResponse>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<BaseResponse<RevenueReportResponse>> call,
                            @NonNull Response<BaseResponse<RevenueReportResponse>> response
                    ) {
                        hideLoading();

                        if (!response.isSuccessful()) {
                            showToast("Không tải được thống kê. Mã lỗi: " + response.code());
                            renderEmpty();
                            return;
                        }

                        BaseResponse<RevenueReportResponse> body = response.body();
                        if (body == null || !body.isSuccess() || body.getData() == null) {
                            showToast(body == null ? "Server không trả dữ liệu" : body.getSafeMessage());
                            renderEmpty();
                            return;
                        }

                        renderReport(body.getData(), start, end);
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<BaseResponse<RevenueReportResponse>> call,
                            @NonNull Throwable t
                    ) {
                        hideLoading();
                        showToast("Không kết nối được server: " + t.getMessage());
                        renderEmpty();
                    }
                });
    }

    private void renderReport(RevenueReportResponse report, Calendar start, Calendar end) {
        if (report == null) {
            renderEmpty();
            return;
        }

        List<SellerRevenueStatisticChartView.StatisticItem> items = MODE_MONTH.equals(mode)
                ? buildMonthlyItems(start, end, report.getDailyDetails())
                : buildWeeklyItems(start, end, report.getDailyDetails());

        long totalRevenue = 0L;
        long totalOrders = 0L;
        for (SellerRevenueStatisticChartView.StatisticItem item : items) {
            totalRevenue += item.revenue;
            totalOrders += item.orderCount;
        }

        txtTotalRevenue.setText(formatMoney(totalRevenue));
        txtTotalOrders.setText(totalOrders + " đơn");
        chartView.setData(items);
        renderRows(items);
    }

    private void renderEmpty() {
        txtTotalRevenue.setText("0đ");
        txtTotalOrders.setText("0 đơn");
        chartView.setData(new ArrayList<>());
        layoutStatisticRows.removeAllViews();
        layoutStatisticRows.addView(createEmptyRow());
    }

    private List<SellerRevenueStatisticChartView.StatisticItem> buildWeeklyItems(
            Calendar start,
            Calendar end,
            List<DailyRevenueResponse> details
    ) {
        LinkedHashMap<String, SellerRevenueStatisticChartView.StatisticItem> map = new LinkedHashMap<>();

        Calendar cursor = cloneCalendar(start);
        moveToMonday(cursor);
        clearTime(cursor);

        Calendar safeEnd = cloneCalendar(end);
        while (!cursor.after(safeEnd)) {
            Calendar weekStart = cloneCalendar(cursor);
            Calendar weekEnd = cloneCalendar(cursor);
            weekEnd.add(Calendar.DAY_OF_MONTH, 6);

            String key = apiDateFormat.format(weekStart.getTime());
            String label = dayLabelFormat.format(weekStart.getTime()) + " - " + dayLabelFormat.format(weekEnd.getTime());
            map.put(key, new SellerRevenueStatisticChartView.StatisticItem(label, 0L, 0L));

            cursor.add(Calendar.WEEK_OF_YEAR, 1);
        }

        if (details != null) {
            for (DailyRevenueResponse detail : details) {
                Calendar date = parseDate(detail == null ? null : detail.getDate());
                if (date == null) continue;

                moveToMonday(date);
                clearTime(date);
                String key = apiDateFormat.format(date.getTime());

                SellerRevenueStatisticChartView.StatisticItem item = map.get(key);
                if (item != null) {
                    item.revenue += detail.getRevenue();
                    item.orderCount += detail.getOrderCount();
                }
            }
        }

        return new ArrayList<>(map.values());
    }

    private List<SellerRevenueStatisticChartView.StatisticItem> buildMonthlyItems(
            Calendar start,
            Calendar end,
            List<DailyRevenueResponse> details
    ) {
        LinkedHashMap<String, SellerRevenueStatisticChartView.StatisticItem> map = new LinkedHashMap<>();

        Calendar cursor = cloneCalendar(start);
        cursor.set(Calendar.DAY_OF_MONTH, 1);
        clearTime(cursor);

        Calendar safeEnd = cloneCalendar(end);
        while (!cursor.after(safeEnd)) {
            String key = monthLabelFormat.format(cursor.getTime());
            map.put(key, new SellerRevenueStatisticChartView.StatisticItem(key, 0L, 0L));
            cursor.add(Calendar.MONTH, 1);
        }

        if (details != null) {
            for (DailyRevenueResponse detail : details) {
                Calendar date = parseDate(detail == null ? null : detail.getDate());
                if (date == null) continue;

                String key = monthLabelFormat.format(date.getTime());
                SellerRevenueStatisticChartView.StatisticItem item = map.get(key);
                if (item != null) {
                    item.revenue += detail.getRevenue();
                    item.orderCount += detail.getOrderCount();
                }
            }
        }

        return new ArrayList<>(map.values());
    }

    private void renderRows(List<SellerRevenueStatisticChartView.StatisticItem> items) {
        layoutStatisticRows.removeAllViews();

        if (items == null || items.isEmpty()) {
            layoutStatisticRows.addView(createEmptyRow());
            return;
        }

        for (SellerRevenueStatisticChartView.StatisticItem item : items) {
            layoutStatisticRows.addView(createStatisticRow(item));
        }
    }

    private TextView createEmptyRow() {
        TextView textView = new TextView(this);
        textView.setText("Chưa có dữ liệu doanh thu trong khoảng thời gian này");
        textView.setTextColor(ContextCompat.getColor(this, R.color.seller_text_secondary));
        textView.setTextSize(14);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, dp(18), 0, dp(18));
        return textView;
    }

    private LinearLayout createStatisticRow(SellerRevenueStatisticChartView.StatisticItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(12));

        TextView label = new TextView(this);
        label.setText(item.label);
        label.setTextColor(ContextCompat.getColor(this, R.color.seller_text_primary));
        label.setTextSize(14);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(label, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView value = new TextView(this);
        value.setText(formatMoney(item.revenue) + " • " + item.orderCount + " đơn");
        value.setTextColor(ContextCompat.getColor(this, R.color.seller_primary_dark));
        value.setTextSize(14);
        value.setGravity(Gravity.END);
        row.addView(value, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.35f));

        return row;
    }

    private Calendar parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(apiDateFormat.parse(raw.trim().substring(0, Math.min(10, raw.trim().length()))));
            return calendar;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void moveToMonday(Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int diff = day == Calendar.SUNDAY ? -6 : Calendar.MONDAY - day;
        calendar.add(Calendar.DAY_OF_MONTH, diff);
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    private Calendar cloneCalendar(Calendar source) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(source.getTimeInMillis());
        return calendar;
    }

    private String formatMoney(long value) {
        return moneyFormat.format(value) + "đ";
    }
}
