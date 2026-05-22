package com.gr6.smartcart_android.chat.request;

import com.google.gson.annotations.SerializedName;

public class ChatMessageRequest {

    @SerializedName("receiverId")
    private Long receiverId;

    @SerializedName("content")
    private String content;

    public ChatMessageRequest(Long receiverId, String content) {
        this.receiverId = receiverId;
        this.content = content;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }
}