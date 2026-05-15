package com.gr6.smartcart_android.buyer.address.location;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class LocationPickerAdapter extends RecyclerView.Adapter<LocationPickerAdapter.LocationViewHolder>
        implements Filterable {

    private final List<LocationUnit> originalData = new ArrayList<>();
    private final List<LocationUnit> filteredData = new ArrayList<>();

    private OnLocationClickListener listener;

    public void setData(List<LocationUnit> data) {
        originalData.clear();
        filteredData.clear();

        if (data != null) {
            originalData.addAll(data);
            filteredData.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void setListener(OnLocationClickListener listener) {
        this.listener = listener;
    }

    public boolean isEmpty() {
        return filteredData.isEmpty();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location_picker, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull LocationViewHolder holder,
            int position
    ) {
        holder.bind(filteredData.get(position));
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    @Override
    public Filter getFilter() {
        return locationFilter;
    }

    private final Filter locationFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<LocationUnit> result = new ArrayList<>();

            String keyword = constraint == null ? "" : constraint.toString().trim();

            if (TextUtils.isEmpty(keyword)) {
                result.addAll(originalData);
            } else {
                String normalizedKeyword = normalizeSearchText(keyword);

                for (LocationUnit unit : originalData) {
                    String normalizedName = normalizeSearchText(unit.getName());

                    if (normalizedName.contains(normalizedKeyword)) {
                        result.add(unit);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = result;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData.clear();

            if (results.values instanceof List) {
                //noinspection unchecked
                filteredData.addAll((List<LocationUnit>) results.values);
            }

            notifyDataSetChanged();
        }
    };

    private String normalizeSearchText(String text) {
        if (text == null) return "";

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replace("Đ", "D").replace("đ", "d");

        return normalized.toLowerCase().trim();
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtLocationName;
        private final TextView txtLocationType;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);

            txtLocationName = itemView.findViewById(R.id.txtLocationName);
            txtLocationType = itemView.findViewById(R.id.txtLocationType);
        }

        void bind(LocationUnit unit) {
            txtLocationName.setText(unit.getName());

            String type = unit.getDivisionType();

            if (type == null || type.trim().isEmpty()) {
                txtLocationType.setVisibility(View.GONE);
            } else {
                txtLocationType.setVisibility(View.VISIBLE);
                txtLocationType.setText(type);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(unit);
                }
            });
        }
    }

    public interface OnLocationClickListener {
        void onClick(LocationUnit unit);
    }
}