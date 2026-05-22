package com.gr6.smartcart_android.chat.repository;

import android.content.Context;

import com.gr6.smartcart_android.chat.api.ChatApiService;
import com.gr6.smartcart_android.chat.model.ChatMessageRequest;
import com.gr6.smartcart_android.chat.model.ChatMessageResponse;
import com.gr6.smartcart_android.chat.model.ConversationResponse;
import com.gr6.smartcart_android.chat.model.SpringPageResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;

import java.util.List;

import retrofit2.Call;

public class ChatRepository {

    private final ChatApiService apiService;

    public ChatRepository(Context context) {
        apiService = ApiClient.createService(context, ChatApiService.class);
    }

    public Call<BaseResponse<List<ConversationResponse>>> getConversations() {
        return apiService.getConversations();
    }

    public Call<BaseResponse<SpringPageResponse<ChatMessageResponse>>> getMessages(Long partnerId, int page, int size) {
        return apiService.getMessages(partnerId, page, size);
    }

    public Call<BaseResponse<ChatMessageResponse>> sendMessage(Long receiverId, String content) {
        return apiService.sendMessage(new ChatMessageRequest(receiverId, content));
    }

    public Call<BaseResponse<Integer>> markAsRead(Long partnerId) {
        return apiService.markAsRead(partnerId);
    }
}
