package com.gr6.smartcart_android.buyer.category;

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

public class CategoryGridAdapter extends RecyclerView.Adapter<CategoryGridAdapter.CategoryVH> {

    public interface OnCategoryClickListener {
        void onCategoryClick(HomeCategoryResponse category);
    }

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
    public CategoryVH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_grid, parent, false);

        return new CategoryVH(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CategoryVH holder,
            int position
    ) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryVH extends RecyclerView.ViewHolder {

        private final ImageView imgCategory;
        private final TextView txtCategoryName;

        CategoryVH(@NonNull View itemView) {
            super(itemView);

            imgCategory = itemView.findViewById(R.id.imgCategory);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
        }

        void bind(HomeCategoryResponse category) {
            if (category == null) return;

            txtCategoryName.setText(category.getCategoryName());

            String imageUrl = category.getCategoryImageUrl();

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                imgCategory.setImageResource(android.R.drawable.ic_menu_gallery);
            } else {
                ImageLoader.loadCircle(itemView.getContext(), imageUrl.trim(), imgCategory);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }
    }
}