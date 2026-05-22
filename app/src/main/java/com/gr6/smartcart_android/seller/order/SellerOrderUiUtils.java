package com.gr6.smartcart_android.seller.order;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class SellerOrderUiUtils {
    private static final DecimalFormat VND_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        VND_FORMAT = new DecimalFormat("#,###", symbols);
    }

    private SellerOrderUiUtils() {
    }

    public static String money(BigDecimal value) {
        if (value == null) return "0đ";
        return VND_FORMAT.format(value) + "đ";
    }

    public static String statusLabel(String status) {
        if (status == null) return "KHÔNG RÕ";
        switch (status) {
            case "PENDING": return "CHỜ XÁC NHẬN";
            case "CONFIRMED": return "ĐÃ XÁC NHẬN";
            case "SHIPPING": return "ĐANG GIAO";
            case "DELIVERED": return "ĐÃ GIAO";
            case "COMPLETED": return "HOÀN THÀNH";
            case "CANCELLED": return "ĐÃ HỦY";
            default: return status;
        }
    }

    public static String formatTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";
        String value = raw.replace('T', ' ');
        int dotIndex = value.indexOf('.');
        if (dotIndex > 0) value = value.substring(0, dotIndex);
        if (value.length() >= 16) return value.substring(0, 16);
        return value;
    }

    public static boolean matchesStatus(String itemStatus, String filter) {
        if (filter == null || filter.equals("ALL")) return true;
        if (filter.equals("DELIVERING")) return "SHIPPING".equals(itemStatus) || "DELIVERED".equals(itemStatus);
        return filter.equals(itemStatus);
    }
}
