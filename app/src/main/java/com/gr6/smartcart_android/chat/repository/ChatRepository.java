package com.gr6.smartcart_android.chat.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.chat.api.ChatApiService;
import com.gr6.smartcart_android.chat.request.ChatMessageRequest;
import com.gr6.smartcart_android.chat.response.ChatMessageResponse;
import com.gr6.smartcart_android.chat.response.ChatMessagesPageResponse;
import com.gr6.smartcart_android.chat.response.ConversationResponse;
import com.gr6.smartcart_android.chat.websocket.ChatWebSocketClient;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {

    private final Context appContext;
    private final ChatApiService apiService;
    private final ChatWebSocketClient socketClient;

    public ChatRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.apiService = ApiClient.createService(appContext, ChatApiService.class);
        this.socketClient = new ChatWebSocketClient();
    }

    public void getConversations(DataCallback<List<ConversationResponse>> callback) {
        apiService.getConversations().enqueue(new Callback<BaseResponse<List<ConversationResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<ConversationResponse>>> call,
                    @NonNull Response<BaseResponse<List<ConversationResponse>>> response
            ) {
                handleResponse(response, callback, "Không tải được danh sách hội thoại");
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
        if (partnerId == null || partnerId <= 0) {
            callback.onError("Không tìm thấy người cần nhắn tin");
            return;
        }

        apiService.getMessages(partnerId, page, size)
                .enqueue(new Callback<BaseResponse<ChatMessagesPageResponse>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<BaseResponse<ChatMessagesPageResponse>> call,
                            @NonNull Response<BaseResponse<ChatMessagesPageResponse>> response
                    ) {
                        handleResponse(response, callback, "Không tải được tin nhắn");
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
        if (receiverId == null || receiverId <= 0) {
            callback.onError("Không tìm thấy người nhận");
            return;
        }

        if (content == null || content.trim().isEmpty()) {
            callback.onError("Tin nhắn không được để trống");
            return;
        }

        ChatMessageRequest request = new ChatMessageRequest(receiverId, content.trim());

        apiService.sendMessage(request).enqueue(new Callback<BaseResponse<ChatMessageResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<ChatMessageResponse>> call,
                    @NonNull Response<BaseResponse<ChatMessageResponse>> response
            ) {
                handleResponse(response, callback, "Gửi tin nhắn thất bại");
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
        if (partnerId == null || partnerId <= 0) return;

        apiService.markAsRead(partnerId).enqueue(new Callback<BaseResponse<Integer>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<Integer>> call,
                    @NonNull Response<BaseResponse<Integer>> response
            ) {
                // Không cần xử lý UI
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<Integer>> call,
                    @NonNull Throwable t
            ) {
                // Không cần báo lỗi làm phiền người dùng
            }
        });
    }

    public void connectWebSocket(ChatWebSocketClient.Listener listener) {
        String token = TokenManager.getInstance(appContext).getToken();
        Long currentUserId = UserSession.getInstance(appContext).getUserId();

        socketClient.setListener(listener);
        socketClient.connect(token, currentUserId);
    }

    public boolean isSocketConnected() {
        return socketClient.isConnected();
    }

    public boolean sendMessageBySocket(Long receiverId, String content) {
        return socketClient.sendMessage(receiverId, content);
    }

    public void disconnectWebSocket() {
        socketClient.disconnect();
    }

    private <T> void handleResponse(
            Response<BaseResponse<T>> response,
            DataCallback<T> callback,
            String fallbackMessage
    ) {
        if (!response.isSuccessful()) {
            callback.onError(fallbackMessage + ". Mã lỗi: " + response.code());
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