package com.gr6.smartcart_android.seller.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.seller.order.response.OrderListResponse;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SellerOrderAdapter extends RecyclerView.Adapter<SellerOrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(OrderListResponse order);

        void onPrimaryAction(OrderListResponse order);

        void onPrintShippingLabel(OrderListResponse order);
    }

    private final List<OrderListResponse> orders = new ArrayList<>();
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    private OnOrderClickListener listener;

    public void setListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<OrderListResponse> newOrders) {
        orders.clear();

        if (newOrders != null) {
            orders.addAll(newOrders);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seller_order, parent, false);

        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull OrderViewHolder holder,
            int position
    ) {
        OrderListResponse order = orders.get(position);

        if (order == null) {
            return;
        }

        String status = OrderStatusHelper.normalize(order.getStatus());

        holder.txtOrderCode.setText(order.getOrderCode());
        holder.txtTime.setText(formatTime(order.getCreatedAt()));
        holder.txtStatus.setText(OrderStatusHelper.label(status));
        holder.txtProductName.setText(order.getFirstProductName());

        String variantName = order.getFirstVariantName();
        if (variantName == null || variantName.trim().isEmpty()) {
            holder.txtVariant.setText("Phân loại: mặc định");
        } else {
            holder.txtVariant.setText("Phân loại: " + variantName.trim());
        }

        holder.txtTotal.setText(formatMoney(order.getTotalAmount()));
        holder.btnAction.setText(OrderStatusHelper.nextActionLabel(status));

        boolean canAction = OrderStatusHelper.canSellerQuickAction(status);
        holder.btnAction.setAlpha(canAction ? 1f : 0.85f);

        boolean canPrintLabel = canPrintShippingLabel(status);
        holder.btnPrintLabel.setVisibility(canPrintLabel ? View.VISIBLE : View.GONE);
        holder.btnPrintLabel.setText("In mã vận đơn");

        ImageLoader.load(
                holder.itemView.getContext(),
                order.getFirstProductImage(),
                holder.imgProduct
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPrimaryAction(order);
            }
        });

        holder.btnPrintLabel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPrintShippingLabel(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private boolean canPrintShippingLabel(String status) {
        String normalized = OrderStatusHelper.normalize(status);

        // Chỉ cho in mã vận đơn khi shop đã xác nhận đơn.
        // Từ trạng thái ĐANG GIAO trở đi thì không được in nữa.
        return "CONFIRMED".equals(normalized);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0đ";
        return moneyFormat.format(value) + "đ";
    }

    private String formatTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";

        String value = raw.replace('T', ' ');

        int dotIndex = value.indexOf('.');
        if (dotIndex > 0) {
            value = value.substring(0, dotIndex);
        }

        if (value.length() >= 16) {
            return value.substring(0, 16);
        }

        return value;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView txtOrderCode;
        TextView txtTime;
        TextView txtStatus;
        ImageView imgProduct;
        TextView txtProductName;
        TextView txtVariant;
        TextView txtTotal;
        TextView btnPrintLabel;
        TextView btnAction;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            txtOrderCode = itemView.findViewById(R.id.txtOrderCode);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtVariant = itemView.findViewById(R.id.txtVariant);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            btnPrintLabel = itemView.findViewById(R.id.btnPrintLabel);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}


