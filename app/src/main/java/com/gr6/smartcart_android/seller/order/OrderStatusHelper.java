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
            default:
                return s.isEmpty() ? "KHÔNG RÕ" : s;
        }
    }

    public static boolean belongsToTab(String status, String tab) {
        String s = normalize(status);
        String t = normalize(tab);

        if (t.equals("ALL")) return true;
        if (t.equals("SHIPPING")) return s.equals("SHIPPING") || s.equals("DELIVERED");
        if (t.equals("COMPLETED")) return s.equals("COMPLETED");
        return s.equals(t);
    }

    public static String nextActionLabel(String status) {
        String s = normalize(status);
        switch (s) {
            case "PENDING":
                return "Xác nhận đơn";
            case "CONFIRMED":
                return "Chuyển đang giao";
            case "SHIPPING":
                return "Hoàn thành giao";
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
}
