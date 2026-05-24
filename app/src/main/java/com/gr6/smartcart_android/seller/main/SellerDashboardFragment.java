package com.gr6.smartcart_android.seller.main;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.chat.api.ChatApiService;
import com.gr6.smartcart_android.chat.response.ConversationResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.base.PageResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.seller.inventory.api.SellerInventoryApiService;
import com.gr6.smartcart_android.seller.inventory.response.InventoryItemResponse;
import com.gr6.smartcart_android.seller.order.api.SellerOrderApiService;
import com.gr6.smartcart_android.seller.order.response.OrderListResponse;
import com.gr6.smartcart_android.seller.product.AddProductActivity;
import com.gr6.smartcart_android.seller.product.api.SellerProductApiService;
import com.gr6.smartcart_android.seller.product.response.ProductResponse;
import com.gr6.smartcart_android.seller.shop.SellerShopInfoActivity;
import com.gr6.smartcart_android.seller.shop.api.SellerShopApiService;
import com.gr6.smartcart_android.seller.shop.response.SellerShopInfoResponse;
import com.gr6.smartcart_android.seller.voucher.CreateVoucherActivity;
import com.gr6.smartcart_android.seller.voucher.SellerVoucherActivity;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerDashboardFragment extends Fragment {

    private static SellerShopInfoResponse cachedShop;

    private ImageView imgSellerShopAvatar;
    private TextView txtSellerShopName;
    private TextView txtSellerStatus;
    private TextView txtTodayRevenue;
    private TextView txtPendingOrders;
    private TextView txtProductCount;
    private TextView txtLowStockCount;
    private TextView txtShopRating;
    private TextView txtUnreadMessageBadge;
    private TextView txtRevenueDetail;
    private LinearLayout layoutRecentOrdersContainer;

    private View btnShopSetting;
    private View btnDashboardChat;
    private View cardLowStock;
    private View toolAddProduct;
    private View toolOrders;
    private View toolInventory;
    private View toolChat;
    private View toolVoucher;
    private View toolCreateVoucher;
    private TextView txtViewAllOrders;

    private SellerShopApiService shopApiService;
    private SellerProductApiService productApiService;
    private SellerOrderApiService orderApiService;
    private SellerInventoryApiService inventoryApiService;
    private ChatApiService chatApiService;
    private final List<OrderListResponse> cachedOrders = new ArrayList<>();

    public static SellerDashboardFragment newInstance(SellerShopInfoResponse shop) {
        cachedShop = shop;
        return new SellerDashboardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_seller_dashboard, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        initServices();
        initViews(view);
        bindShopInfo(cachedShop);
        initEvents();
        loadDashboardData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shopApiService != null) {
            loadShopInfo();
        }
        if (chatApiService != null) {
            loadUnreadMessages();
        }
    }

    private void initServices() {
        shopApiService = ApiClient.createService(requireContext(), SellerShopApiService.class);
        productApiService = ApiClient.createService(requireContext(), SellerProductApiService.class);
        orderApiService = ApiClient.createService(requireContext(), SellerOrderApiService.class);
        inventoryApiService = ApiClient.createService(requireContext(), SellerInventoryApiService.class);
        chatApiService = ApiClient.createService(requireContext(), ChatApiService.class);
    }

    private void initViews(@NonNull View view) {
        imgSellerShopAvatar = view.findViewById(R.id.imgSellerShopAvatar);
        txtSellerShopName = view.findViewById(R.id.txtSellerShopName);
        txtSellerStatus = view.findViewById(R.id.txtSellerStatus);
        txtTodayRevenue = view.findViewById(R.id.txtTodayRevenue);
        txtPendingOrders = view.findViewById(R.id.txtPendingOrders);
        txtProductCount = view.findViewById(R.id.txtProductCount);
        txtLowStockCount = view.findViewById(R.id.txtLowStockCount);
        txtShopRating = view.findViewById(R.id.txtShopRating);
        txtUnreadMessageBadge = view.findViewById(R.id.txtUnreadMessageBadge);
        txtRevenueDetail = view.findViewById(R.id.txtRevenueDetail);
        layoutRecentOrdersContainer = view.findViewById(R.id.layoutRecentOrdersContainer);

        btnShopSetting = view.findViewById(R.id.btnShopSetting);
        btnDashboardChat = view.findViewById(R.id.btnDashboardChat);
        cardLowStock = view.findViewById(R.id.cardLowStock);
        toolAddProduct = view.findViewById(R.id.toolAddProduct);
        toolOrders = view.findViewById(R.id.toolOrders);
        toolInventory = view.findViewById(R.id.toolInventory);
        toolChat = view.findViewById(R.id.toolChat);
        toolVoucher = view.findViewById(R.id.toolVoucher);
        toolCreateVoucher = view.findViewById(R.id.toolCreateVoucher);
        txtViewAllOrders = view.findViewById(R.id.txtViewAllOrders);
    }

    private void bindShopInfo(SellerShopInfoResponse shop) {
        String shopName = shop == null ? "SmartCart Seller" : shop.getSafeShopName();
        String status = shop == null ? "ACTIVE" : shop.getStatus();
        String logoUrl = shop == null ? "" : shop.getLogoUrl();

        txtSellerShopName.setText(shopName);
        txtSellerStatus.setText("● " + statusLabel(status));
        txtSellerStatus.setTextColor(statusColor(status));
        txtSellerStatus.setTypeface(null, Typeface.BOLD);
        ImageLoader.loadCircle(requireContext(), logoUrl, imgSellerShopAvatar);
    }

    private void initEvents() {
        btnShopSetting.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SellerShopInfoActivity.class))
        );

        btnDashboardChat.setOnClickListener(v -> openChatList());

        toolAddProduct.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddProductActivity.class))
        );

        toolOrders.setOnClickListener(v -> openOrdersTab());
        toolInventory.setOnClickListener(v -> openInventoryTab());
        toolChat.setOnClickListener(v -> openChatList());

        toolVoucher.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SellerVoucherActivity.class))
        );

        toolCreateVoucher.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateVoucherActivity.class))
        );

        txtViewAllOrders.setOnClickListener(v -> openOrdersTab());

        if (cardLowStock != null) {
            cardLowStock.setOnClickListener(v -> openLowStockInventoryTab());
        }

        if (txtRevenueDetail != null) {
            txtRevenueDetail.setOnClickListener(v -> showTodayOrderStatsDialog());
        }
    }

    private void loadDashboardData() {
        loadShopInfo();
        loadOrders();
        loadUnreadMessages();
    }

    private void loadShopInfo() {
        shopApiService.getMyShopInfo().enqueue(new Callback<BaseResponse<SellerShopInfoResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<SellerShopInfoResponse>> call,
                    @NonNull Response<BaseResponse<SellerShopInfoResponse>> response
            ) {
                if (!isAdded()) return;

                BaseResponse<SellerShopInfoResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    cachedShop = body.getData();
                    bindShopInfo(cachedShop);
                    loadProductsAndRating();
                    loadLowStock();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<SellerShopInfoResponse>> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded()) return;
                bindShopInfo(cachedShop);
            }
        });
    }

    private void loadProductsAndRating() {
        Long shopId = cachedShop == null ? null : cachedShop.getShopId();
        if (shopId == null || shopId <= 0) {
            txtProductCount.setText("0");
            setRatingText("--");
            return;
        }

        productApiService.getProductsByShop(shopId, 0, 100).enqueue(new Callback<BaseResponse<PageResponse<ProductResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<PageResponse<ProductResponse>>> call,
                    @NonNull Response<BaseResponse<PageResponse<ProductResponse>>> response
            ) {
                if (!isAdded()) return;

                BaseResponse<PageResponse<ProductResponse>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    txtProductCount.setText("0");
                    setRatingText("--");
                    return;
                }

                PageResponse<ProductResponse> page = body.getData();
                txtProductCount.setText(String.valueOf(page.getTotalElements()));
                setRatingText(calculateAverageProductRating(page.getData()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<PageResponse<ProductResponse>>> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded()) return;
                txtProductCount.setText("0");
                setRatingText("--");
            }
        });
    }

    private void loadOrders() {
        orderApiService.getOrders(null).enqueue(new Callback<BaseResponse<List<OrderListResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<OrderListResponse>>> call,
                    @NonNull Response<BaseResponse<List<OrderListResponse>>> response
            ) {
                if (!isAdded()) return;

                BaseResponse<List<OrderListResponse>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    txtPendingOrders.setText("0");
                    txtTodayRevenue.setText("0đ");
                    renderRecentOrders(new ArrayList<>());
                    return;
                }

                List<OrderListResponse> orders = body.getData() == null ? new ArrayList<>() : body.getData();
                cachedOrders.clear();
                cachedOrders.addAll(orders);
                txtPendingOrders.setText(String.valueOf(countPendingOrders(orders)));
                txtTodayRevenue.setText(formatMoney(calculateTodayRevenue(orders)));
                renderRecentOrders(orders);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<OrderListResponse>>> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded()) return;
                txtPendingOrders.setText("0");
                txtTodayRevenue.setText("0đ");
                renderRecentOrders(new ArrayList<>());
            }
        });
    }

    private void loadLowStock() {
        if (inventoryApiService == null) {
            loadLowStockFromProducts();
            return;
        }

        inventoryApiService.getSellerInventory(null, 5).enqueue(new Callback<BaseResponse<List<InventoryItemResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<InventoryItemResponse>>> call,
                    @NonNull Response<BaseResponse<List<InventoryItemResponse>>> response
            ) {
                if (!isAdded()) return;

                BaseResponse<List<InventoryItemResponse>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    loadLowStockFromProducts();
                    return;
                }

                int count = countLowStockItems(body.getData());
                if (count > 0) {
                    txtLowStockCount.setText(formatLowStockCount(count));
                    return;
                }

                loadLowStockFromProducts();
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<InventoryItemResponse>>> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded()) return;
                loadLowStockFromProducts();
            }
        });
    }

    private int countLowStockItems(List<InventoryItemResponse> items) {
        int count = 0;
        if (items == null) return 0;

        for (InventoryItemResponse item : items) {
            if (item == null) continue;

            if (item.isLowStock() || item.getStockQuantity() <= 5) {
                count++;
            }
        }

        return count;
    }

    private void loadLowStockFromProducts() {
        Long shopId = cachedShop == null ? null : cachedShop.getShopId();
        if (shopId == null || shopId <= 0) {
            txtLowStockCount.setText("00");
            return;
        }

        productApiService.getProductsByShop(shopId, 0, 100).enqueue(new Callback<BaseResponse<PageResponse<ProductResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<PageResponse<ProductResponse>>> call,
                    @NonNull Response<BaseResponse<PageResponse<ProductResponse>>> response
            ) {
                if (!isAdded()) return;

                BaseResponse<PageResponse<ProductResponse>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    txtLowStockCount.setText("00");
                    return;
                }

                int count = 0;
                List<ProductResponse> products = body.getData().getData();
                if (products != null) {
                    for (ProductResponse product : products) {
                        if (product == null || product.getVariants() == null) continue;

                        for (com.gr6.smartcart_android.seller.product.response.VariantResponse variant : product.getVariants()) {
                            if (variant != null && variant.getStockQuantity() <= 5) {
                                count++;
                            }
                        }
                    }
                }

                txtLowStockCount.setText(formatLowStockCount(count));
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<PageResponse<ProductResponse>>> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded()) return;
                txtLowStockCount.setText("00");
            }
        });
    }

    private String formatLowStockCount(int count) {
        return String.format(Locale.getDefault(), "%02d", Math.max(count, 0));
    }

    private void showTodayOrderStatsDialog() {
        int soldOrders = 0;
        int cancelledOrders = 0;
        BigDecimal todayRevenue = BigDecimal.ZERO;

        for (OrderListResponse order : cachedOrders) {
            if (order == null || !isToday(order.getCreatedAt())) continue;

            String status = normalizeStatus(order.getStatus());
            if ("CANCELLED".equals(status) || "PAYMENT_FAILED".equals(status)) {
                cancelledOrders++;
            } else if (isRevenueOrderStatus(status)) {
                soldOrders++;
                if (order.getTotalAmount() != null) {
                    todayRevenue = todayRevenue.add(order.getTotalAmount());
                }
            }
        }

        String message = "Số đơn hàng bán được: " + soldOrders
                + "\nSố đơn hàng hủy: " + cancelledOrders
                + "\nDoanh thu hôm nay: " + formatMoney(todayRevenue);

        new AlertDialog.Builder(requireContext())
                .setTitle("Chi tiết doanh thu hôm nay")
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void loadUnreadMessages() {
        if (chatApiService == null || txtUnreadMessageBadge == null) return;

        chatApiService.getConversations().enqueue(new Callback<BaseResponse<List<ConversationResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<ConversationResponse>>> call,
                    @NonNull Response<BaseResponse<List<ConversationResponse>>> response
            ) {
                if (!isAdded()) return;

                BaseResponse<List<ConversationResponse>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    updateUnreadBadge(0);
                    return;
                }

                int unread = 0;
                for (ConversationResponse conversation : body.getData()) {
                    if (conversation != null) {
                        unread += conversation.getUnreadCount();
                    }
                }
                updateUnreadBadge(unread);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<ConversationResponse>>> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded()) return;
                updateUnreadBadge(0);
            }
        });
    }

    private void updateUnreadBadge(int unread) {
        if (txtUnreadMessageBadge == null) return;

        if (unread <= 0) {
            txtUnreadMessageBadge.setVisibility(View.GONE);
            txtUnreadMessageBadge.setText("0");
            return;
        }

        txtUnreadMessageBadge.setVisibility(View.VISIBLE);
        txtUnreadMessageBadge.setText(unread > 99 ? "99+" : String.valueOf(unread));
    }

    private int countPendingOrders(List<OrderListResponse> orders) {
        int count = 0;
        for (OrderListResponse order : orders) {
            String status = normalizeStatus(order == null ? null : order.getStatus());
            if ("PENDING".equals(status) || "CONFIRMED".equals(status) || "PREPARING".equals(status)) {
                count++;
            }
        }
        return count;
    }

    private BigDecimal calculateTodayRevenue(List<OrderListResponse> orders) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderListResponse order : orders) {
            if (order == null || order.getTotalAmount() == null) continue;

            String status = normalizeStatus(order.getStatus());
            boolean revenueOrder = isRevenueOrderStatus(status);
            if (revenueOrder && isToday(order.getCreatedAt())) {
                total = total.add(order.getTotalAmount());
            }
        }
        return total;
    }


    private void setRatingText(String ratingText) {
        if (txtShopRating == null) return;

        String value = ratingText == null || ratingText.trim().isEmpty() ? "--" : ratingText.trim();
        String text = value + " ★";

        SpannableString spannable = new SpannableString(text);
        int starIndex = text.lastIndexOf("★");
        if (starIndex >= 0) {
            spannable.setSpan(
                    new ForegroundColorSpan(0xFFFFC107),
                    starIndex,
                    starIndex + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        txtShopRating.setText(spannable);
    }

    private String calculateAverageProductRating(List<ProductResponse> products) {
        if (products == null || products.isEmpty()) return "--";

        double weightedTotal = 0.0;
        int reviewTotal = 0;

        for (ProductResponse product : products) {
            if (product == null || product.getAverageRating() == null || product.getAverageRating() <= 0) {
                continue;
            }

            int reviewCount = Math.max(product.getReviewCount(), 0);
            if (reviewCount <= 0) {
                reviewCount = 1;
            }

            weightedTotal += product.getAverageRating() * reviewCount;
            reviewTotal += reviewCount;
        }

        if (reviewTotal == 0) return "--";
        return String.format(Locale.US, "%.1f", weightedTotal / reviewTotal);
    }

    private void renderRecentOrders(List<OrderListResponse> orders) {
        layoutRecentOrdersContainer.removeAllViews();

        if (orders == null || orders.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Chưa có đơn hàng gần đây");
            empty.setTextColor(color(R.color.text_secondary));
            empty.setTextSize(14);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setBackgroundResource(R.drawable.bg_seller_card);
            empty.setPadding(dp(16), dp(22), dp(16), dp(22));
            layoutRecentOrdersContainer.addView(empty, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            return;
        }

        int limit = Math.min(3, orders.size());
        for (int i = 0; i < limit; i++) {
            OrderListResponse order = orders.get(i);
            if (order == null) continue;
            layoutRecentOrdersContainer.addView(createOrderRow(order, i > 0));
        }
    }

    private View createOrderRow(OrderListResponse order, boolean hasTopMargin) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));
        row.setBackgroundResource(R.drawable.bg_seller_card);
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> openOrdersTab());

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(86)
        );
        if (hasTopMargin) {
            rowParams.setMargins(0, dp(12), 0, 0);
        }
        row.setLayoutParams(rowParams);

        ImageView image = new ImageView(requireContext());
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundResource(R.drawable.bg_seller_chip_soft);
        image.setPadding(dp(8), dp(8), dp(8), dp(8));
        row.addView(image, new LinearLayout.LayoutParams(dp(52), dp(52)));
        ImageLoader.load(requireContext(), order.getFirstProductImage(), image);

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        infoParams.setMargins(dp(14), 0, dp(10), 0);
        row.addView(info, infoParams);

        TextView code = new TextView(requireContext());
        code.setText(order.getOrderCode());
        code.setTextColor(color(R.color.text_primary));
        code.setTextSize(17);
        code.setTypeface(null, Typeface.BOLD);
        code.setSingleLine(true);
        info.addView(code);

        TextView sub = new TextView(requireContext());
        sub.setText(order.getFirstProductName() + " • " + formatMoney(order.getTotalAmount()));
        sub.setTextColor(color(R.color.text_secondary));
        sub.setTextSize(13);
        sub.setSingleLine(true);
        info.addView(sub);

        TextView status = new TextView(requireContext());
        status.setText(statusLabel(order.getStatus()));
        status.setTextColor(statusColor(order.getStatus()));
        status.setTextSize(12);
        status.setTypeface(null, Typeface.BOLD);
        status.setGravity(android.view.Gravity.CENTER);
        status.setBackgroundResource(R.drawable.bg_seller_chip_soft);
        status.setPadding(dp(10), 0, dp(10), 0);
        row.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(34)
        ));

        return row;
    }

    private void openOrdersTab() {
        if (getActivity() instanceof SellerHomeActivity) {
            ((SellerHomeActivity) getActivity()).openOrdersTab();
        }
    }

    private void openInventoryTab() {
        if (getActivity() instanceof SellerHomeActivity) {
            ((SellerHomeActivity) getActivity()).openInventoryFromDashboard();
        }
    }

    private void openLowStockInventoryTab() {
        if (getActivity() instanceof SellerHomeActivity) {
            ((SellerHomeActivity) getActivity()).openLowStockInventoryFromDashboard();
        }
    }

    private void openChatList() {
        if (getActivity() instanceof SellerHomeActivity) {
            ((SellerHomeActivity) getActivity()).openChatList();
        }
    }

    private boolean isToday(String value) {
        Date date = parseDate(value);
        if (date == null) return false;

        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTime(date);

        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
    }

    private Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;

        String raw = value.trim();
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };

        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.US).parse(raw);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private String formatMoney(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(safeValue) + "đ";
    }

    private boolean isRevenueOrderStatus(String status) {
        String s = normalizeStatus(status);
        return "PENDING".equals(s)
                || "CONFIRMED".equals(s)
                || "PREPARING".equals(s)
                || "SHIPPING".equals(s)
                || "DELIVERED".equals(s)
                || "COMPLETED".equals(s);
    }

    private String statusLabel(String status) {
        String s = normalizeStatus(status);

        if ("ACTIVE".equals(s)) return "ACTIVE";
        if ("PENDING".equals(s)) return "Chờ xác nhận";
        if ("PENDING_PAYMENT".equals(s)) return "Chờ thanh toán";
        if ("CONFIRMED".equals(s)) return "Đã xác nhận";
        if ("PREPARING".equals(s)) return "Đang chuẩn bị";
        if ("SHIPPING".equals(s)) return "Đang giao";
        if ("DELIVERED".equals(s)) return "Đã giao";
        if ("COMPLETED".equals(s)) return "Hoàn thành";
        if ("CANCELLED".equals(s)) return "Đã hủy";
        if ("PAYMENT_FAILED".equals(s)) return "Thanh toán lỗi";
        if ("REJECTED".equals(s)) return "Bị từ chối";
        if ("BANNED".equals(s)) return "Bị khóa";
        if ("".equals(s)) return "Chờ duyệt";

        return s;
    }

    private int statusColor(String status) {
        String s = normalizeStatus(status);

        if ("ACTIVE".equals(s) || "DELIVERED".equals(s) || "COMPLETED".equals(s)) {
            return color(R.color.success);
        }
        if ("CANCELLED".equals(s) || "PAYMENT_FAILED".equals(s) || "REJECTED".equals(s) || "BANNED".equals(s)) {
            return color(R.color.danger);
        }
        return color(R.color.brand_primary);
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        return status.trim().toUpperCase();
    }

    private int color(int colorId) {
        return ContextCompat.getColor(requireContext(), colorId);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}




