package com.gr6.smartcart_android.buyer.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.main.response.HomeProductResponse;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchProductAdapter extends RecyclerView.Adapter<SearchProductAdapter.ProductViewHolder> {

    private final List<HomeProductResponse> products = new ArrayList<>();
    private final NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    private OnProductClickListener listener;

    public void setData(List<HomeProductResponse> data) {
        products.clear();

        if (data != null) {
            products.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void appendData(List<HomeProductResponse> data) {
        if (data == null || data.isEmpty()) return;

        int start = products.size();
        products.addAll(data);
        notifyItemRangeInserted(start, data.size());
    }

    public void clear() {
        products.clear();
        notifyDataSetChanged();
    }

    public int getDataSize() {
        return products.size();
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_product, parent, false);

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder,
            int position
    ) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;
        private final TextView txtName;
        private final TextView txtPrice;
        private final TextView txtMeta;
        private final TextView txtShopName;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtMeta = itemView.findViewById(R.id.txtMeta);
            txtShopName = itemView.findViewById(R.id.txtShopName);
        }

        void bind(HomeProductResponse product) {
            if (product == null) return;

            txtName.setText(product.getProductName());
            txtPrice.setText(formatMoney(product.getDisplayPrice()));

            txtMeta.setText(formatRating(product.getAverageRating())
                            + " | Đã bán " + formatSold(product.getSoldQuantity())
            );

            txtShopName.setText(product.getShopName());

            ImageLoader.load(itemView.getContext(), product.getImageUrl(), imgProduct);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0đ";
        return numberFormat.format(value) + "đ";
    }

    private String formatRating(Double value) {
        if (value == null) return "0.0";
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    private String formatSold(Integer value) {
        int sold = value == null ? 0 : Math.max(value, 0);

        if (sold >= 1000) {
            double k = sold / 1000.0;
            return String.format(Locale.getDefault(), "%.1fk", k);
        }

        return String.valueOf(sold);
    }

    public interface OnProductClickListener {
        void onProductClick(HomeProductResponse product);
    }
}