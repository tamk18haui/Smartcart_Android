package com.gr6.smartcart_android.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.chat.response.ConversationResponse;
import com.gr6.smartcart_android.chat.util.ChatTimeFormatter;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private final Context context;
    private final List<ConversationResponse> conversations = new ArrayList<>();
    private OnConversationClickListener listener;

    public ConversationAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<ConversationResponse> data) {
        conversations.clear();

        if (data != null) {
            conversations.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void setOnConversationClickListener(OnConversationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);

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

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);

            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtName = itemView.findViewById(R.id.txtName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtUnread = itemView.findViewById(R.id.txtUnread);
        }

        void bind(ConversationResponse item) {
            if (item == null) return;

            txtName.setText(item.getPartnerName());
            txtLastMessage.setText(item.getLastMessage());
            txtTime.setText(ChatTimeFormatter.shortTime(item.getUpdatedAt()));

            if (item.getUnreadCount() > 0) {
                txtUnread.setVisibility(View.VISIBLE);
                txtUnread.setText(String.valueOf(item.getUnreadCount()));
            } else {
                txtUnread.setVisibility(View.GONE);
            }

            String avatar = item.getPartnerAvatarUrl();

            if (avatar == null || avatar.trim().isEmpty()) {
                imgAvatar.setImageResource(R.drawable.ic_user);
            } else {
                ImageLoader.loadCircle(context, avatar, imgAvatar);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(item);
                }
            });
        }
    }

    public interface OnConversationClickListener {
        void onConversationClick(ConversationResponse conversation);
    }
}