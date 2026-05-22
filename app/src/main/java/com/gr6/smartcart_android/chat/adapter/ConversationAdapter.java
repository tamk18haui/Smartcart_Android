package com.gr6.smartcart_android.chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.chat.model.ConversationResponse;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    public interface OnConversationClickListener {
        void onClick(ConversationResponse conversation);
    }

    private final List<ConversationResponse> conversations;
    private final OnConversationClickListener listener;

    public ConversationAdapter(List<ConversationResponse> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ConversationResponse conversation = conversations.get(position);

        holder.tvAvatar.setText(getInitial(conversation.getPartnerName()));
        holder.tvName.setText(conversation.getPartnerName());
        holder.tvLastMessage.setText(conversation.getLastMessage());
        holder.tvTime.setText(formatTime(conversation.getUpdatedAt()));

        int unread = conversation.getUnreadCount();
        if (unread > 0) {
            holder.tvUnread.setVisibility(View.VISIBLE);
            holder.tvUnread.setText(unread > 99 ? "99+" : String.valueOf(unread));
        } else {
            holder.tvUnread.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations == null ? 0 : conversations.size();
    }

    private String getInitial(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "K";
        }
        return name.trim().substring(0, 1).toUpperCase();
    }

    private String formatTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "";
        }

        String value = raw.replace("T", " ");
        int dotIndex = value.indexOf(".");
        if (dotIndex > 0) {
            value = value.substring(0, dotIndex);
        }

        if (value.length() >= 16) {
            return value.substring(0, 16);
        }
        return value;
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar;
        TextView tvName;
        TextView tvLastMessage;
        TextView tvTime;
        TextView tvUnread;

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnread = itemView.findViewById(R.id.tvUnread);
        }
    }
}
