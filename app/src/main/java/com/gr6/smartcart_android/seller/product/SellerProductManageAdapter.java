package com.gr6.smartcart_android.seller.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.seller.product.response.ProductResponse;
import com.gr6.smartcart_android.seller.product.response.VariantResponse;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SellerProductManageAdapter extends RecyclerView.Adapter<SellerProductManageAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(ProductResponse product);
    }

    private final List<ProductResponse> products = new ArrayList<>();
    private final OnProductClickListener listener;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    public SellerProductManageAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ProductResponse> newProducts) {
        products.clear();

        if (newProducts != null) {
            products.addAll(newProducts);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seller_product_manage, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductResponse product = products.get(position);

        holder.txtProductName.setText(product.getName());
        holder.txtProductPrice.setText(formatMoney(product.getBasePrice()));
        holder.txtProductStock.setText("Kho: " + totalStock(product));
        holder.txtProductSold.setText("Đã bán: " + safeInt(product.getSoldQuantity()));
        holder.txtVariantCount.setText(product.getVariants().size() + " phân loại");
        holder.txtProductSku.setText(buildSkuText(product));

        holder.txtProductStatus.setText(statusText(product.getStatus()));
        holder.txtProductStatus.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                statusTextColor(product.getStatus())
        ));
        holder.txtProductStatus.setBackgroundResource(statusBackground(product.getStatus()));

        ImageLoader.load(
                holder.itemView.getContext(),
                product.getFirstImage(),
                holder.imgProduct
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private String buildSkuText(ProductResponse product) {
        if (product == null || product.getVariants().isEmpty()) {
            return "SKU: SP-" + (product == null || product.getProductId() == null ? "N/A" : product.getProductId());
        }

        VariantResponse firstVariant = product.getVariants().get(0);

        if (firstVariant.getSku() != null && !firstVariant.getSku().trim().isEmpty()) {
            return "SKU: " + firstVariant.getSku();
        }

        return "SKU: SP-" + (product.getProductId() == null ? "N/A" : product.getProductId());
    }

    private int totalStock(ProductResponse product) {
        if (product == null || product.getVariants().isEmpty()) {
            return 0;
        }

        int total = 0;

        for (VariantResponse variant : product.getVariants()) {
            if (variant != null) {
                total += variant.getStockQuantity();
            }
        }

        return total;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0đ";
        }

        return moneyFormat.format(value) + "đ";
    }

    private String statusText(String status) {
        String normalized = normalizeStatus(status);

        switch (normalized) {
            case "ACTIVE":
                return "Đang hoạt động";
            case "INACTIVE":
            case "HIDDEN":
                return "Tạm ẩn";
            case "DELETED":
                return "Đã xóa";
            default:
                return normalized;
        }
    }

    private int statusTextColor(String status) {
        String normalized = normalizeStatus(status);

        switch (normalized) {
            case "ACTIVE":
                return R.color.success;
            case "INACTIVE":
            case "HIDDEN":
                return R.color.warning;
            case "DELETED":
                return R.color.danger;
            default:
                return R.color.text_secondary;
        }
    }

    private int statusBackground(String status) {
        String normalized = normalizeStatus(status);

        switch (normalized) {
            case "ACTIVE":
                return R.drawable.bg_seller_status_active;
            case "INACTIVE":
            case "HIDDEN":
                return R.drawable.bg_seller_status_warning;
            case "DELETED":
                return R.drawable.bg_seller_status_danger;
            default:
                return R.drawable.bg_seller_status_neutral;
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "ACTIVE";
        }

        return status.trim().toUpperCase();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtProductStatus;
        TextView txtProductName;
        TextView txtProductSku;
        TextView txtProductPrice;
        TextView txtProductStock;
        TextView txtProductSold;
        TextView txtVariantCount;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductStatus = itemView.findViewById(R.id.txtProductStatus);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductSku = itemView.findViewById(R.id.txtProductSku);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtProductStock = itemView.findViewById(R.id.txtProductStock);
            txtProductSold = itemView.findViewById(R.id.txtProductSold);
            txtVariantCount = itemView.findViewById(R.id.txtVariantCount);
        }
    }
}