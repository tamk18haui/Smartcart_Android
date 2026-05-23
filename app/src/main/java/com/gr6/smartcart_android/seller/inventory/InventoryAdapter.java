package com.gr6.smartcart_android.seller.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.seller.inventory.response.InventoryItemResponse;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    public interface OnStockClickListener {
        void onIncrease(InventoryItemResponse item);
        void onDecrease(InventoryItemResponse item);
    }

    private final List<InventoryItemResponse> items = new ArrayList<>();
    private final OnStockClickListener listener;

    public InventoryAdapter(OnStockClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<InventoryItemResponse> newItems) {
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
                .inflate(R.layout.item_seller_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItemResponse item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class InventoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgProduct;
        private final TextView txtName;
        private final TextView txtVariant;
        private final TextView txtStock;
        private final TextView txtStockNumber;
        private final ImageButton btnMinus;
        private final ImageButton btnPlus;

        InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtProductName);
            txtVariant = itemView.findViewById(R.id.txtVariant);
            txtStock = itemView.findViewById(R.id.txtStock);
            txtStockNumber = itemView.findViewById(R.id.txtStockNumber);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }

        void bind(InventoryItemResponse item) {
            txtName.setText(nonEmpty(item.getProductName(), "Sản phẩm"));
            txtVariant.setText(nonEmpty(item.getVariantName(), nonEmpty(item.getSku(), "Phân loại mặc định")));
            txtStock.setText("Tồn kho: " + item.getStockQuantity());
            txtStockNumber.setText(String.valueOf(item.getStockQuantity()));

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .centerCrop()
                    .into(imgProduct);

            btnPlus.setOnClickListener(v -> {
                if (listener != null) listener.onIncrease(item);
            });

            btnMinus.setEnabled(item.getStockQuantity() > 0);
            btnMinus.setAlpha(item.getStockQuantity() > 0 ? 1f : 0.45f);
            btnMinus.setOnClickListener(v -> {
                if (listener != null && item.getStockQuantity() > 0) listener.onDecrease(item);
            });
        }
    }

    private String nonEmpty(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        return value.trim();
    }
}


