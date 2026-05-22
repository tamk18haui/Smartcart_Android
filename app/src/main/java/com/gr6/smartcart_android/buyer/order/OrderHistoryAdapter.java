package com.gr6.smartcart_android.buyer.order;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderVH> {

    public interface OnOrderActionListener {
        void onCancelClick(OrderHistoryUiModel order);
    }

    private final Context context;
    private final List<OrderHistoryUiModel> orders = new ArrayList<>();
    private OnOrderActionListener actionListener;

    public OrderHistoryAdapter(Context context) {
        this.context = context;
    }

    public void setOnOrderActionListener(OnOrderActionListener listener) {
        this.actionListener = listener;
    }

    public void submitList(List<OrderHistoryUiModel> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderVH holder, int position) {
        OrderHistoryUiModel order = orders.get(position);

        holder.txtOrderCode.setText(
                "Order #" + safeId(order.getOrderId()) + " · Shop #" + safeId(order.getShopId())
        );

        holder.txtShopName.setText(order.getShopName());
        holder.txtCreatedAt.setText("Đặt lúc " + order.getCreatedAt());

        int productCount = order.getItems() == null ? 0 : order.getItems().size();
        int totalQuantity = order.getTotalQuantity();

        holder.txtProductSummary.setText(
                productCount + " sản phẩm · Tổng số lượng: " + totalQuantity
        );

        holder.txtTotalAmount.setText(formatVnd(order.getTotalAmount()));

        holder.txtStatus.setText(getDisplayStatus(order.getStatus()));
        applyStatusStyle(holder.txtStatus, order.getStatus());

        bindItems(holder.layoutItems, order.getItems());
        bindCancelButton(holder.btnCancelOrder, order);

        holder.btnDetail.setOnClickListener(v -> {
            if (order.getShopOrderId() == null || order.getShopOrderId() <= 0) {
                Toast.makeText(context, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra(OrderDetailActivity.EXTRA_SHOP_ORDER_ID, order.getShopOrderId());
            context.startActivity(intent);
        });

        holder.btnBuyAgain.setOnClickListener(v ->
                Toast.makeText(
                        context,
                        "Mua lại orderId = " + order.getOrderId(),
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private void bindCancelButton(TextView button, OrderHistoryUiModel order) {
        boolean canCancel = order.canCancel();

        button.setEnabled(canCancel);
        button.setAlpha(canCancel ? 1f : 0.42f);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(999));

        if (canCancel) {
            drawable.setColor(ContextCompat.getColor(context, R.color.danger_light));
            drawable.setStroke(dp(1), ContextCompat.getColor(context, R.color.danger));
            button.setTextColor(ContextCompat.getColor(context, R.color.danger));
        } else {
            drawable.setColor(ContextCompat.getColor(context, R.color.surface_soft));
            drawable.setStroke(dp(1), ContextCompat.getColor(context, R.color.border));
            button.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }

        button.setBackground(drawable);

        button.setOnClickListener(v -> {
            if (!order.canCancel()) return;

            if (actionListener != null) {
                actionListener.onCancelClick(order);
            }
        });
    }

    private void bindItems(
            LinearLayout layoutItems,
            List<OrderHistoryUiModel.OrderItemUiModel> items
    ) {
        layoutItems.removeAllViews();

        if (items == null || items.isEmpty()) {
            layoutItems.setVisibility(View.GONE);
            return;
        }

        layoutItems.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(context);

        for (OrderHistoryUiModel.OrderItemUiModel item : items) {
            View row = inflater.inflate(
                    R.layout.item_order_history_product,
                    layoutItems,
                    false
            );

            ImageView imgProduct = row.findViewById(R.id.imgProduct);
            TextView txtProductName = row.findViewById(R.id.txtProductName);
            TextView txtVariantSku = row.findViewById(R.id.txtVariantSku);
            TextView txtItemPrice = row.findViewById(R.id.txtItemPrice);
            TextView txtQuantity = row.findViewById(R.id.txtQuantity);

            txtProductName.setText(item.getProductName());
            txtVariantSku.setText(item.getVariantSku());
            txtItemPrice.setText(formatVnd(item.getPriceAtPurchase()));
            txtQuantity.setText("x" + item.getQuantity());

            ImageLoader.load(context, item.getImageUrl(), imgProduct);

            layoutItems.addView(row);
        }
    }

    private String safeId(Long id) {
        return id == null ? "--" : String.valueOf(id);
    }

    private String formatVnd(long value) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(value) + "đ";
    }

    private String getDisplayStatus(String status) {
        if (status == null) return "Không rõ";

        switch (status) {
            case "PENDING_PAYMENT":
                return "Chờ thanh toán";
            case "PENDING":
                return "Chờ xác nhận";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "PREPARING":
                return "Đang chuẩn bị";
            case "SHIPPING":
                return "Đang giao";
            case "DELIVERED":
            case "COMPLETED":
                return "Hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            case "PAYMENT_FAILED":
                return "Thanh toán lỗi";
            default:
                return status;
        }
    }

    private void applyStatusStyle(TextView textView, String status) {
        int bgColor;
        int textColor;

        if ("COMPLETED".equals(status) || "DELIVERED".equals(status)) {
            bgColor = ContextCompat.getColor(context, R.color.success_light);
            textColor = ContextCompat.getColor(context, R.color.success);
        } else if ("CANCELLED".equals(status) || "PAYMENT_FAILED".equals(status)) {
            bgColor = ContextCompat.getColor(context, R.color.danger_light);
            textColor = ContextCompat.getColor(context, R.color.danger);
        } else if ("SHIPPING".equals(status)) {
            bgColor = ContextCompat.getColor(context, R.color.warning_light);
            textColor = ContextCompat.getColor(context, R.color.warning);
        } else {
            bgColor = ContextCompat.getColor(context, R.color.brand_primary_light);
            textColor = ContextCompat.getColor(context, R.color.brand_primary);
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(bgColor);
        drawable.setCornerRadius(dp(999));

        textView.setBackground(drawable);
        textView.setTextColor(textColor);
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    static class OrderVH extends RecyclerView.ViewHolder {

        TextView txtOrderCode;
        TextView txtShopName;
        TextView txtStatus;
        TextView txtCreatedAt;
        TextView txtProductSummary;
        TextView txtTotalAmount;
        TextView btnDetail;
        TextView btnBuyAgain;
        TextView btnCancelOrder;
        LinearLayout layoutItems;

        public OrderVH(@NonNull View itemView) {
            super(itemView);

            txtOrderCode = itemView.findViewById(R.id.txtOrderCode);
            txtShopName = itemView.findViewById(R.id.txtShopName);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtCreatedAt = itemView.findViewById(R.id.txtCreatedAt);
            txtProductSummary = itemView.findViewById(R.id.txtProductSummary);
            txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            btnBuyAgain = itemView.findViewById(R.id.btnBuyAgain);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
            layoutItems = itemView.findViewById(R.id.layoutItems);
        }
    }
}