package com.gr6.smartcart_android.seller.inventory;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.seller.inventory.model.InventoryHistoryItem;

import java.util.ArrayList;
import java.util.List;

public class StockHistoryAdapter extends RecyclerView.Adapter<StockHistoryAdapter.HistoryViewHolder> {

    private final List<InventoryHistoryItem> items = new ArrayList<>();

    public void addFirst(InventoryHistoryItem item) {
        if (item == null) return;
        items.add(0, item);
        notifyItemInserted(0);
    }

    public void submitList(List<InventoryHistoryItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        InventoryHistoryItem item = items.get(position);

        String title;
        int iconRes;
        int deltaColor;

        if (item.getType() == InventoryHistoryItem.Type.IMPORT) {
            title = "Nhập kho: ";
            iconRes = R.drawable.ic_seller_products;
            deltaColor = Color.rgb(22, 163, 74);
        } else if (item.getType() == InventoryHistoryItem.Type.EXPORT) {
            title = "Xuất kho: ";
            iconRes = R.drawable.ic_cart;
            deltaColor = Color.rgb(220, 38, 38);
        } else {
            title = "Điều chỉnh: ";
            iconRes = R.drawable.ic_edit;
            deltaColor = Color.rgb(11, 116, 229);
        }

        holder.imgIcon.setImageResource(iconRes);
        holder.txtTitle.setText(title + item.getProductName());
        holder.txtMeta.setText(item.getTimeText() + " • " + item.getSourceText());
        holder.txtDelta.setText((item.getQuantityDelta() > 0 ? "+" : "") + item.getQuantityDelta());
        holder.txtDelta.setTextColor(deltaColor);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView txtTitle;
        TextView txtMeta;
        TextView txtDelta;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtMeta = itemView.findViewById(R.id.txtMeta);
            txtDelta = itemView.findViewById(R.id.txtDelta);
        }
    }
}
