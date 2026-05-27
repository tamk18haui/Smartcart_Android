package com.gr6.smartcart_android.buyer.order;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import androidx.core.content.ContextCompat;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderVH> {

    public interface OnOrderActionListener {
        void onOrderClick(OrderHistoryUiModel order);

        void onCancelClick(OrderHistoryUiModel order);

        void onReviewClick(OrderHistoryUiModel order);

        void onBuyAgainClick(OrderHistoryUiModel order);

        void onCompleteClick(OrderHistoryUiModel order);

        void onPayAgainClick(OrderHistoryUiModel order);
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

        holder.txtOrderCode.setText("SC-" + safeId(order.getShopOrderId()));
        holder.txtShopName.setText(order.getShopName());
        holder.txtCreatedAt.setText("Đặt lúc " + order.getCreatedAt());

        int productCount = order.getItems() == null ? 0 : order.getItems().size();
        int totalQuantity = order.getTotalQuantity();

        holder.txtProductSummary.setText(
                productCount + " sản phẩm · Tổng số lượng: " + totalQuantity
        );

        holder.txtTotalAmount.setText(formatVnd(order.getTotalAmount()));

        holder.txtStatus.setText(getDisplayStatus(order.getStatus(), order.getPaymentStatus()));
        applyStatusStyle(holder.txtStatus, order.getStatus(), order.getPaymentStatus());


        bindItems(holder.layoutItems, order.getItems());
        bindButtons(holder, order);

        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private void bindButtons(OrderVH holder, OrderHistoryUiModel order) {
        String status = normalizeStatus(order.getStatus());

        boolean canCancel = order.canCancel();
        boolean canComplete = "DELIVERED".equals(status);
        boolean canPayAgain = "PENDING_PAYMENT".equals(status);
        boolean completed = "COMPLETED".equals(status);

        boolean canBuyAgain = "COMPLETED".equals(status)
                || "CANCELLED".equals(status)
                || "PAYMENT_FAILED".equals(status);

        holder.btnCancelOrder.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        holder.btnCompleteOrder.setVisibility(canComplete ? View.VISIBLE : View.GONE);
        holder.btnPayAgain.setVisibility(canPayAgain ? View.VISIBLE : View.GONE);
        holder.btnBuyAgain.setVisibility(canBuyAgain ? View.VISIBLE : View.GONE);

        bindReviewButton(holder, order, completed);

        holder.btnCancelOrder.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onCancelClick(order);
            }
        });

        holder.btnCompleteOrder.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onCompleteClick(order);
            }
        });

        holder.btnPayAgain.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onPayAgainClick(order);
            }
        });

        holder.btnBuyAgain.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onBuyAgainClick(order);
            }
        });
    }

    private void bindReviewButton(
            OrderVH holder,
            OrderHistoryUiModel order,
            boolean completed
    ) {
        holder.btnReview.setVisibility(View.GONE);
        holder.btnReview.setEnabled(false);
        holder.btnReview.setAlpha(1f);
        holder.btnReview.setOnClickListener(null);

        if (!completed) {
            return;
        }

        boolean allReviewed = order.isAllReviewed();

        if (allReviewed) {
            holder.btnReview.setVisibility(View.VISIBLE);
            holder.btnReview.setText("Đã đánh giá");
            holder.btnReview.setEnabled(false);
            holder.btnReview.setAlpha(0.75f);
            holder.btnReview.setTextColor(
                    ContextCompat.getColor(context, R.color.review_disabled_text)
            );
            holder.btnReview.setBackgroundResource(R.drawable.bg_review_disabled);
            return;
        }

        holder.btnReview.setVisibility(View.VISIBLE);
        holder.btnReview.setText("Đánh giá");
        holder.btnReview.setEnabled(true);
        holder.btnReview.setAlpha(1f);
        holder.btnReview.setTextColor(Color.parseColor("#EE4D2D"));
        holder.btnReview.setBackgroundResource(R.drawable.bg_role_unselected);

        holder.btnReview.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onReviewClick(order);
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
            if (item == null) continue;

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

            if (item.getVariantSku() == null || item.getVariantSku().trim().isEmpty()) {
                txtVariantSku.setVisibility(View.GONE);
            } else {
                txtVariantSku.setVisibility(View.VISIBLE);
                txtVariantSku.setText(item.getVariantSku());
            }

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

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }

    private String getDisplayStatus(String status, String paymentStatus) {
        String s = normalizeStatus(status);
        String p = normalizeStatus(paymentStatus);

        if ("PENDING".equals(s) && "COMPLETED".equals(p)) {
            return "Đã thanh toán - Chờ xác nhận";
        }

        if ("PENDING_PAYMENT".equals(s)) {
            return "Chờ thanh toán";
        }

        if ("PAYMENT_FAILED".equals(s) || "FAILED".equals(p)) {
            return "Thanh toán lỗi";
        }

        switch (s) {
            case "PENDING":
                return "Chờ xác nhận";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "PREPARING":
                return "Đang chuẩn bị";
            case "SHIPPING":
                return "Đang giao";
            case "DELIVERED":
                return "Đã giao";
            case "COMPLETED":
                return "Hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return "Không rõ";
        }
    }
    private void applyStatusStyle(TextView textView, String status, String paymentStatus) {
        String s = normalizeStatus(status);
        String p = normalizeStatus(paymentStatus);

        int bgColor;
        int textColor;

        if ("PENDING".equals(s) && "COMPLETED".equals(p)) {
            bgColor = Color.parseColor("#E8F8EF");
            textColor = Color.parseColor("#16A34A");
        } else if ("COMPLETED".equals(s) || "DELIVERED".equals(s)) {
            bgColor = Color.parseColor("#E8F8EF");
            textColor = Color.parseColor("#16A34A");
        } else if ("CANCELLED".equals(s) || "PAYMENT_FAILED".equals(s) || "FAILED".equals(p)) {
            bgColor = Color.parseColor("#FEECEC");
            textColor = Color.parseColor("#DC2626");
        } else if ("SHIPPING".equals(s)) {
            bgColor = Color.parseColor("#FFF7E6");
            textColor = Color.parseColor("#D97706");
        } else {
            bgColor = Color.parseColor("#FFF1ED");
            textColor = Color.parseColor("#EE4D2D");
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

        TextView btnCancelOrder;
        TextView btnCompleteOrder;
        TextView btnPayAgain;
        TextView btnReview;
        TextView btnBuyAgain;

        LinearLayout layoutItems;

        public OrderVH(@NonNull View itemView) {
            super(itemView);

            txtOrderCode = itemView.findViewById(R.id.txtOrderCode);
            txtShopName = itemView.findViewById(R.id.txtShopName);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtCreatedAt = itemView.findViewById(R.id.txtCreatedAt);
            txtProductSummary = itemView.findViewById(R.id.txtProductSummary);
            txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);

            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
            btnCompleteOrder = itemView.findViewById(R.id.btnCompleteOrder);
            btnPayAgain = itemView.findViewById(R.id.btnPayAgain);
            btnReview = itemView.findViewById(R.id.btnReview);
            btnBuyAgain = itemView.findViewById(R.id.btnBuyAgain);

            layoutItems = itemView.findViewById(R.id.layoutItems);
        }
    }
}