package com.gr6.smartcart_android.seller.wallet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gr6.smartcart_android.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SellerRevenueStatisticChartView extends View {

    public static class StatisticItem {
        public String label;
        public long revenue;
        public long orderCount;

        public StatisticItem(String label, long revenue, long orderCount) {
            this.label = label == null ? "" : label;
            this.revenue = Math.max(revenue, 0L);
            this.orderCount = Math.max(orderCount, 0L);
        }
    }

    private final List<StatisticItem> items = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();
    private final DecimalFormat compactFormat = new DecimalFormat("#,###");

    public SellerRevenueStatisticChartView(Context context) {
        super(context);
        init();
    }

    public SellerRevenueStatisticChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SellerRevenueStatisticChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setMinimumHeight(dp(230));
    }

    public void setData(List<StatisticItem> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        int left = dp(18);
        int right = width - dp(18);
        int top = dp(22);
        int bottom = height - dp(48);

        drawBackground(canvas, left, top, right, bottom);

        if (items.isEmpty()) {
            drawEmpty(canvas, width, height);
            return;
        }

        long maxRevenue = 0L;
        for (StatisticItem item : items) {
            if (item != null && item.revenue > maxRevenue) {
                maxRevenue = item.revenue;
            }
        }

        if (maxRevenue <= 0L) {
            maxRevenue = 1L;
        }

        int count = items.size();
        float chartWidth = right - left;
        float slotWidth = chartWidth / Math.max(count, 1);
        float barWidth = Math.min(dp(32), slotWidth * 0.46f);

        for (int i = 0; i < count; i++) {
            StatisticItem item = items.get(i);
            if (item == null) {
                continue;
            }

            float centerX = left + slotWidth * i + slotWidth / 2f;
            float percent = item.revenue <= 0 ? 0f : (float) item.revenue / (float) maxRevenue;
            float barHeight = Math.max(dp(8), (bottom - top) * percent);
            float barTop = bottom - barHeight;

            barRect.set(
                    centerX - barWidth / 2f,
                    barTop,
                    centerX + barWidth / 2f,
                    bottom
            );

            paint.setColor(ContextCompat.getColor(getContext(), R.color.seller_primary_dark));
            canvas.drawRoundRect(barRect, dp(9), dp(9), paint);

            paint.setTextAlign(Paint.Align.CENTER);

            paint.setColor(ContextCompat.getColor(getContext(), R.color.seller_text_secondary));
            paint.setTextSize(dp(9));
            canvas.drawText(item.orderCount + " đơn", centerX, Math.max(top + dp(10), barTop - dp(6)), paint);

            paint.setColor(ContextCompat.getColor(getContext(), R.color.seller_text_primary));
            paint.setTextSize(dp(10));
            canvas.drawText(compactLabel(item.label, i), centerX, height - dp(25), paint);

            paint.setColor(ContextCompat.getColor(getContext(), R.color.seller_primary_dark));
            paint.setTextSize(dp(9));
            canvas.drawText(compactMoney(item.revenue), centerX, height - dp(8), paint);
        }
    }

    private void drawBackground(Canvas canvas, int left, int top, int right, int bottom) {
        paint.setStrokeWidth(dp(1));
        paint.setColor(ContextCompat.getColor(getContext(), R.color.seller_divider));

        for (int i = 0; i <= 4; i++) {
            float y = top + (bottom - top) * i / 4f;
            canvas.drawLine(left, y, right, y, paint);
        }
    }

    private void drawEmpty(Canvas canvas, int width, int height) {
        paint.setColor(ContextCompat.getColor(getContext(), R.color.seller_text_secondary));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dp(14));
        canvas.drawText("Chưa có dữ liệu biểu đồ", width / 2f, height / 2f, paint);
    }

    private String compactLabel(String label, int index) {
        if (label == null) {
            return "";
        }

        String value = label.trim();
        if (value.isEmpty()) {
            return "";
        }

        // Nhãn thống kê theo tuần dạng "18/05 - 24/05" sẽ bị dài và chồng chữ,
        // nên trên biểu đồ chỉ hiện T1, T2, T3...
        if (value.contains(" - ")) {
            return "T" + (index + 1);
        }

        // Nhãn thống kê theo tháng dạng "05/2026" rút gọn thành "05/26".
        if (value.matches("\\d{2}/\\d{4}")) {
            return value.substring(0, 2) + "/" + value.substring(5);
        }

        if (value.length() <= 8) {
            return value;
        }

        return value.substring(0, 8);
    }

    private String compactMoney(long value) {
        if (value >= 1_000_000_000L) {
            return compactFormat.format(value / 1_000_000_000L) + " tỷ";
        }

        if (value >= 1_000_000L) {
            return compactFormat.format(value / 1_000_000L) + "tr";
        }

        if (value >= 1_000L) {
            return compactFormat.format(value / 1_000L) + "k";
        }

        return value + "đ";
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}