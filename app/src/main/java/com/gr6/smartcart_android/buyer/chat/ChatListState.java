package com.gr6.smartcart_android.buyer.chat;

import com.gr6.smartcart_android.buyer.chat.response.ConversationResponse;

import java.util.ArrayList;
import java.util.List;

public class ChatListState {

    private final boolean loading;
    private final boolean success;
    private final String message;
    private final List<ConversationResponse> data;

    private ChatListState(
            boolean loading,
            boolean success,
            String message,
            List<ConversationResponse> data
    ) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ChatListState loading() {
        return new ChatListState(true, false, null, null);
    }

    public static ChatListState success(List<ConversationResponse> data, String message) {
        return new ChatListState(false, true, message, data);
    }

    public static ChatListState error(String message) {
        return new ChatListState(false, false, message, null);
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message == null ? "Thao tác thất bại" : message;
    }

    public List<ConversationResponse> getData() {
        return data == null ? new ArrayList<>() : data;
    }
}