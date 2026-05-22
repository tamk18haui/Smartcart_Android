package com.gr6.smartcart_android.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.chat.response.ChatMessageResponse;
import com.gr6.smartcart_android.chat.util.ChatTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private static final int TYPE_MINE = 1;
    private static final int TYPE_OTHER = 2;

    private final List<ChatMessageResponse> messages = new ArrayList<>();
    private Long currentUserId;

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setData(List<ChatMessageResponse> data) {
        messages.clear();

        if (data != null) {
            messages.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void addMessage(ChatMessageResponse message) {
        if (message == null) return;

        Long newId = message.getMessageId();

        if (newId != null) {
            for (ChatMessageResponse old : messages) {
                if (newId.equals(old.getMessageId())) {
                    return;
                }
            }
        }

        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessageResponse message = messages.get(position);

        if (message != null && message.isMine(currentUserId)) {
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

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
        }

        void bind(ChatMessageResponse message) {
            if (message == null) return;

            txtMessage.setText(message.getContent());
            txtTime.setText(ChatTimeFormatter.shortTime(message.getCreatedAt()));
        }
    }
}