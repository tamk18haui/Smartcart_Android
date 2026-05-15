package com.gr6.smartcart_android.buyer.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.cart.response.CartDetailResponse;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartShopAdapter extends RecyclerView.Adapter<CartShopAdapter.ShopViewHolder> {

    private final List<CartDetailResponse.ShopCart> shops = new ArrayList<>();
    private CartListener listener;

    public void setData(List<CartDetailResponse.ShopCart> data) {
        shops.clear();

        if (data != null) {
            shops.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void setListener(CartListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        holder.bind(shops.get(position));
    }

    @Override
    public int getItemCount() {
        return shops.size();
    }

    class ShopViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox cbShop;
        private final TextView txtShopName;
        private final TextView txtShopTotal;
        private final LinearLayout layoutProducts;

        ShopViewHolder(@NonNull View itemView) {
            super(itemView);

            cbShop = itemView.findViewById(R.id.cbShop);
            txtShopName = itemView.findViewById(R.id.txtShopName);
            txtShopTotal = itemView.findViewById(R.id.txtShopTotal);
            layoutProducts = itemView.findViewById(R.id.layoutProducts);
        }

        void bind(CartDetailResponse.ShopCart shop) {
            txtShopName.setText(shop.getShopName());
            txtShopTotal.setText(formatMoney(calcShopSelectedTotal(shop)));

            cbShop.setOnCheckedChangeListener(null);
            cbShop.setChecked(shop.areAllItemsSelected());

            cbShop.setOnCheckedChangeListener((buttonView, isChecked) -> {
                for (CartDetailResponse.CartItem item : shop.getItems()) {
                    item.setSelected(isChecked);
                }

                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(pos);
                }

                if (listener != null) {
                    listener.onSelectionChanged();
                }
            });

            layoutProducts.removeAllViews();

            for (CartDetailResponse.CartItem item : shop.getItems()) {
                View productView = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.item_cart_product, layoutProducts, false);

                bindProduct(productView, item);

                layoutProducts.addView(productView);
            }
        }

        private void bindProduct(
                View productView,
                CartDetailResponse.CartItem item
        ) {
            CheckBox cbProduct = productView.findViewById(R.id.cbProduct);
            ImageView imgProduct = productView.findViewById(R.id.imgProduct);
            TextView txtProductName = productView.findViewById(R.id.txtProductName);
            TextView txtVariant = productView.findViewById(R.id.txtVariant);
            TextView txtPrice = productView.findViewById(R.id.txtPrice);
            TextView txtQuantity = productView.findViewById(R.id.txtQuantity);
            TextView btnMinus = productView.findViewById(R.id.btnMinus);
            TextView btnPlus = productView.findViewById(R.id.btnPlus);
            ImageView btnDelete = productView.findViewById(R.id.btnDelete);

            txtProductName.setText(item.getProductName());
            txtVariant.setText("Phân loại: " + item.getVariantText() + "  ›");
            txtPrice.setText(formatMoney(item.getPrice()));
            txtQuantity.setText(String.valueOf(item.getQuantity()));

            ImageLoader.load(imgProduct.getContext(), item.getImageUrl(), imgProduct);

            cbProduct.setOnCheckedChangeListener(null);
            cbProduct.setChecked(item.isSelected());

            cbProduct.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);

                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position);
                }

                if (listener != null) {
                    listener.onSelectionChanged();
                }
            });

            txtVariant.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChangeVariant(item);
                }
            });

            productView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(item);
                }
            });

            btnMinus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDecrease(item);
                }
            });

            btnPlus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIncrease(item);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemove(item);
                }
            });
        }
    }

    private double calcShopSelectedTotal(CartDetailResponse.ShopCart shop) {
        double total = 0;

        for (CartDetailResponse.CartItem item : shop.getItems()) {
            if (item.isSelected()) {
                total += item.getLineTotal();
            }
        }

        return total;
    }

    private String formatMoney(double value) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(value) + "đ";
    }

    public interface CartListener {
        void onSelectionChanged();

        void onIncrease(CartDetailResponse.CartItem item);

        void onDecrease(CartDetailResponse.CartItem item);

        void onRemove(CartDetailResponse.CartItem item);

        void onProductClick(CartDetailResponse.CartItem item);

        void onChangeVariant(CartDetailResponse.CartItem item);
    }
}