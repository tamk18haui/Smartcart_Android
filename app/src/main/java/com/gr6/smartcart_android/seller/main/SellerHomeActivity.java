package com.gr6.smartcart_android.seller.main;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.chat.ChatListActivity;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.seller.inventory.SellerInventoryFragment;
import com.gr6.smartcart_android.seller.order.SellerOrdersFragment;
import com.gr6.smartcart_android.seller.product.SellerProductsFragment;
import com.gr6.smartcart_android.seller.shop.repository.SellerShopRepository;
import com.gr6.smartcart_android.seller.shop.response.SellerShopInfoResponse;

public class SellerHomeActivity extends BaseActivity {

    private static final String BACK_STACK_DASHBOARD_CHILD = "seller_dashboard_child";

    private LinearLayout layoutChecking;
    private TextView txtCheckingMessage;
    private FrameLayout sellerContentContainer;
    private LinearLayout sellerBottomBar;

    private LinearLayout tabDashboard;
    private LinearLayout tabProducts;
    private LinearLayout tabOrders;
    private LinearLayout tabProfile;

    private ImageView imgTabDashboard;
    private ImageView imgTabProducts;
    private ImageView imgTabOrders;
    private ImageView imgTabProfile;

    private TextView txtTabDashboard;
    private TextView txtTabProducts;
    private TextView txtTabOrders;
    private TextView txtTabProfile;

    private SellerShopInfoResponse currentShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_home);

        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        initViews();
        initEvents();

        showCheckingShopScreen();
        checkShopStatus();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            updateBottomTab(tabDashboard);
            return;
        }

        super.onBackPressed();
    }

    private void initViews() {
        layoutChecking = findViewById(R.id.layoutChecking);
        txtCheckingMessage = findViewById(R.id.txtCheckingMessage);
        sellerContentContainer = findViewById(R.id.sellerContentContainer);
        sellerBottomBar = findViewById(R.id.sellerBottomBar);

        tabDashboard = findViewById(R.id.tabDashboard);
        tabProducts = findViewById(R.id.tabProducts);
        tabOrders = findViewById(R.id.tabOrders);
        tabProfile = findViewById(R.id.tabProfile);

        imgTabDashboard = findViewById(R.id.imgTabDashboard);
        imgTabProducts = findViewById(R.id.imgTabProducts);
        imgTabOrders = findViewById(R.id.imgTabOrders);
        imgTabProfile = findViewById(R.id.imgTabProfile);

        txtTabDashboard = findViewById(R.id.txtTabDashboard);
        txtTabProducts = findViewById(R.id.txtTabProducts);
        txtTabOrders = findViewById(R.id.txtTabOrders);
        txtTabProfile = findViewById(R.id.txtTabProfile);
    }

    private void initEvents() {
        tabDashboard.setOnClickListener(v -> openDashboardTab());
        tabProducts.setOnClickListener(v -> openProductsTab());
        tabOrders.setOnClickListener(v -> openOrdersTab());
        tabProfile.setOnClickListener(v -> openProfileTab());
    }

    private void showCheckingShopScreen() {
        layoutChecking.setVisibility(View.VISIBLE);
        sellerContentContainer.setVisibility(View.GONE);
        sellerBottomBar.setVisibility(View.GONE);

        txtCheckingMessage.setText("Đang kiểm tra trạng thái cửa hàng...");
    }

    private void checkShopStatus() {
        new SellerShopRepository(this).loadMyShopInfo(new SellerShopRepository.ShopCallback<SellerShopInfoResponse>() {
            @Override
            public void onSuccess(SellerShopInfoResponse data, String message) {
                runOnUiThread(() -> {
                    currentShop = data;

                    String status = normalizeStatus(data == null ? null : data.getStatus());

                    if ("ACTIVE".equals(status)) {
                        showSellerMainScreen();
                    } else {
                        showShopPendingScreen(data, status, message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(SellerHomeActivity.this, message, Toast.LENGTH_LONG).show();
                    showShopPendingScreen(null, "PENDING", message);
                });
            }
        });
    }

    private void showSellerMainScreen() {
        layoutChecking.setVisibility(View.GONE);
        sellerContentContainer.setVisibility(View.VISIBLE);
        sellerBottomBar.setVisibility(View.VISIBLE);

        openDashboardTab();
    }

    private void showShopPendingScreen(SellerShopInfoResponse shop, String status, String message) {
        layoutChecking.setVisibility(View.GONE);
        sellerContentContainer.setVisibility(View.VISIBLE);
        sellerBottomBar.setVisibility(View.GONE);

        String shopName = shop == null ? "Cửa hàng SmartCart" : shop.getSafeShopName();

        clearSellerBackStack();
        replaceFragment(
                SellerShopPendingFragment.newInstance(status, shopName, message)
        );
    }

    public void openDashboardTab() {
        clearSellerBackStack();
        updateBottomTab(tabDashboard);
        replaceFragment(SellerDashboardFragment.newInstance(currentShop));
    }

    public void openProductsTab() {
        clearSellerBackStack();
        updateBottomTab(tabProducts);
        replaceFragment(
                SellerProductsFragment.newInstance(
                        currentShop == null ? null : currentShop.getShopId()
                )
        );
    }

    public void openOrdersTab() {
        clearSellerBackStack();
        updateBottomTab(tabOrders);
        replaceFragment(new SellerOrdersFragment());
    }

    public void openProfileTab() {
        clearSellerBackStack();
        updateBottomTab(tabProfile);
        replaceFragment(new SellerProfileFragment());
    }

    public void openInventoryFromDashboard() {
        updateBottomTab(tabDashboard);
        replaceFragmentWithBackStack(new SellerInventoryFragment());
    }

    public void openLowStockInventoryFromDashboard() {
        updateBottomTab(tabDashboard);
        replaceFragmentWithBackStack(SellerInventoryFragment.newInstance(true));
    }

    public void openChatList() {
        startActivity(new Intent(this, ChatListActivity.class));
    }

    private void replaceFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.sellerContentContainer, fragment)
                .commit();
    }

    private void replaceFragmentWithBackStack(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.sellerContentContainer, fragment)
                .addToBackStack(BACK_STACK_DASHBOARD_CHILD)
                .commit();
    }

    private void clearSellerBackStack() {
        getSupportFragmentManager().popBackStack(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
        );
    }

    private void updateBottomTab(@NonNull LinearLayout selectedTab) {
        setTabState(
                tabDashboard,
                imgTabDashboard,
                txtTabDashboard,
                selectedTab == tabDashboard
        );

        setTabState(
                tabProducts,
                imgTabProducts,
                txtTabProducts,
                selectedTab == tabProducts
        );

        setTabState(
                tabOrders,
                imgTabOrders,
                txtTabOrders,
                selectedTab == tabOrders
        );

        setTabState(
                tabProfile,
                imgTabProfile,
                txtTabProfile,
                selectedTab == tabProfile
        );
    }

    private void setTabState(
            LinearLayout tab,
            ImageView icon,
            TextView label,
            boolean selected
    ) {
        int color = ContextCompat.getColor(
                this,
                selected ? R.color.nav_active : R.color.nav_inactive
        );

        icon.setColorFilter(color);
        label.setTextColor(color);
        label.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);

        tab.setSelected(selected);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "PENDING";
        }

        return status.trim().toUpperCase();
    }
}
