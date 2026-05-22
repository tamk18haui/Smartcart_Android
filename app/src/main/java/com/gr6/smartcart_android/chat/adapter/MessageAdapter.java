package com.gr6.smartcart_android.chat.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.chat.model.ChatMessageResponse;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<ChatMessageResponse> messages;
    private final Long currentUserId;

    public MessageAdapter(List<ChatMessageResponse> messages, Long currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessageResponse message = messages.get(position);
        boolean mine = message.isMine(currentUserId);

        holder.messageRoot.setGravity(mine ? Gravity.END : Gravity.START);
        holder.tvMessage.setText(message.getContent());
        holder.tvMessage.setBackgroundResource(mine ? R.drawable.bg_chat_bubble_me : R.drawable.bg_chat_bubble_partner);
        holder.tvMessage.setTextColor(holder.itemView.getResources().getColor(mine ? R.color.text_white : R.color.text_primary));
        holder.tvTime.setText(formatTime(message.getCreatedAt()));
        holder.tvTime.setGravity(mine ? Gravity.END : Gravity.START);
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
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
            return value.substring(11, 16);
        }
        return value;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageRoot;
        TextView tvMessage;
        TextView tvTime;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageRoot = itemView.findViewById(R.id.messageRoot);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
