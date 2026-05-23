package com.gr6.smartcart_android.buyer.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.main.response.HomeCategoryResponse;

import java.util.ArrayList;
import java.util.List;

public class SearchFilterCategoryAdapter
        extends RecyclerView.Adapter<SearchFilterCategoryAdapter.CategoryViewHolder> {

    private final List<CategoryItem> items = new ArrayList<>();

    private Long selectedCategoryId = null;
    private OnCategorySelectedListener listener;

    public void setData(
            List<HomeCategoryResponse> categories,
            Long selectedCategoryId
    ) {
        this.selectedCategoryId = selectedCategoryId;

        items.clear();

        CategoryItem all = new CategoryItem();
        all.categoryId = null;
        all.categoryName = "Tất cả";
        items.add(all);

        if (categories != null) {
            for (HomeCategoryResponse category : categories) {
                if (category == null) continue;

                CategoryItem item = new CategoryItem();
                item.categoryId = category.getCategoryId();
                item.categoryName = category.getCategoryName();

                items.add(item);
            }
        }

        notifyDataSetChanged();
    }

    public void setSelectedCategoryId(Long selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
        notifyDataSetChanged();
    }

    public Long getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter_category, parent, false);

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CategoryViewHolder holder,
            int position
    ) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtCategoryName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
        }

        void bind(CategoryItem item) {
            if (item == null) return;

            txtCategoryName.setText(item.categoryName);

            boolean selected;

            if (item.categoryId == null) {
                selected = selectedCategoryId == null;
            } else {
                selected = item.categoryId.equals(selectedCategoryId);
            }

            txtCategoryName.setBackgroundResource(
                    selected
                            ? R.drawable.bg_filter_category_selected
                            : R.drawable.bg_filter_category_normal
            );

            txtCategoryName.setTextColor(ContextCompat.getColor(
                    itemView.getContext(),
                    selected ? R.color.surface : R.color.text_primary
            ));

            txtCategoryName.setOnClickListener(v -> {
                selectedCategoryId = item.categoryId;
                notifyDataSetChanged();

                if (listener != null) {
                    listener.onCategorySelected(item.categoryId);
                }
            });
        }
    }

    private static class CategoryItem {
        private Long categoryId;
        private String categoryName;
    }

    public interface OnCategorySelectedListener {
        void onCategorySelected(Long categoryId);
    }
}