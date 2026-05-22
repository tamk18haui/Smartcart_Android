package com.gr6.smartcart_android.buyer.chat;

import com.gr6.smartcart_android.buyer.chat.response.ChatMessageResponse;

import java.util.ArrayList;
import java.util.List;

public class ChatMessagesState {

    private final boolean loading;
    private final boolean success;
    private final String message;
    private final List<ChatMessageResponse> data;

    private ChatMessagesState(
            boolean loading,
            boolean success,
            String message,
            List<ChatMessageResponse> data
    ) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ChatMessagesState loading() {
        return new ChatMessagesState(true, false, null, null);
    }

    public static ChatMessagesState success(List<ChatMessageResponse> data, String message) {
        return new ChatMessagesState(false, true, message, data);
    }

    public static ChatMessagesState error(String message) {
        return new ChatMessagesState(false, false, message, null);
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message == null ? "Không tải được tin nhắn" : message;
    }

    public List<ChatMessageResponse> getData() {
        return data == null ? new ArrayList<>() : data;
    }
}