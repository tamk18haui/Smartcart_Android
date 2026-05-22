package com.gr6.smartcart_android.buyer.chat.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.buyer.chat.api.ChatApiService;
import com.gr6.smartcart_android.buyer.chat.request.ChatMessageRequest;
import com.gr6.smartcart_android.buyer.chat.response.ChatMessageResponse;
import com.gr6.smartcart_android.buyer.chat.response.ChatMessagesPageResponse;
import com.gr6.smartcart_android.buyer.chat.response.ConversationResponse;
import com.gr6.smartcart_android.buyer.chat.websocket.ChatWebSocketClient;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {

    private final ChatApiService apiService;
    private final ChatWebSocketClient webSocketClient;

    public ChatRepository(Context context) {
        apiService = ApiClient.createService(context, ChatApiService.class);
        webSocketClient = new ChatWebSocketClient();
    }

    public void getConversations(DataCallback<List<ConversationResponse>> callback) {
        apiService.getConversations().enqueue(new Callback<BaseResponse<List<ConversationResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<ConversationResponse>>> call,
                    @NonNull Response<BaseResponse<List<ConversationResponse>>> response
            ) {
                handleResponse(response, callback);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<ConversationResponse>>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void getMessages(
            Long partnerId,
            int page,
            int size,
            DataCallback<ChatMessagesPageResponse> callback
    ) {
        apiService.getMessages(partnerId, page, size).enqueue(new Callback<BaseResponse<ChatMessagesPageResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<ChatMessagesPageResponse>> call,
                    @NonNull Response<BaseResponse<ChatMessagesPageResponse>> response
            ) {
                handleResponse(response, callback);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<ChatMessagesPageResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void sendMessageByRest(
            Long receiverId,
            String content,
            DataCallback<ChatMessageResponse> callback
    ) {
        ChatMessageRequest request = new ChatMessageRequest(receiverId, content);

        apiService.sendMessage(request).enqueue(new Callback<BaseResponse<ChatMessageResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<ChatMessageResponse>> call,
                    @NonNull Response<BaseResponse<ChatMessageResponse>> response
            ) {
                handleResponse(response, callback);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<ChatMessageResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void markAsRead(Long partnerId) {
        apiService.markAsRead(partnerId).enqueue(new Callback<BaseResponse<Integer>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<Integer>> call,
                    @NonNull Response<BaseResponse<Integer>> response
            ) {
                // Không cần chặn UI.
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<Integer>> call,
                    @NonNull Throwable t
            ) {
                // Không cần chặn UI.
            }
        });
    }

    public void connectWebSocket(
            String token,
            Long currentUserId,
            ChatWebSocketClient.Listener listener
    ) {
        webSocketClient.setListener(listener);
        webSocketClient.connect(token, currentUserId);
    }
    public boolean isWebSocketConnected() {
        return webSocketClient.isConnected();
    }

    public void sendMessageByWebSocket(Long receiverId, String content) {
        webSocketClient.sendMessage(receiverId, content);
    }

    public void disconnectWebSocket() {
        webSocketClient.disconnect();
    }

    private <T> void handleResponse(
            Response<BaseResponse<T>> response,
            DataCallback<T> callback
    ) {
        if (!response.isSuccessful()) {
            callback.onError("Thao tác thất bại. Mã lỗi: " + response.code());
            return;
        }

        BaseResponse<T> body = response.body();

        if (body == null) {
            callback.onError("Server không trả dữ liệu");
            return;
        }

        if (!body.isSuccess()) {
            callback.onError(body.getSafeMessage());
            return;
        }

        callback.onSuccess(body.getData(), body.getSafeMessage());
    }

    public interface DataCallback<T> {
        void onSuccess(T data, String message);

        void onError(String message);
    }
}