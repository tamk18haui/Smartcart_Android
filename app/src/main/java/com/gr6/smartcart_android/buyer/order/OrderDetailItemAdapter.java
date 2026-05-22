package com.gr6.smartcart_android.buyer.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.order.response.OrderDetailResponse;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailItemAdapter extends RecyclerView.Adapter<OrderDetailItemAdapter.ItemViewHolder> {

    private final List<OrderDetailResponse.OrderItemResponse> items = new ArrayList<>();
    private final NumberFormat moneyFormat =
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public void setData(List<OrderDetailResponse.OrderItemResponse> data) {
        items.clear();

        if (data != null) {
            items.addAll(data);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail_product, parent, false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ItemViewHolder holder,
            int position
    ) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;
        private final TextView txtProductName;
        private final TextView txtVariantSku;
        private final TextView txtQuantity;
        private final TextView txtPrice;
        private final TextView txtLineTotal;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtVariantSku = itemView.findViewById(R.id.txtVariantSku);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtLineTotal = itemView.findViewById(R.id.txtLineTotal);
        }

        void bind(OrderDetailResponse.OrderItemResponse item) {
            if (item == null) return;

            txtProductName.setText(item.getProductName());
            txtVariantSku.setText("Phân loại: " + item.getVariantSku());
            txtQuantity.setText("x" + item.getQuantity());
            txtPrice.setText(formatMoney(item.getPriceAtPurchase()));
            txtLineTotal.setText(formatMoney(item.getLineTotal()));

            if (item.getImageUrl().isEmpty()) {
                imgProduct.setImageResource(R.drawable.ic_cart);
            } else {
                ImageLoader.load(itemView.getContext(), item.getImageUrl(), imgProduct);
            }
        }

        private String formatMoney(Long value) {
            if (value == null) value = 0L;
            return moneyFormat.format(value);
        }
    }
}