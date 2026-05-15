package com.gr6.smartcart_android.buyer.address;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.address.response.AddressResponse;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private final List<AddressResponse> addresses = new ArrayList<>();
    private AddressListener listener;

    public void setData(List<AddressResponse> data) {
        addresses.clear();

        if (data != null) {
            addresses.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void setListener(AddressListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AddressViewHolder holder,
            int position
    ) {
        holder.bind(addresses.get(position));
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtReceiverName;
        private final TextView txtReceiverPhone;
        private final TextView txtFullAddress;
        private final TextView txtDefaultBadge;
        private final TextView btnEdit;
        private final TextView btnDelete;
        private final TextView btnSetDefault;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);

            txtReceiverName = itemView.findViewById(R.id.txtReceiverName);
            txtReceiverPhone = itemView.findViewById(R.id.txtReceiverPhone);
            txtFullAddress = itemView.findViewById(R.id.txtFullAddress);
            txtDefaultBadge = itemView.findViewById(R.id.txtDefaultBadge);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnSetDefault = itemView.findViewById(R.id.btnSetDefault);
        }

        void bind(AddressResponse address) {
            txtReceiverName.setText(address.getReceiverName());
            txtReceiverPhone.setText(address.getReceiverPhone());
            txtFullAddress.setText(address.getFullAddress());

            if (address.isDefaultAddress()) {
                txtDefaultBadge.setVisibility(View.VISIBLE);
                btnSetDefault.setText("Mặc định");
                btnSetDefault.setEnabled(false);
                btnSetDefault.setAlpha(0.55f);
            } else {
                txtDefaultBadge.setVisibility(View.GONE);
                btnSetDefault.setText("Đặt mặc định");
                btnSetDefault.setEnabled(true);
                btnSetDefault.setAlpha(1f);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSelect(address);
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(address);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(address);
            });

            btnSetDefault.setOnClickListener(v -> {
                if (listener != null) listener.onSetDefault(address);
            });
        }
    }

    public interface AddressListener {
        void onSelect(AddressResponse address);

        void onEdit(AddressResponse address);

        void onDelete(AddressResponse address);

        void onSetDefault(AddressResponse address);
    }
}