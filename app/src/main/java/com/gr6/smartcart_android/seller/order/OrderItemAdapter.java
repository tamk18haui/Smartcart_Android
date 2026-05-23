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
import com.gr6.smartcart_android.seller.order.response.OrderItemResponse;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ItemViewHolder> {

    private final List<OrderItemResponse> items = new ArrayList<>();
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    public void submitList(List<OrderItemResponse> newItems) {
        items.clear();

        if (newItems != null) {
            items.addAll(newItems);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seller_order_detail_product, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        OrderItemResponse item = items.get(position);

        holder.txtName.setText(item.getProductName());

        String variantName = item.getVariantName();
        holder.txtVariant.setText(
                variantName == null || variantName.trim().isEmpty()
                        ? "Phân loại: mặc định"
                        : "Phân loại: " + variantName
        );

        holder.txtQuantity.setText("Số lượng: x" + item.getQuantity());
        holder.txtPrice.setText(formatMoney(item.getPrice()));

        ImageLoader.load(
                holder.itemView.getContext(),
                item.getImageUrl(),
                holder.imgProduct
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0đ";
        return moneyFormat.format(value) + "đ";
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtName;
        TextView txtVariant;
        TextView txtQuantity;
        TextView txtPrice;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtVariant = itemView.findViewById(R.id.txtVariant);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
}