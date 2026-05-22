package com.gr6.smartcart_android.common.utils;

public class Constants {

    private Constants() {
    }

    /*
     * Emulator Android Studio dùng:
     * http://10.0.2.2:8080/
     *
     * Máy thật dùng IP máy tính, ví dụ:
     * http://192.168.1.5:8080/
     */
    public static final String BASE_URL = "http://192.168.2.22:8080/";

    public static final int CONNECT_TIMEOUT_SECONDS = 30;
    public static final int READ_TIMEOUT_SECONDS = 30;
    public static final int WRITE_TIMEOUT_SECONDS = 30;

    public static final String ROLE_BUYER = "BUYER";
    public static final String ROLE_SELLER = "SELLER";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String CHECKOUT_SOURCE_BUY_NOW = "BUY_NOW";
    public static final String CHECKOUT_SOURCE_FROM_CART = "FROM_CART";

    public static final String PAYMENT_METHOD_COD = "COD";
    public static final String PAYMENT_METHOD_ONLINE = "ONLINE";

    public static final String PAYMENT_PROVIDER_NONE = "NONE";
    public static final String PAYMENT_PROVIDER_MOMO = "MOMO";
    public static final String PAYMENT_PROVIDER_VNPAY = "VNPAY";

    public static final String PREF_AUTH = "smartcart_auth";
    public static final String PREF_USER = "smartcart_user";

    public static final String KEY_TOKEN = "token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_FULL_NAME = "full_name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_AVATAR_URL = "avatar_url";
    public static final String KEY_ROLE = "role";
    public static final String KEY_PHONE_NUMBER = "phone_number";

    public static final String CLOUDINARY_CLOUD_NAME = "dtnyw0cyr";
    public static final String CLOUDINARY_UPLOAD_PRESET = "smartcart_avatar";
}