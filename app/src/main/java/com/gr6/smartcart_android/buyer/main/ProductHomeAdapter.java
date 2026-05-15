package com.gr6.smartcart_android.buyer.main;

import android.graphics.Paint;
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

public class ProductHomeAdapter extends RecyclerView.Adapter<ProductHomeAdapter.ProductViewHolder> {

    private final List<HomeProductResponse> products = new ArrayList<>();
    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private OnProductClickListener listener;

    public void setData(List<HomeProductResponse> data) {
        products.clear();

        if (data != null) {
            products.addAll(data);
        }

        notifyDataSetChanged();
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
                .inflate(R.layout.item_home_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder,
            int position
    ) {
        HomeProductResponse item = products.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;
        private final TextView txtProductName;
        private final TextView txtPrice;
        private final TextView txtOriginalPrice;
        private final TextView txtSold;
        private final TextView txtRating;
        private final TextView txtShopName;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtOriginalPrice = itemView.findViewById(R.id.txtOriginalPrice);
            txtSold = itemView.findViewById(R.id.txtSold);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtShopName = itemView.findViewById(R.id.txtShopName);
        }

        void bind(HomeProductResponse item) {
            ImageLoader.load(itemView.getContext(), item.getImageUrl(), imgProduct);

            txtProductName.setText(item.getProductName());
            txtPrice.setText(formatMoney(item.getDisplayPrice()));
            txtSold.setText("Đã bán " + item.getSoldQuantity());
            txtRating.setText(String.format(Locale.getDefault(), "%.1f", item.getAverageRating()));
            txtShopName.setText(item.getShopName());

            BigDecimal originalPrice = item.getOriginalPrice();
            if (originalPrice != null && originalPrice.compareTo(item.getDisplayPrice()) > 0) {
                txtOriginalPrice.setVisibility(View.VISIBLE);
                txtOriginalPrice.setText(formatMoney(originalPrice));
                txtOriginalPrice.setPaintFlags(
                        txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                );
            } else {
                txtOriginalPrice.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(item);
                }
            });
        }

        private String formatMoney(BigDecimal value) {
            if (value == null) return "0 ₫";
            return currencyFormat.format(value);
        }
    }

    public interface OnProductClickListener {
        void onProductClick(HomeProductResponse product);
    }
}