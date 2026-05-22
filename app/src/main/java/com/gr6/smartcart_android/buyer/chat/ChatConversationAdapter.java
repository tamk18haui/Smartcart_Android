package com.gr6.smartcart_android.buyer.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.chat.response.ConversationResponse;
import com.gr6.smartcart_android.buyer.chat.util.ChatTimeFormatter;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ChatConversationAdapter extends RecyclerView.Adapter<ChatConversationAdapter.ConversationViewHolder> {

    private final List<ConversationResponse> conversations = new ArrayList<>();
    private OnConversationClickListener listener;

    public void setListener(OnConversationClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<ConversationResponse> data) {
        conversations.clear();

        if (data != null) {
            conversations.addAll(data);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_conversation, parent, false);

        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ConversationViewHolder holder,
            int position
    ) {
        holder.bind(conversations.get(position));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgAvatar;
        private final TextView txtName;
        private final TextView txtLastMessage;
        private final TextView txtTime;
        private final TextView txtUnread;

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);

            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtName = itemView.findViewById(R.id.txtName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtUnread = itemView.findViewById(R.id.txtUnread);
        }

        void bind(ConversationResponse item) {
            txtName.setText(item.getPartnerName());
            txtLastMessage.setText(item.getLastMessage());
            txtTime.setText(ChatTimeFormatter.shortTime(item.getUpdatedAt()));

            if (item.getPartnerAvatarUrl() == null || item.getPartnerAvatarUrl().trim().isEmpty()) {
                imgAvatar.setImageResource(R.drawable.ic_user);
            } else {
                ImageLoader.loadCircle(itemView.getContext(), item.getPartnerAvatarUrl(), imgAvatar);
            }

            long unread = item.getUnreadCount();

            if (unread > 0) {
                txtUnread.setVisibility(View.VISIBLE);
                txtUnread.setText(unread > 99 ? "99+" : String.valueOf(unread));
            } else {
                txtUnread.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(item);
                }
            });
        }
    }

    public interface OnConversationClickListener {
        void onClick(ConversationResponse conversation);
    }
}