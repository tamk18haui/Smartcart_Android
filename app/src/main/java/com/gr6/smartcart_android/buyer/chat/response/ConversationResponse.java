package com.gr6.smartcart_android.buyer.chat.response;

import com.google.gson.annotations.SerializedName;

public class ConversationResponse {

    @SerializedName("conversationId")
    private Long conversationId;

    @SerializedName("partnerId")
    private Long partnerId;

    @SerializedName("partnerName")
    private String partnerName;

    @SerializedName("partnerAvatarUrl")
    private String partnerAvatarUrl;

    @SerializedName("lastMessage")
    private String lastMessage;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("unreadCount")
    private Long unreadCount;

    public Long getConversationId() {
        return conversationId;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public String getPartnerName() {
        if (partnerName == null || partnerName.trim().isEmpty()) {
            return "Người dùng SmartCart";
        }
        return partnerName;
    }

    public String getPartnerAvatarUrl() {
        return partnerAvatarUrl;
    }

    public String getLastMessage() {
        if (lastMessage == null || lastMessage.trim().isEmpty()) {
            return "Chưa có tin nhắn";
        }
        return lastMessage;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public long getUnreadCount() {
        return unreadCount == null ? 0 : unreadCount;
    }
}