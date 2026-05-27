package com.gr6.smartcart_android.chat.request;

import com.google.gson.annotations.SerializedName;

public class ChatMessageRequest {

    @SerializedName("receiverId")
    private Long receiverId;

    @SerializedName("content")
    private String content;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("messageType")
    private String messageType;

    public ChatMessageRequest(Long receiverId, String content) {
        this(receiverId, content, null, "TEXT");
    }

    public ChatMessageRequest(
            Long receiverId,
            String content,
            String imageUrl,
            String messageType
    ) {
        this.receiverId = receiverId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.messageType = messageType;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public String getImageUrl() {
        return imageUrl == null ? "" : imageUrl;
    }

    public String getMessageType() {
        if (messageType == null || messageType.trim().isEmpty()) {
            return getImageUrl().isEmpty() ? "TEXT" : "IMAGE";
        }

        return messageType.trim().toUpperCase();
    }
}