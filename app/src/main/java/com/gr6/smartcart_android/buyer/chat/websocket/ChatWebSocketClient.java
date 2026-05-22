package com.gr6.smartcart_android.buyer.chat.websocket;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.gr6.smartcart_android.buyer.chat.request.ChatMessageRequest;
import com.gr6.smartcart_android.buyer.chat.response.ChatMessageResponse;
import com.gr6.smartcart_android.common.utils.Constants;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatWebSocketClient {

    private static final String NULL = String.valueOf((char) 0);

    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final OkHttpClient client;

    private WebSocket webSocket;
    private Listener listener;

    private boolean connected;
    private String token;
    private Long currentUserId;

    public ChatWebSocketClient() {
        client = new OkHttpClient.Builder()
                .pingInterval(20, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void connect(String token, Long currentUserId) {
        if (token == null || token.trim().isEmpty()) {
            notifyError("Bạn cần đăng nhập để chat");
            return;
        }

        if (currentUserId == null || currentUserId <= 0) {
            notifyError("Không tìm thấy user hiện tại");
            return;
        }

        this.token = token;
        this.currentUserId = currentUserId;

        disconnect();

        String wsUrl = buildWebSocketUrl(Constants.BASE_URL);

        Request request = new Request.Builder()
                .url(wsUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                String frame = "CONNECT\n"
                        + "accept-version:1.2\n"
                        + "heart-beat:10000,10000\n"
                        + "\n"
                        + NULL;

                webSocket.send(frame);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                handleFrame(text);
            }

            @Override
            public void onFailure(
                    @NonNull WebSocket webSocket,
                    @NonNull Throwable t,
                    @Nullable Response response
            ) {
                connected = false;
                notifyError("WebSocket lỗi: " + t.getMessage());
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                connected = false;
            }
        });
    }

    public boolean isConnected() {
        return connected;
    }

    public void sendMessage(Long receiverId, String content) {
        if (webSocket == null || !connected) {
            notifyError("WebSocket chưa kết nối");
            return;
        }

        if (token == null || token.trim().isEmpty()) {
            notifyError("Bạn cần đăng nhập để chat");
            return;
        }

        ChatMessageRequest request = new ChatMessageRequest(receiverId, content);
        String body = gson.toJson(request);

        String frame = "SEND\n"
                + "destination:/app/chat.send\n"
                + "content-type:application/json\n"
                + "Authorization:Bearer " + token + "\n"
                + "\n"
                + body
                + NULL;

        webSocket.send(frame);
    }

    public void disconnect() {
        connected = false;

        if (webSocket != null) {
            webSocket.close(1000, "close");
            webSocket = null;
        }
    }

    private void handleFrame(String rawFrame) {
        if (rawFrame == null) return;

        String[] frames = rawFrame.split(Pattern.quote(NULL));

        for (String frame : frames) {
            if (frame == null || frame.trim().isEmpty()) continue;

            if (frame.startsWith("CONNECTED")) {
                connected = true;
                subscribeMyChatTopic();
                notifyConnected();
                continue;
            }

            if (frame.startsWith("MESSAGE")) {
                String body = extractBody(frame);

                if (body == null || body.trim().isEmpty()) continue;

                try {
                    ChatMessageResponse message =
                            gson.fromJson(body, ChatMessageResponse.class);

                    notifyMessage(message);
                } catch (Exception e) {
                    notifyError("Không đọc được tin nhắn: " + e.getMessage());
                }

                continue;
            }

            if (frame.startsWith("ERROR")) {
                notifyError(extractBody(frame));
            }
        }
    }

    private void subscribeMyChatTopic() {
        if (webSocket == null || currentUserId == null) return;

        String frame = "SUBSCRIBE\n"
                + "id:smartcart-chat-" + currentUserId + "\n"
                + "destination:/topic/chat/" + currentUserId + "\n"
                + "\n"
                + NULL;

        webSocket.send(frame);
    }

    private String extractBody(String frame) {
        int splitIndex = frame.indexOf("\n\n");

        if (splitIndex < 0) {
            return "";
        }

        return frame.substring(splitIndex + 2)
                .replace(NULL, "")
                .trim();
    }

    private String buildWebSocketUrl(String baseUrl) {
        String url = baseUrl == null ? "" : baseUrl.trim();

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        if (url.startsWith("https://")) {
            url = "wss://" + url.substring("https://".length());
        } else if (url.startsWith("http://")) {
            url = "ws://" + url.substring("http://".length());
        }

        return url + "/ws-chat";
    }

    private void notifyConnected() {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onConnected();
            }
        });
    }

    private void notifyMessage(ChatMessageResponse message) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onMessage(message);
            }
        });
    }

    private void notifyError(String message) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onError(message == null ? "Lỗi WebSocket" : message);
            }
        });
    }

    public interface Listener {
        void onConnected();

        void onMessage(ChatMessageResponse message);

        void onError(String message);
    }
}