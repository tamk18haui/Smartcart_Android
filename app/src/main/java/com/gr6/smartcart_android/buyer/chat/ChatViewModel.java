package com.gr6.smartcart_android.buyer.chat;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.buyer.chat.repository.ChatRepository;
import com.gr6.smartcart_android.buyer.chat.response.ChatMessageResponse;
import com.gr6.smartcart_android.buyer.chat.response.ChatMessagesPageResponse;
import com.gr6.smartcart_android.buyer.chat.websocket.ChatWebSocketClient;
import com.gr6.smartcart_android.common.storage.TokenManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repository;

    private final MutableLiveData<ChatMessagesState> messagesState = new MutableLiveData<>();
    private final MutableLiveData<SendMessageState> sendMessageState = new MutableLiveData<>();
    private final MutableLiveData<ChatMessageResponse> incomingMessage = new MutableLiveData<>();
    private final MutableLiveData<String> socketStatus = new MutableLiveData<>();

    private Long partnerId;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        repository = new ChatRepository(application);
    }

    public LiveData<ChatMessagesState> getMessagesState() {
        return messagesState;
    }

    public LiveData<SendMessageState> getSendMessageState() {
        return sendMessageState;
    }

    public LiveData<ChatMessageResponse> getIncomingMessage() {
        return incomingMessage;
    }

    public LiveData<String> getSocketStatus() {
        return socketStatus;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public void loadMessages() {
        if (partnerId == null || partnerId <= 0) {
            messagesState.setValue(ChatMessagesState.error("Không tìm thấy người nhận"));
            return;
        }

        messagesState.setValue(ChatMessagesState.loading());

        repository.getMessages(
                partnerId,
                0,
                50,
                new ChatRepository.DataCallback<ChatMessagesPageResponse>() {
                    @Override
                    public void onSuccess(ChatMessagesPageResponse data, String message) {
                        List<ChatMessageResponse> messages = data == null
                                ? new ArrayList<>()
                                : new ArrayList<>(data.getContent());

                        Collections.reverse(messages);

                        messagesState.postValue(ChatMessagesState.success(messages, message));
                    }

                    @Override
                    public void onError(String message) {
                        messagesState.postValue(ChatMessagesState.error(message));
                    }
                }
        );
    }

    public void connectSocket() {
        String token = TokenManager.getInstance(getApplication()).getToken();

        if (token == null || token.trim().isEmpty()) {
            socketStatus.postValue("Bạn cần đăng nhập để chat");
            return;
        }

        Long currentUserId = com.gr6.smartcart_android.common.storage.UserSession
                .getInstance(getApplication())
                .getUserId();

        if (currentUserId == null || currentUserId <= 0) {
            socketStatus.postValue("Không tìm thấy user hiện tại");
            return;
        }

        repository.connectWebSocket(token, currentUserId, new ChatWebSocketClient.Listener() {
            @Override
            public void onConnected() {
                socketStatus.postValue("Đang hoạt động");
            }

            @Override
            public void onMessage(ChatMessageResponse message) {
                if (message == null || partnerId == null) return;

                boolean related =
                        partnerId.equals(message.getSenderId())
                                || partnerId.equals(message.getReceiverId());

                if (!related) return;

                incomingMessage.postValue(message);
                markAsRead();
            }

            @Override
            public void onError(String message) {
                socketStatus.postValue("Đang kết nối lại...");
            }
        });
    }
    public void sendMessage(String content) {
        if (partnerId == null || partnerId <= 0) {
            sendMessageState.setValue(SendMessageState.error("Không tìm thấy người nhận"));
            return;
        }

        if (content == null || content.trim().isEmpty()) {
            sendMessageState.setValue(SendMessageState.error("Vui lòng nhập tin nhắn"));
            return;
        }

        String safeContent = content.trim();

        sendMessageState.setValue(SendMessageState.loading());

        if (repository.isWebSocketConnected()) {
            repository.sendMessageByWebSocket(partnerId, safeContent);
            sendMessageState.postValue(SendMessageState.success(null, "Đã gửi"));
            return;
        }

        repository.sendMessageByRest(
                partnerId,
                safeContent,
                new ChatRepository.DataCallback<ChatMessageResponse>() {
                    @Override
                    public void onSuccess(ChatMessageResponse data, String message) {
                        sendMessageState.postValue(SendMessageState.success(data, message));
                    }

                    @Override
                    public void onError(String message) {
                        sendMessageState.postValue(SendMessageState.error(message));
                    }
                }
        );
    }

    public void markAsRead() {
        if (partnerId == null || partnerId <= 0) return;
        repository.markAsRead(partnerId);
    }

    public void disconnectSocket() {
        repository.disconnectWebSocket();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.disconnectWebSocket();
    }
}