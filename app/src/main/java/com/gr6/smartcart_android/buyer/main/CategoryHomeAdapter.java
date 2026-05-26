package com.gr6.smartcart_android.buyer.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.main.response.HomeCategoryResponse;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class CategoryHomeAdapter extends RecyclerView.Adapter<CategoryHomeAdapter.CategoryViewHolder> {

    private final List<HomeCategoryResponse> categories = new ArrayList<>();
    private OnCategoryClickListener listener;

    public void setData(List<HomeCategoryResponse> data) {
        categories.clear();

        if (data != null) {
            categories.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CategoryViewHolder holder,
            int position
    ) {
        HomeCategoryResponse item = categories.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgCategory;
        private final TextView txtCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCategory = itemView.findViewById(R.id.imgCategory);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
        }

        void bind(HomeCategoryResponse item) {
            txtCategoryName.setText(item.getCategoryName());
            ImageLoader.loadCircle(itemView.getContext(), item.getCategoryImageUrl(), imgCategory);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(item);
                }
            });
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(HomeCategoryResponse category);
    }
}