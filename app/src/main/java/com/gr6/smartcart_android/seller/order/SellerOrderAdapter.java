package com.gr6.smartcart_android.seller.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.seller.order.model.OrderListResponse;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SellerOrderAdapter extends RecyclerView.Adapter<SellerOrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(OrderListResponse order);
        void onPrimaryAction(OrderListResponse order);
    }

    private final List<OrderListResponse> orders = new ArrayList<>();
    private OnOrderClickListener listener;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

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
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seller_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderListResponse order = orders.get(position);

        holder.txtOrderCode.setText(order.getOrderCode());
        holder.txtTime.setText(order.getCreatedAt() == null ? "" : order.getCreatedAt());
        holder.txtStatus.setText(OrderStatusHelper.label(order.getStatus()));
        holder.txtProductName.setText(order.getFirstProductName());
        holder.txtVariant.setText(order.getFirstVariantName().isEmpty() ? "Phân loại: mặc định" : "Phân loại: " + order.getFirstVariantName());
        holder.txtTotal.setText(formatMoney(order.getTotalAmount()));
        holder.btnAction.setText(OrderStatusHelper.nextActionLabel(order.getStatus()));

        ImageLoader.load(holder.itemView.getContext(), order.getFirstProductImage(), holder.imgProduct);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) listener.onPrimaryAction(order);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private String formatMoney(java.math.BigDecimal value) {
        if (value == null) return "0đ";
        return moneyFormat.format(value) + "đ";
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderCode;
        TextView txtTime;
        TextView txtStatus;
        ImageView imgProduct;
        TextView txtProductName;
        TextView txtVariant;
        TextView txtTotal;
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
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
