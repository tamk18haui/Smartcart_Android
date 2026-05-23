package com.gr6.smartcart_android.seller.order;

public class OrderStatusHelper {

    private OrderStatusHelper() {
    }

    public static String normalize(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }

    public static String label(String status) {
        String s = normalize(status);

        switch (s) {
            case "PENDING_PAYMENT":
                return "CHỜ THANH TOÁN";
            case "PENDING":
                return "CHỜ XÁC NHẬN";
            case "CONFIRMED":
                return "ĐÃ XÁC NHẬN";
            case "SHIPPING":
                return "ĐANG GIAO";
            case "DELIVERED":
                return "ĐÃ GIAO";
            case "COMPLETED":
                return "HOÀN THÀNH";
            case "CANCELLED":
                return "ĐÃ HỦY";
            case "PAYMENT_FAILED":
                return "THANH TOÁN LỖI";
            default:
                return s.isEmpty() ? "KHÔNG RÕ" : s;
        }
    }

    public static boolean belongsToTab(String status, String tab) {
        String s = normalize(status);
        String t = normalize(tab);

        if ("ALL".equals(t)) {
            return true;
        }

        if ("PENDING".equals(t)) {
            return "PENDING".equals(s) || "PENDING_PAYMENT".equals(s);
        }

        if ("SHIPPING".equals(t)) {
            return "SHIPPING".equals(s) || "DELIVERED".equals(s);
        }

        if ("COMPLETED".equals(t)) {
            return "COMPLETED".equals(s);
        }

        return s.equals(t);
    }

    public static String nextActionLabel(String status) {
        String s = normalize(status);

        switch (s) {
            case "PENDING_PAYMENT":
                return "Chờ thanh toán";
            case "PENDING":
                return "Xác nhận đơn";
            case "CONFIRMED":
                return "Chuyển đang giao";
            case "SHIPPING":
                return "Đã giao hàng";
            case "DELIVERED":
                return "Chờ người mua";
            case "COMPLETED":
                return "Xem chi tiết";
            case "CANCELLED":
                return "Xem chi tiết";
            case "PAYMENT_FAILED":
                return "Xem chi tiết";
            default:
                return "Xem chi tiết";
        }
    }

    public static String nextStatus(String status) {
        String s = normalize(status);

        switch (s) {
            case "PENDING":
                return "CONFIRMED";
            case "CONFIRMED":
                return "SHIPPING";
            case "SHIPPING":
                return "DELIVERED";
            default:
                return "";
        }
    }

    public static boolean canSellerQuickAction(String status) {
        String s = normalize(status);

        return "PENDING".equals(s)
                || "CONFIRMED".equals(s)
                || "SHIPPING".equals(s);
    }
}