package com.gr6.smartcart_android.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.gr6.smartcart_android.MainActivity;
import com.gr6.smartcart_android.auth.login.LoginActivity;
import com.gr6.smartcart_android.auth.register.RegisterActivity;
//import com.gr6.smartcart_android.buyer.address.AddressActivity;
//import com.gr6.smartcart_android.buyer.cart.CartActivity;
//import com.gr6.smartcart_android.buyer.checkout.CheckoutActivity;
//import com.gr6.smartcart_android.buyer.main.BuyerMainActivity;
//import com.gr6.smartcart_android.buyer.product.ProductDetailActivity;
//import com.gr6.smartcart_android.buyer.shop.BuyerShopActivity;

public class AppNavigator {

    private AppNavigator() {
    }

    public static void openLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void openRegister(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }

    public static void openRoleRouter(Context context) {
        Intent intent = new Intent(context, RoleRouterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

//    public static void openBuyerHome(Context context) {
//        Intent intent = new Intent(context, BuyerMainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        context.startActivity(intent);
//    }
//
//    public static void openProductDetail(Context context, Long productId) {
//        Intent intent = new Intent(context, ProductDetailActivity.class);
//        intent.putExtra(AppRoute.EXTRA_PRODUCT_ID, productId);
//        context.startActivity(intent);
//    }
//
//    public static void openShop(Context context, Long shopId) {
//        Intent intent = new Intent(context, BuyerShopActivity.class);
//        intent.putExtra(AppRoute.EXTRA_SHOP_ID, shopId);
//        context.startActivity(intent);
//    }
//
//    public static void openCart(Context context) {
//        Intent intent = new Intent(context, CartActivity.class);
//        context.startActivity(intent);
//    }
//
//    public static void openCheckoutFromCart(Context context) {
//        Intent intent = new Intent(context, CheckoutActivity.class);
//        intent.putExtra(AppRoute.EXTRA_CHECKOUT_SOURCE, AppRoute.CHECKOUT_SOURCE_FROM_CART);
//        context.startActivity(intent);
//    }
//
//    public static void openCheckoutBuyNow(
//            Context context,
//            Long productId,
//            Long variantId,
//            Integer quantity,
//            Long shopId
//    ) {
//        Intent intent = new Intent(context, CheckoutActivity.class);
//        intent.putExtra(AppRoute.EXTRA_CHECKOUT_SOURCE, AppRoute.CHECKOUT_SOURCE_BUY_NOW);
//        intent.putExtra(AppRoute.EXTRA_PRODUCT_ID_CHECKOUT, productId);
//        intent.putExtra(AppRoute.EXTRA_VARIANT_ID, variantId);
//        intent.putExtra(AppRoute.EXTRA_QUANTITY, quantity);
//        intent.putExtra(AppRoute.EXTRA_SHOP_ID_CHECKOUT, shopId);
//        context.startActivity(intent);
//    }
//
//    public static void openAddressList(Context context) {
//        Intent intent = new Intent(context, AddressActivity.class);
//        intent.putExtra(AppRoute.EXTRA_SELECT_MODE, false);
//        context.startActivity(intent);
//    }
//
//    public static void openAddressSelect(Activity activity, int requestCode) {
//        Intent intent = new Intent(activity, AddressActivity.class);
//        intent.putExtra(AppRoute.EXTRA_SELECT_MODE, true);
//        activity.startActivityForResult(intent, requestCode);
//    }

    public static void restartApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void finishWithFade(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}