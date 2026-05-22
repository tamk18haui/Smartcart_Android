package com.gr6.smartcart_android.buyer.chat;

import com.gr6.smartcart_android.buyer.chat.response.ChatMessageResponse;

public class SendMessageState {

    private final boolean loading;
    private final boolean success;
    private final String message;
    private final ChatMessageResponse data;

    private SendMessageState(
            boolean loading,
            boolean success,
            String message,
            ChatMessageResponse data
    ) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static SendMessageState loading() {
        return new SendMessageState(true, false, null, null);
    }

    public static SendMessageState success(ChatMessageResponse data, String message) {
        return new SendMessageState(false, true, message, data);
    }

    public static SendMessageState error(String message) {
        return new SendMessageState(false, false, message, null);
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message == null ? "Gửi tin nhắn thất bại" : message;
    }

    public ChatMessageResponse getData() {
        return data;
    }
}