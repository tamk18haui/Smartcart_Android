package com.gr6.smartcart_android.buyer.chat.request;

public class ChatMessageRequest {

    private Long receiverId;
    private String content;

    public ChatMessageRequest() {
    }

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