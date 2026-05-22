package com.gr6.smartcart_android.buyer.chat.util;

public class ChatTimeFormatter {

    private ChatTimeFormatter() {
    }

    public static String shortTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "";
        }

        try {
            // Backend thường trả: 2026-05-21T20:30:15.123
            String value = raw.trim();

            int tIndex = value.indexOf('T');
            if (tIndex >= 0 && value.length() >= tIndex + 6) {
                return value.substring(tIndex + 1, tIndex + 6);
            }

            if (value.length() >= 16) {
                return value.substring(11, 16);
            }

            return value;
        } catch (Exception e) {
            return "";
        }
    }
}