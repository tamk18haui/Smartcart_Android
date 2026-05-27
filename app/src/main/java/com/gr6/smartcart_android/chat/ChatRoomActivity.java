package com.gr6.smartcart_android.chat;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.chat.response.ChatMessageResponse;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.cloudinary.CloudinaryRepository;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.common.utils.ThemeColor;

public class ChatRoomActivity extends BaseActivity {

    public static final String EXTRA_PARTNER_ID = "partner_id";
    public static final String EXTRA_PARTNER_NAME = "partner_name";
    public static final String EXTRA_PARTNER_AVATAR = "partner_avatar";

    private ImageView imgBack;
    private ImageView imgPartnerAvatar;
    private TextView txtPartnerName;
    private TextView txtSocketStatus;
    private RecyclerView rcvMessages;
    private EditText edtMessage;
    private ImageView btnPickImage;
    private ImageView btnSend;
    private View layoutEmpty;

    private ChatRoomViewModel viewModel;
    private ChatMessageAdapter adapter;
    private CloudinaryRepository cloudinaryRepository;
    private ActivityResultLauncher<String> pickImageLauncher;

    private Long partnerId;
    private String partnerName;
    private String partnerAvatar;
    private boolean uploadingImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        viewModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        cloudinaryRepository = new CloudinaryRepository();
        setupImagePicker();

        readIntent();
        initViews();
        setupRecyclerView();
        initEvents();
        observeData();

        viewModel.init(partnerId);
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    uploadAndSendImage(uri);
                }
        );
    }

    private void readIntent() {
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
        txtPartnerName = findViewById(R.id.txtPartnerName);
        txtSocketStatus = findViewById(R.id.txtSocketStatus);
        rcvMessages = findViewById(R.id.rcvMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnSend = findViewById(R.id.btnSend);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        txtPartnerName.setText(
                partnerName == null || partnerName.trim().isEmpty()
                        ? "SmartCart Chat"
                        : partnerName.trim()
        );

        if (partnerAvatar == null || partnerAvatar.trim().isEmpty()) {
            imgPartnerAvatar.setImageResource(R.drawable.ic_user);
        } else {
            ImageLoader.loadCircle(this, partnerAvatar, imgPartnerAvatar);
        }
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

        btnPickImage.setOnClickListener(v -> {
            hideKeyboard();
            pickImageLauncher.launch("image/*");
        });

        btnSend.setOnClickListener(v -> sendMessage());

        edtMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
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
                adapter.setData(state.getData());
                updateEmptyState(state.getData().isEmpty());
                scrollToBottom();
            } else {
                showLongToast(state.getMessage());
                updateEmptyState(true);
            }
        });

        viewModel.getSendMessageState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                setSendControlsEnabled(false);
                return;
            }

            uploadingImage = false;
            hideLoading();
            setSendControlsEnabled(true);

            if (state.isSuccess()) {
                edtMessage.setText("");

                ChatMessageResponse restMessage = state.getData();

                if (restMessage != null) {
                    adapter.addMessage(restMessage);
                    updateEmptyState(false);
                    scrollToBottom();
                }
            } else {
                showToast(state.getMessage());
            }
        });

        viewModel.getIncomingMessage().observe(this, message -> {
            if (message == null) return;

            adapter.addMessage(message);
            updateEmptyState(false);
            scrollToBottom();
        });

        viewModel.getSocketStatus().observe(this, status -> {
            if (status == null || status.trim().isEmpty()) {
                txtSocketStatus.setText("");
            } else {
                txtSocketStatus.setText(status);
            }
        });
    }

    private void sendMessage() {
        String content = edtMessage.getText().toString().trim();

        if (content.isEmpty()) {
            showToast("Nhập tin nhắn trước đã");
            return;
        }

        viewModel.sendMessage(content);
        hideKeyboard();
    }

    private void uploadAndSendImage(Uri imageUri) {
        uploadingImage = true;
        setSendControlsEnabled(false);
        showLoading();

        cloudinaryRepository.uploadImage(this, imageUri, new CloudinaryRepository.UploadCallback() {
            @Override
            public void onSuccess(String mediaUrl) {
                runOnUiThread(() -> {
                    if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
                        uploadingImage = false;
                        hideLoading();
                        setSendControlsEnabled(true);
                        showToast("Cloudinary không trả link ảnh");
                        return;
                    }

                    viewModel.sendImage(mediaUrl.trim());
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    uploadingImage = false;
                    hideLoading();
                    setSendControlsEnabled(true);
                    showLongToast(message == null ? "Upload ảnh thất bại" : message);
                });
            }
        });
    }

    private void setSendControlsEnabled(boolean enabled) {
        btnSend.setEnabled(enabled);
        btnSend.setAlpha(enabled ? 1f : 0.55f);

        btnPickImage.setEnabled(enabled);
        btnPickImage.setAlpha(enabled ? 1f : 0.55f);

        edtMessage.setEnabled(enabled || uploadingImage);
    }

    private void updateEmptyState(boolean empty) {
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rcvMessages.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void scrollToBottom() {
        rcvMessages.post(() -> {
            int count = adapter.getItemCount();

            if (count > 0) {
                rcvMessages.smoothScrollToPosition(count - 1);
            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.disconnectSocket();
    }
}
