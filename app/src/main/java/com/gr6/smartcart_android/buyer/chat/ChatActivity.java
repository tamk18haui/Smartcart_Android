package com.gr6.smartcart_android.buyer.chat;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.chat.response.ChatMessageResponse;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.util.List;

public class ChatActivity extends BaseActivity {

    public static final String EXTRA_PARTNER_ID = "partner_id";
    public static final String EXTRA_PARTNER_NAME = "partner_name";
    public static final String EXTRA_PARTNER_AVATAR = "partner_avatar";

    private ImageView imgBack;
    private ImageView imgPartnerAvatar;
    private ImageView imgSend;

    private TextView txtPartnerName;
    private TextView txtStatus;
    private TextView txtEmpty;

    private EditText edtMessage;
    private RecyclerView rcvMessages;

    private Long partnerId;
    private String partnerName;
    private String partnerAvatar;

    private ChatMessageAdapter adapter;
    private ChatViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        readExtras();

        if (partnerId == null || partnerId <= 0) {
            showToast("Không tìm thấy người nhận");
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.setPartnerId(partnerId);

        initViews();
        setupRecyclerView();
        initEvents();
        bindHeader();
        observeData();

        viewModel.loadMessages();
        viewModel.connectSocket();
        viewModel.markAsRead();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (viewModel != null) {
            viewModel.disconnectSocket();
        }
    }

    private void readExtras() {
        partnerId = getIntent().getLongExtra(EXTRA_PARTNER_ID, -1L);

        if (partnerId == -1L) {
            partnerId = null;
        }

        partnerName = getIntent().getStringExtra(EXTRA_PARTNER_NAME);
        partnerAvatar = getIntent().getStringExtra(EXTRA_PARTNER_AVATAR);
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        imgPartnerAvatar = findViewById(R.id.imgPartnerAvatar);
        imgSend = findViewById(R.id.imgSend);

        txtPartnerName = findViewById(R.id.txtPartnerName);
        txtStatus = findViewById(R.id.txtStatus);
        txtEmpty = findViewById(R.id.txtEmpty);

        edtMessage = findViewById(R.id.edtMessage);
        rcvMessages = findViewById(R.id.rcvMessages);
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter();
        adapter.setCurrentUserId(UserSession.getInstance(this).getUserId());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        rcvMessages.setLayoutManager(layoutManager);
        rcvMessages.setAdapter(adapter);
    }

    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());

        imgSend.setOnClickListener(v -> {
            String content = edtMessage.getText().toString().trim();

            if (content.isEmpty()) {
                showToast("Vui lòng nhập tin nhắn");
                return;
            }

            edtMessage.setText("");
            hideKeyboard();

            viewModel.sendMessage(content);
        });
    }

    private void bindHeader() {
        if (partnerName == null || partnerName.trim().isEmpty()) {
            txtPartnerName.setText("Người dùng SmartCart");
        } else {
            txtPartnerName.setText(partnerName);
        }

        if (partnerAvatar == null || partnerAvatar.trim().isEmpty()) {
            imgPartnerAvatar.setImageResource(R.drawable.ic_user);
        } else {
            ImageLoader.loadCircle(this, partnerAvatar, imgPartnerAvatar);
        }
    }

    private void observeData() {
        viewModel.getMessagesState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                bindMessages(state.getData());
            } else {
                txtEmpty.setVisibility(View.VISIBLE);
                txtEmpty.setText("Hãy gửi lời chào đầu tiên cho shop");
            }
        });

        viewModel.getIncomingMessage().observe(this, message -> {
            if (message == null) return;

            adapter.addMessage(message);
            updateEmptyState();
            scrollToBottom();
        });

        viewModel.getSendMessageState().observe(this, state -> {
            if (state == null) return;

            // Quan trọng: loading không phải lỗi
            if (state.isLoading()) {
                return;
            }

            if (!state.isSuccess()) {
                showLongToast(state.getMessage());
                return;
            }

            ChatMessageResponse sentMessage = state.getData();

            if (sentMessage != null) {
                adapter.addMessage(sentMessage);
                updateEmptyState();
                scrollToBottom();
            }
        });

        viewModel.getSocketStatus().observe(this, status -> {
            if (status == null || status.trim().isEmpty()) {
                txtStatus.setText("Đang kết nối...");
            } else {
                txtStatus.setText(status);
            }
        });
    }

    private void bindMessages(List<ChatMessageResponse> messages) {
        adapter.setData(messages);
        updateEmptyState();
        scrollToBottom();
    }

    private void updateEmptyState() {
        txtEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            rcvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(edtMessage.getWindowToken(), 0);
            }
        } catch (Exception ignored) {
        }
    }
}