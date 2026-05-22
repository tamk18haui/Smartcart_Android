package com.gr6.smartcart_android.chat.model;

import com.google.gson.annotations.SerializedName;

public class ChatMessageResponse {

    @SerializedName("messageId")
    private Long messageId;

    @SerializedName("conversationId")
    private Long conversationId;

    @SerializedName("senderId")
    private Long senderId;

    @SerializedName("senderName")
    private String senderName;

    @SerializedName("senderAvatarUrl")
    private String senderAvatarUrl;

    @SerializedName("receiverId")
    private Long receiverId;

    @SerializedName("receiverName")
    private String receiverName;

    @SerializedName("receiverAvatarUrl")
    private String receiverAvatarUrl;

    @SerializedName("content")
    private String content;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("readAt")
    private String readAt;

    public Long getMessageId() {
        return messageId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverAvatarUrl() {
        return receiverAvatarUrl;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getReadAt() {
        return readAt;
    }

    public boolean isMine(Long currentUserId) {
        return currentUserId != null && senderId != null && currentUserId.equals(senderId);
    }
}
