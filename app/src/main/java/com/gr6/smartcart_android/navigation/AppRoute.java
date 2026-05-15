package com.gr6.smartcart_android.navigation;

public class AppRoute {

    private AppRoute() {
    }

    // Product
    public static final String EXTRA_PRODUCT_ID = "product_id";

    // Shop
    public static final String EXTRA_SHOP_ID = "shop_id";

    // Address
    public static final String EXTRA_SELECT_MODE = "select_mode";

    // Checkout
    public static final String EXTRA_CHECKOUT_SOURCE = "checkout_source";
    public static final String EXTRA_PRODUCT_ID_CHECKOUT = "product_id";
    public static final String EXTRA_VARIANT_ID = "variant_id";
    public static final String EXTRA_QUANTITY = "quantity";
    public static final String EXTRA_SHOP_ID_CHECKOUT = "shop_id";

    public static final String CHECKOUT_SOURCE_BUY_NOW = "BUY_NOW";
    public static final String CHECKOUT_SOURCE_FROM_CART = "FROM_CART";
}