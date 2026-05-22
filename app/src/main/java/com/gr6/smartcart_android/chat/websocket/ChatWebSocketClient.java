package com.gr6.smartcart_android.chat.websocket;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.gr6.smartcart_android.chat.model.ChatMessageRequest;
import com.gr6.smartcart_android.chat.model.ChatMessageResponse;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.utils.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatWebSocketClient {

    public interface Listener {
        void onConnected();

        void onDisconnected();

        void onMessage(ChatMessageResponse message);

        void onError(String message);
    }

    private final Context appContext;
    private final Listener listener;
    private final Gson gson = new Gson();

    private OkHttpClient client;
    private WebSocket webSocket;
    private String token;
    private Long currentUserId;
    private boolean connected;

    public ChatWebSocketClient(Context context, Listener listener) {
        this.appContext = context.getApplicationContext();
        this.listener = listener;
    }

    public void connect(Long currentUserId) {
        this.currentUserId = currentUserId;
        this.token = TokenManager.getInstance(appContext).getToken();

        if (currentUserId == null) {
            notifyError("Không tìm thấy userId trong phiên đăng nhập");
            return;
        }

        if (TextUtils.isEmpty(token)) {
            notifyError("Bạn cần đăng nhập để sử dụng chat");
            return;
        }

        disconnect();

        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(buildWebSocketUrl(token))
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket socket, @NonNull Response response) {
                sendConnectFrame(socket);
            }

            @Override
            public void onMessage(@NonNull WebSocket socket, @NonNull String text) {
                handleStompPayload(text);
            }

            @Override
            public void onClosed(@NonNull WebSocket socket, int code, @NonNull String reason) {
                connected = false;
                if (listener != null) listener.onDisconnected();
            }

            @Override
            public void onFailure(@NonNull WebSocket socket, @NonNull Throwable t, @Nullable Response response) {
                connected = false;
                notifyError(t.getMessage() == null ? "Không kết nối được WebSocket chat" : t.getMessage());
                if (listener != null) listener.onDisconnected();
            }
        });
    }

    public boolean sendMessage(Long receiverId, String content) {
        if (!connected || webSocket == null || receiverId == null || TextUtils.isEmpty(content)) {
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
                + "\u0000";

        return webSocket.send(frame);
    }

    public void disconnect() {
        connected = false;

        if (webSocket != null) {
            try {
                webSocket.send("DISCONNECT\n\n\u0000");
                webSocket.close(1000, "Seller chat closed");
            } catch (Exception ignored) {
            }
            webSocket = null;
        }

        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client = null;
        }
    }

    private String buildWebSocketUrl(String token) {
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
                + "heart-beat:0,0\n"
                + "Authorization:Bearer " + token + "\n"
                + "\n"
                + "\u0000";
        socket.send(frame);
    }

    private void subscribeMyTopic() {
        if (webSocket == null || currentUserId == null) return;

        String frame = "SUBSCRIBE\n"
                + "id:seller-chat-" + currentUserId + "\n"
                + "destination:/topic/chat/" + currentUserId + "\n"
                + "\n"
                + "\u0000";

        webSocket.send(frame);
    }

    private void handleStompPayload(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return;
        }

        String[] frames = payload.split("\u0000");

        for (String frame : frames) {
            if (frame == null || frame.trim().isEmpty()) {
                continue;
            }

            if (frame.startsWith("CONNECTED")) {
                connected = true;
                subscribeMyTopic();
                if (listener != null) listener.onConnected();
            } else if (frame.startsWith("MESSAGE")) {
                String body = extractBody(frame);
                if (!TextUtils.isEmpty(body)) {
                    try {
                        ChatMessageResponse message = gson.fromJson(body, ChatMessageResponse.class);
                        if (listener != null && message != null) {
                            listener.onMessage(message);
                        }
                    } catch (Exception e) {
                        notifyError("Không đọc được tin nhắn realtime");
                    }
                }
            } else if (frame.startsWith("ERROR")) {
                notifyError(extractBody(frame));
            }
        }
    }

    private String extractBody(String frame) {
        int index = frame.indexOf("\n\n");
        if (index < 0) {
            return "";
        }

        String body = frame.substring(index + 2);
        return body.replace("\u0000", "").trim();
    }

    private void notifyError(String message) {
        if (listener != null) {
            listener.onError(TextUtils.isEmpty(message) ? "Có lỗi khi kết nối chat" : message);
        }
    }
}
