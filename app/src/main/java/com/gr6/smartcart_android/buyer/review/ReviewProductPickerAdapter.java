package com.gr6.smartcart_android.buyer.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ReviewProductPickerAdapter
        extends RecyclerView.Adapter<ReviewProductPickerAdapter.ProductVH> {

    public interface OnItemClickListener {
        void onItemClick(ReviewActivity.ReviewItemArg item);
    }

    private final List<ReviewActivity.ReviewItemArg> items = new ArrayList<>();
    private Long selectedOrderItemId;
    private OnItemClickListener listener;

    public void setData(
            List<ReviewActivity.ReviewItemArg> data,
            Long selectedOrderItemId
    ) {
        items.clear();

        if (data != null) {
            items.addAll(data);
        }

        this.selectedOrderItemId = selectedOrderItemId;
        notifyDataSetChanged();
    }

    public void setSelectedOrderItemId(Long selectedOrderItemId) {
        this.selectedOrderItemId = selectedOrderItemId;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductVH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review_product_picker, parent, false);

        return new ProductVH(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductVH holder,
            int position
    ) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ProductVH extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;
        private final TextView txtProductName;
        private final TextView txtVariantSku;

        ProductVH(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtVariantSku = itemView.findViewById(R.id.txtVariantSku);
        }

        void bind(ReviewActivity.ReviewItemArg item) {
            if (item == null) return;

            txtProductName.setText(item.getProductName());

            if (item.getProductImage().isEmpty()) {
                imgProduct.setImageResource(R.drawable.ic_cart);
            } else {
                ImageLoader.load(itemView.getContext(), item.getProductImage(), imgProduct);
            }

            boolean reviewed = item.isReviewed();

            if (reviewed) {
                itemView.setAlpha(0.45f);
                itemView.setEnabled(false);
                itemView.setBackgroundResource(R.drawable.bg_review_disabled);

                txtVariantSku.setVisibility(View.VISIBLE);
                txtVariantSku.setText("Đã đánh giá");

                itemView.setOnClickListener(null);
                return;
            }

            itemView.setAlpha(1f);
            itemView.setEnabled(true);

            if (item.getVariantSku().isEmpty()) {
                txtVariantSku.setVisibility(View.GONE);
            } else {
                txtVariantSku.setVisibility(View.VISIBLE);
                txtVariantSku.setText(item.getVariantSku());
            }

            boolean selected = item.getOrderItemId() != null
                    && item.getOrderItemId().equals(selectedOrderItemId);

            itemView.setBackgroundResource(
                    selected
                            ? R.drawable.bg_review_product_selected
                            : R.drawable.bg_review_product_normal
            );

            itemView.setOnClickListener(v -> {
                if (item.isReviewed()) return;

                selectedOrderItemId = item.getOrderItemId();
                notifyDataSetChanged();

                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}