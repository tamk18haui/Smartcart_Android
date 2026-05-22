package com.gr6.smartcart_android.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.chat.adapter.MessageAdapter;
import com.gr6.smartcart_android.chat.model.ChatMessageResponse;
import com.gr6.smartcart_android.chat.model.SpringPageResponse;
import com.gr6.smartcart_android.chat.repository.ChatRepository;
import com.gr6.smartcart_android.chat.websocket.ChatWebSocketClient;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.storage.UserSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomActivity extends AppCompatActivity {

    public static final String EXTRA_PARTNER_ID = "extra_partner_id";
    public static final String EXTRA_PARTNER_NAME = "extra_partner_name";
    public static final String EXTRA_PARTNER_AVATAR = "extra_partner_avatar";

    private TextView btnBack;
    private TextView tvPartnerName;
    private TextView tvConnectionStatus;
    private RecyclerView rvMessages;
    private ProgressBar progressBar;
    private EditText edtMessage;
    private TextView btnSend;

    private MessageAdapter adapter;
    private ChatRepository repository;
    private ChatWebSocketClient webSocketClient;

    private final List<ChatMessageResponse> messages = new ArrayList<>();

    private Long currentUserId;
    private Long partnerId;
    private String partnerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        currentUserId = UserSession.getInstance(this).getUserId();
        partnerId = getIntent().getLongExtra(EXTRA_PARTNER_ID, -1L);
        if (partnerId == -1L) partnerId = null;

        partnerName = getIntent().getStringExtra(EXTRA_PARTNER_NAME);
        if (TextUtils.isEmpty(partnerName)) {
            partnerName = "Khách hàng";
        }

        if (currentUserId == null || partnerId == null) {
            Toast.makeText(this, "Thiếu thông tin người dùng để mở chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupRecyclerView();

        repository = new ChatRepository(this);

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendCurrentMessage());

        tvPartnerName.setText(partnerName);

        loadMessages();
        markConversationAsRead();
        connectWebSocket();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPartnerName = findViewById(R.id.tvPartnerName);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        rvMessages = findViewById(R.id.rvMessages);
        progressBar = findViewById(R.id.progressBar);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messages, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    private void loadMessages() {
        progressBar.setVisibility(View.VISIBLE);

        repository.getMessages(partnerId, 0, 50).enqueue(new Callback<BaseResponse<SpringPageResponse<ChatMessageResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<SpringPageResponse<ChatMessageResponse>>> call,
                                   @NonNull Response<BaseResponse<SpringPageResponse<ChatMessageResponse>>> response) {
                progressBar.setVisibility(View.GONE);

                BaseResponse<SpringPageResponse<ChatMessageResponse>> body = response.body();
                if (response.isSuccessful() && body != null && body.getData() != null) {
                    List<ChatMessageResponse> loaded = new ArrayList<>(body.getData().getContent());
                    Collections.reverse(loaded);

                    messages.clear();
                    messages.addAll(loaded);
                    adapter.notifyDataSetChanged();
                    scrollToBottom();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<SpringPageResponse<ChatMessageResponse>>> call,
                                  @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ChatRoomActivity.this, "Không tải được tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void connectWebSocket() {
        webSocketClient = new ChatWebSocketClient(this, new ChatWebSocketClient.Listener() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> tvConnectionStatus.setText("Đang online"));
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> tvConnectionStatus.setText("Mất kết nối realtime"));
            }

            @Override
            public void onMessage(ChatMessageResponse message) {
                runOnUiThread(() -> {
                    if (!isMessageOfCurrentRoom(message)) {
                        return;
                    }

                    addMessageIfNotExists(message);
                    scrollToBottom();

                    if (sameId(message.getSenderId(), partnerId)) {
                        markConversationAsRead();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> tvConnectionStatus.setText("Realtime chưa sẵn sàng"));
            }
        });

        webSocketClient.connect(currentUserId);
    }

    private void sendCurrentMessage() {
        String content = edtMessage.getText() == null ? "" : edtMessage.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        edtMessage.setText("");

        boolean sentBySocket = webSocketClient != null && webSocketClient.sendMessage(partnerId, content);
        if (!sentBySocket) {
            sendMessageByRest(content);
        }
    }

    private void sendMessageByRest(String content) {
        repository.sendMessage(partnerId, content).enqueue(new Callback<BaseResponse<ChatMessageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<ChatMessageResponse>> call,
                                   @NonNull Response<BaseResponse<ChatMessageResponse>> response) {
                BaseResponse<ChatMessageResponse> body = response.body();

                if (response.isSuccessful() && body != null && body.getData() != null) {
                    addMessageIfNotExists(body.getData());
                    scrollToBottom();
                } else {
                    Toast.makeText(ChatRoomActivity.this, "Không gửi được tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<ChatMessageResponse>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(ChatRoomActivity.this, "Không gửi được tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markConversationAsRead() {
        repository.markAsRead(partnerId).enqueue(new Callback<BaseResponse<Integer>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<Integer>> call,
                                   @NonNull Response<BaseResponse<Integer>> response) {
                // Không cần hiển thị gì cho người dùng.
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<Integer>> call,
                                  @NonNull Throwable t) {
                // Bỏ qua để không làm gián đoạn trải nghiệm chat.
            }
        });
    }

    private boolean isMessageOfCurrentRoom(ChatMessageResponse message) {
        if (message == null) return false;

        boolean partnerToMe = sameId(message.getSenderId(), partnerId) && sameId(message.getReceiverId(), currentUserId);
        boolean meToPartner = sameId(message.getSenderId(), currentUserId) && sameId(message.getReceiverId(), partnerId);

        return partnerToMe || meToPartner;
    }

    private void addMessageIfNotExists(ChatMessageResponse message) {
        if (message == null) return;

        Long messageId = message.getMessageId();
        if (messageId != null) {
            for (ChatMessageResponse item : messages) {
                if (messageId.equals(item.getMessageId())) {
                    return;
                }
            }
        }

        messages.add(message);
        adapter.notifyItemInserted(messages.size() - 1);
    }

    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            rvMessages.scrollToPosition(messages.size() - 1);
        }
    }

    private boolean sameId(Long a, Long b) {
        return a != null && b != null && a.equals(b);
    }
}
