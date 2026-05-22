package com.gr6.smartcart_android.chat.util;

public class ChatTimeFormatter {

    private ChatTimeFormatter() {
    }

    public static String shortTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "";
        }

        try {
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

    public static String dateTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "";
        }

        try {
            String value = raw.trim().replace("T", " ");

            if (value.length() >= 16) {
                return value.substring(0, 16);
            }

            return value;
        } catch (Exception e) {
            return "";
        }
    }
}