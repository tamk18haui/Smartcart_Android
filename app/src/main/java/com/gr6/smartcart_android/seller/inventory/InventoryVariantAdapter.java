package com.gr6.smartcart_android.seller.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.seller.inventory.model.InventoryVariantItem;

import java.util.ArrayList;
import java.util.List;

public class InventoryVariantAdapter extends RecyclerView.Adapter<InventoryVariantAdapter.InventoryViewHolder> {

    public interface OnStockActionListener {
        void onIncrease(InventoryVariantItem item);
        void onDecrease(InventoryVariantItem item);
    }

    private final List<InventoryVariantItem> items = new ArrayList<>();
    private OnStockActionListener listener;

    public void setListener(OnStockActionListener listener) {
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
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_variant, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryVariantItem item = items.get(position);

        holder.txtName.setText(item.getProductName());
        holder.txtVariant.setText(item.getVariant() == null ? "Phân loại mặc định" : item.getVariant().getAttributeText());
        holder.txtStock.setText("Tồn kho: " + item.getStock());
        holder.txtQuantity.setText(String.valueOf(item.getStock()));
        ImageLoader.load(holder.itemView.getContext(), item.getDisplayImage(), holder.imgProduct);

        holder.btnPlus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIncrease(item);
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecrease(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName;
        TextView txtVariant;
        TextView txtStock;
        TextView txtQuantity;
        ImageButton btnMinus;
        ImageButton btnPlus;

        InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtVariant = itemView.findViewById(R.id.txtVariant);
            txtStock = itemView.findViewById(R.id.txtStock);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}
