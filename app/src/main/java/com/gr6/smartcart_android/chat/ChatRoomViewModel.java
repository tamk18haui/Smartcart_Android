package com.gr6.smartcart_android.chat;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.chat.repository.ChatRepository;
import com.gr6.smartcart_android.chat.response.ChatMessageResponse;
import com.gr6.smartcart_android.chat.response.ChatMessagesPageResponse;
import com.gr6.smartcart_android.chat.websocket.ChatWebSocketClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatRoomViewModel extends AndroidViewModel {

    private static final int PAGE = 0;
    private static final int SIZE = 50;

    private final ChatRepository repository;

    private final MutableLiveData<ChatMessagesState> messagesState = new MutableLiveData<>();
    private final MutableLiveData<SendMessageState> sendMessageState = new MutableLiveData<>();
    private final MutableLiveData<ChatMessageResponse> incomingMessage = new MutableLiveData<>();
    private final MutableLiveData<String> socketStatus = new MutableLiveData<>();

    private Long partnerId;

    public ChatRoomViewModel(@NonNull Application application) {
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

    public void init(Long partnerId) {
        this.partnerId = partnerId;

        loadMessages(true);
        markAsRead();
        connectSocket();
    }

    public void loadMessages(boolean showLoading) {
        if (partnerId == null || partnerId <= 0) {
            messagesState.setValue(ChatMessagesState.error("Không tìm thấy người cần nhắn tin"));
            return;
        }

        if (showLoading) {
            messagesState.setValue(ChatMessagesState.loading());
        }

        repository.getMessages(
                partnerId,
                PAGE,
                SIZE,
                new ChatRepository.DataCallback<ChatMessagesPageResponse>() {
                    @Override
                    public void onSuccess(ChatMessagesPageResponse data, String message) {
                        List<ChatMessageResponse> messages = data == null
                                ? new ArrayList<>()
                                : new ArrayList<>(data.getContent());

                        Collections.sort(messages, (a, b) -> {
                            long idA = a.getMessageId() == null ? 0L : a.getMessageId();
                            long idB = b.getMessageId() == null ? 0L : b.getMessageId();
                            return Long.compare(idA, idB);
                        });

                        messagesState.postValue(ChatMessagesState.success(messages, message));
                    }

                    @Override
                    public void onError(String message) {
                        messagesState.postValue(ChatMessagesState.error(message));
                    }
                }
        );
    }

    private void connectSocket() {
        socketStatus.setValue("Đang kết nối...");

        repository.connectWebSocket(new ChatWebSocketClient.Listener() {
            @Override
            public void onConnected() {
                socketStatus.postValue("Đang hoạt động");
            }

            @Override
            public void onDisconnected() {
                socketStatus.postValue("Mất kết nối");
            }

            @Override
            public void onMessageReceived(ChatMessageResponse message) {
                if (isMessageOfCurrentRoom(message)) {
                    incomingMessage.postValue(message);
                    markAsRead();
                }
            }

            @Override
            public void onError(String message) {
                socketStatus.postValue("Lỗi kết nối");
            }
        });
    }

    public void sendMessage(String content) {
        if (partnerId == null || partnerId <= 0) {
            sendMessageState.setValue(SendMessageState.error("Không tìm thấy người nhận"));
            return;
        }

        if (content == null || content.trim().isEmpty()) {
            sendMessageState.setValue(SendMessageState.error("Tin nhắn không được để trống"));
            return;
        }

        sendMessageState.setValue(SendMessageState.loading());

        boolean sentBySocket = repository.sendMessageBySocket(partnerId, content.trim());

        if (sentBySocket) {
            // Khi gửi bằng WebSocket, backend sẽ bắn lại message qua topic của sender.
            // Vì vậy data để null để Activity không add trùng.
            sendMessageState.setValue(SendMessageState.success(null, "Đã gửi"));
            return;
        }

        // Fallback REST nếu WebSocket chưa kết nối
        repository.sendMessageByRest(
                partnerId,
                content.trim(),
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
        repository.markAsRead(partnerId);
    }

    public void disconnectSocket() {
        repository.disconnectWebSocket();
    }

    private boolean isMessageOfCurrentRoom(ChatMessageResponse message) {
        if (message == null || partnerId == null) return false;

        Long senderId = message.getSenderId();
        Long receiverId = message.getReceiverId();

        return partnerId.equals(senderId) || partnerId.equals(receiverId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.disconnectWebSocket();
    }
}