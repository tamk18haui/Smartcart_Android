package com.gr6.smartcart_android.buyer.notification;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.notification.response.BuyerNotificationResponse;

import java.util.ArrayList;
import java.util.List;

public class BuyerNotificationAdapter extends RecyclerView.Adapter<BuyerNotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(BuyerNotificationResponse notification);
    }

    private final List<BuyerNotificationResponse> items = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public BuyerNotificationAdapter(
            List<BuyerNotificationResponse> initialItems,
            OnNotificationClickListener listener
    ) {
        if (initialItems != null) {
            items.addAll(initialItems);
        }
        this.listener = listener;
    }

    public void setItems(List<BuyerNotificationResponse> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_buyer_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout root;
        private final TextView txtTitle;
        private final TextView txtContent;
        private final TextView txtTime;
        private final View unreadDot;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.rootNotification);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtTime = itemView.findViewById(R.id.txtTime);
            unreadDot = itemView.findViewById(R.id.unreadDot);
        }

        void bind(BuyerNotificationResponse item, OnNotificationClickListener listener) {
            txtTitle.setText(item.getTitle());
            txtContent.setText(item.getContent());
            txtTime.setText(formatTime(item.getCreatedAt()));

            boolean unread = !item.isRead();
            unreadDot.setVisibility(unread ? View.VISIBLE : View.INVISIBLE);
            txtTitle.setTypeface(null, unread ? Typeface.BOLD : Typeface.NORMAL);
            root.setAlpha(unread ? 1f : 0.78f);

            root.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(item);
                }
            });
        }

        private String formatTime(String raw) {
            if (raw == null || raw.trim().isEmpty()) return "";
            String value = raw.replace('T', ' ');
            int dotIndex = value.indexOf('.');
            if (dotIndex > 0) value = value.substring(0, dotIndex);
            if (value.length() >= 16) return value.substring(0, 16);
            return value;
        }
    }
}
