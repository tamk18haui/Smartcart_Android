package com.gr6.smartcart_android.buyer.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.chat.response.ChatMessageResponse;
import com.gr6.smartcart_android.buyer.chat.util.ChatTimeFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private static final int TYPE_MINE = 1;
    private static final int TYPE_OTHER = 2;

    private final List<ChatMessageResponse> messages = new ArrayList<>();
    private final Set<Long> messageIds = new HashSet<>();

    private Long currentUserId;

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setData(List<ChatMessageResponse> data) {
        messages.clear();
        messageIds.clear();

        if (data != null) {
            for (ChatMessageResponse item : data) {
                addInternal(item);
            }
        }

        notifyDataSetChanged();
    }

    public void addMessage(ChatMessageResponse message) {
        if (message == null) return;

        if (!addInternal(message)) return;

        notifyItemInserted(messages.size() - 1);
    }

    private boolean addInternal(ChatMessageResponse message) {
        Long id = message.getMessageId();

        if (id != null && messageIds.contains(id)) {
            return false;
        }

        messages.add(message);

        if (id != null) {
            messageIds.add(id);
        }

        return true;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessageResponse message = messages.get(position);

        if (currentUserId != null && currentUserId.equals(message.getSenderId())) {
            return TYPE_MINE;
        }

        return TYPE_OTHER;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        int layout = viewType == TYPE_MINE
                ? R.layout.item_chat_message_mine
                : R.layout.item_chat_message_other;

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MessageViewHolder holder,
            int position
    ) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtMessage;
        private final TextView txtTime;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
        }

        void bind(ChatMessageResponse message) {
            txtMessage.setText(message.getContent());
            txtTime.setText(ChatTimeFormatter.shortTime(message.getCreatedAt()));
        }
    }
}