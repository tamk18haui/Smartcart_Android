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
import com.gr6.smartcart_android.seller.wallet.response.DailyRevenueResponse;

import java.util.ArrayList;
import java.util.List;

public class SellerRevenueChartView extends View {

    private final List<DailyRevenueResponse> data = new ArrayList<>();
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public SellerRevenueChartView(Context context) {
        super(context);
        init();
    }

    public SellerRevenueChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SellerRevenueChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint.setColor(ContextCompat.getColor(getContext(), R.color.seller_primary_light));
        barPaint.setColor(ContextCompat.getColor(getContext(), R.color.seller_primary));
        highlightPaint.setColor(ContextCompat.getColor(getContext(), R.color.seller_primary_dark));
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        textPaint.setTextSize(sp(11));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<DailyRevenueResponse> newData) {
        data.clear();
        if (newData != null) {
            data.addAll(newData);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int paddingStart = dp(8);
        int paddingEnd = dp(8);
        int top = dp(8);
        int bottomTextHeight = dp(24);
        int chartHeight = Math.max(1, height - top - bottomTextHeight);

        if (data.isEmpty()) {
            textPaint.setTextSize(sp(13));
            canvas.drawText("Chưa có dữ liệu doanh thu", width / 2f, height / 2f, textPaint);
            textPaint.setTextSize(sp(11));
            return;
        }

        long maxRevenue = 0L;
        for (DailyRevenueResponse item : data) {
            if (item != null) {
                maxRevenue = Math.max(maxRevenue, item.getRevenue());
            }
        }
        if (maxRevenue <= 0) {
            maxRevenue = 1;
        }

        int count = data.size();
        float usableWidth = width - paddingStart - paddingEnd;
        float gap = dp(8);
        float barWidth = Math.max(dp(12), (usableWidth - gap * (count - 1)) / count);
        float startX = paddingStart;

        for (int i = 0; i < count; i++) {
            DailyRevenueResponse item = data.get(i);
            long revenue = item == null ? 0L : item.getRevenue();
            float percent = Math.max(0.04f, revenue * 1f / maxRevenue);
            float barHeight = chartHeight * percent;
            float left = startX + i * (barWidth + gap);
            float right = left + barWidth;
            float topBar = top + chartHeight - barHeight;
            float bottomBar = top + chartHeight;

            rect.set(left, top + dp(12), right, bottomBar);
            canvas.drawRoundRect(rect, dp(4), dp(4), bgPaint);

            rect.set(left, topBar, right, bottomBar);
            Paint paint = i == count - 1 ? highlightPaint : barPaint;
            canvas.drawRoundRect(rect, dp(4), dp(4), paint);

            String label = shortDate(item == null ? "" : item.getDate());
            canvas.drawText(label, left + barWidth / 2f, height - dp(6), textPaint);
        }
    }

    private String shortDate(String date) {
        if (date == null || date.length() < 10) return "--";
        return date.substring(8, 10) + "/" + date.substring(5, 7);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int sp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().scaledDensity);
    }
}
