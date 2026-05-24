package com.gr6.smartcart_android.buyer.category;

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

public class CategoryProductAdapter
        extends RecyclerView.Adapter<CategoryProductAdapter.ProductVH> {

    public interface OnProductClickListener {
        void onProductClick(HomeProductResponse product);
    }

    private final List<HomeProductResponse> products = new ArrayList<>();
    private final NumberFormat numberFormat =
            NumberFormat.getInstance(new Locale("vi", "VN"));

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

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductVH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_product_grid, parent, false);

        return new ProductVH(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductVH holder,
            int position
    ) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductVH extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;
        private final TextView txtProductName;
        private final TextView txtRatingSold;
        private final TextView txtPrice;
        private final TextView txtShopName;

        ProductVH(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtRatingSold = itemView.findViewById(R.id.txtRatingSold);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtShopName = itemView.findViewById(R.id.txtShopName);
        }

        void bind(HomeProductResponse product) {
            if (product == null) return;

            txtProductName.setText(product.getProductName());
            txtPrice.setText(formatMoney(product.getDisplayPrice()));

            txtRatingSold.setText(
                    "★ " + formatRating(product.getAverageRating())
                            + "  |  Đã bán " + formatSold(product.getSoldQuantity())
            );

            txtShopName.setText(product.getShopName());

            String imageUrl = product.getImageUrl();

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            } else {
                ImageLoader.load(itemView.getContext(), imageUrl, imgProduct);
            }

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
            return String.format(Locale.getDefault(), "%.1fk", sold / 1000.0);
        }

        return String.valueOf(sold);
    }
}