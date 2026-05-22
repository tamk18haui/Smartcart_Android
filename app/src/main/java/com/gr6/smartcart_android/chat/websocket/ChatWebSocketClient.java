package com.gr6.smartcart_android.chat.websocket;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.gr6.smartcart_android.chat.request.ChatMessageRequest;
import com.gr6.smartcart_android.chat.response.ChatMessageResponse;
import com.gr6.smartcart_android.common.utils.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatWebSocketClient {

    private static final String END = String.valueOf((char) 0);

    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final OkHttpClient client;

    private WebSocket webSocket;
    private Listener listener;

    private String token;
    private Long currentUserId;
    private boolean connected = false;

    public ChatWebSocketClient() {
        client = new OkHttpClient.Builder()
                .pingInterval(20, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isConnected() {
        return connected;
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

        this.token = token.trim();
        this.currentUserId = currentUserId;

        disconnect();

        Request request = new Request.Builder()
                .url(buildWebSocketUrl())
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket socket, @NonNull Response response) {
                sendConnectFrame(socket);
            }

            @Override
            public void onMessage(@NonNull WebSocket socket, @NonNull String text) {
                handleFrame(text);
            }

            @Override
            public void onClosed(@NonNull WebSocket socket, int code, @NonNull String reason) {
                connected = false;
                notifyDisconnected();
            }

            @Override
            public void onFailure(
                    @NonNull WebSocket socket,
                    @NonNull Throwable t,
                    @Nullable Response response
            ) {
                connected = false;
                notifyError(t.getMessage() == null ? "Không kết nối được WebSocket" : t.getMessage());
                notifyDisconnected();
            }
        });
    }

    public boolean sendMessage(Long receiverId, String content) {
        if (receiverId == null || receiverId <= 0) {
            notifyError("Không tìm thấy người nhận");
            return false;
        }

        if (content == null || content.trim().isEmpty()) {
            notifyError("Tin nhắn không được để trống");
            return false;
        }

        if (!connected || webSocket == null) {
            notifyError("WebSocket chưa kết nối");
            return false;
        }

        ChatMessageRequest request = new ChatMessageRequest(receiverId, content.trim());
        String body = gson.toJson(request);

        String frame = "SEND\n"
                + "destination:/app/chat.send\n"
                + "Authorization:Bearer " + token + "\n"
                + "content-type:application/json\n"
                + "\n"
                + body
                + END;

        return webSocket.send(frame);
    }

    public void disconnect() {
        connected = false;

        if (webSocket != null) {
            try {
                webSocket.send("DISCONNECT\n\n" + END);
                webSocket.close(1000, "Chat closed");
            } catch (Exception ignored) {
            }

            webSocket = null;
        }
    }

    private String buildWebSocketUrl() {
        String base = Constants.BASE_URL == null ? "" : Constants.BASE_URL.trim();

        if (base.startsWith("https://")) {
            base = "wss://" + base.substring("https://".length());
        } else if (base.startsWith("http://")) {
            base = "ws://" + base.substring("http://".length());
        }

        if (!base.endsWith("/")) {
            base = base + "/";
        }

        String encodedToken;

        try {
            encodedToken = URLEncoder.encode(token, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encodedToken = token;
        }

        return base + "ws-chat?token=" + encodedToken;
    }

    private void sendConnectFrame(WebSocket socket) {
        String frame = "CONNECT\n"
                + "accept-version:1.2\n"
                + "heart-beat:10000,10000\n"
                + "Authorization:Bearer " + token + "\n"
                + "\n"
                + END;

        socket.send(frame);
    }

    private void subscribeMyTopic() {
        if (webSocket == null || currentUserId == null) return;

        String frame = "SUBSCRIBE\n"
                + "id:smartcart-chat-" + currentUserId + "\n"
                + "destination:/topic/chat/" + currentUserId + "\n"
                + "\n"
                + END;

        webSocket.send(frame);
    }

    private void handleFrame(String rawFrame) {
        if (rawFrame == null || rawFrame.trim().isEmpty()) {
            return;
        }

        String[] frames = rawFrame.split(Pattern.quote(END));

        for (String frame : frames) {
            if (frame == null || frame.trim().isEmpty()) continue;

            if (frame.startsWith("CONNECTED")) {
                connected = true;
                subscribeMyTopic();
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
                    notifyError("Không đọc được tin nhắn realtime");
                }

                continue;
            }

            if (frame.startsWith("ERROR")) {
                notifyError(extractBody(frame));
            }
        }
    }

    private String extractBody(String frame) {
        if (frame == null) return "";

        int index = frame.indexOf("\n\n");

        if (index < 0) {
            return "";
        }

        String body = frame.substring(index + 2);

        if (body.endsWith(END)) {
            body = body.substring(0, body.length() - 1);
        }

        return body.trim();
    }

    private void notifyConnected() {
        mainHandler.post(() -> {
            if (listener != null) listener.onConnected();
        });
    }

    private void notifyDisconnected() {
        mainHandler.post(() -> {
            if (listener != null) listener.onDisconnected();
        });
    }

    private void notifyMessage(ChatMessageResponse message) {
        mainHandler.post(() -> {
            if (listener != null) listener.onMessageReceived(message);
        });
    }

    private void notifyError(String message) {
        mainHandler.post(() -> {
            if (listener != null) listener.onError(message);
        });
    }

    public interface Listener {
        void onConnected();

        void onDisconnected();

        void onMessageReceived(ChatMessageResponse message);

        void onError(String message);
    }
}