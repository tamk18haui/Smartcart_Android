package com.gr6.smartcart_android.seller.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.seller.inventory.model.InventoryVariantItem;

import java.util.ArrayList;
import java.util.List;

public class LowStockAdapter extends RecyclerView.Adapter<LowStockAdapter.LowStockViewHolder> {

    public interface OnLowStockClickListener {
        void onClick(InventoryVariantItem item);
    }

    private final List<InventoryVariantItem> items = new ArrayList<>();
    private OnLowStockClickListener listener;

    public void setListener(OnLowStockClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<InventoryVariantItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LowStockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_low_stock, parent, false);
        return new LowStockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LowStockViewHolder holder, int position) {
        InventoryVariantItem item = items.get(position);
        holder.txtName.setText(item.getProductName());
        holder.txtStock.setText("Chỉ còn " + item.getStock() + " sp");
        ImageLoader.load(holder.itemView.getContext(), item.getDisplayImage(), holder.imgProduct);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LowStockViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName;
        TextView txtStock;

        LowStockViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtStock = itemView.findViewById(R.id.txtStock);
        }
    }
}
