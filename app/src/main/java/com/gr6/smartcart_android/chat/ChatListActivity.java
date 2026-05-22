package com.gr6.smartcart_android.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.chat.response.ConversationResponse;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

public class ChatListActivity extends BaseActivity {

    private ImageView imgBack;
    private SwipeRefreshLayout swipeChatList;
    private RecyclerView rcvConversations;
    private LinearLayout layoutEmpty;
    private TextView txtEmpty;

    private ConversationAdapter adapter;
    private ChatListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);

        initViews();
        setupRecyclerView();
        initEvents();
        observeData();

        viewModel.loadConversations(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (viewModel != null) {
            viewModel.loadConversations(false);
        }
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        swipeChatList = findViewById(R.id.swipeChatList);
        rcvConversations = findViewById(R.id.rcvConversations);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        txtEmpty = findViewById(R.id.txtEmpty);
    }

    private void setupRecyclerView() {
        adapter = new ConversationAdapter(this);
        adapter.setOnConversationClickListener(this::openChatRoom);

        rcvConversations.setLayoutManager(new LinearLayoutManager(this));
        rcvConversations.setAdapter(adapter);
    }

    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());

        swipeChatList.setOnRefreshListener(() ->
                viewModel.loadConversations(false)
        );
    }

    private void observeData() {
        viewModel.getConversationsState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();
            swipeChatList.setRefreshing(false);

            if (state.isSuccess()) {
                adapter.setData(state.getData());
                updateEmptyState(state.getData().isEmpty(), "Chưa có cuộc trò chuyện nào");
            } else {
                adapter.setData(null);
                updateEmptyState(true, state.getMessage());
            }
        });
    }

    private void updateEmptyState(boolean empty, String message) {
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rcvConversations.setVisibility(empty ? View.GONE : View.VISIBLE);
        txtEmpty.setText(message == null ? "Không có dữ liệu" : message);
    }

    private void openChatRoom(ConversationResponse conversation) {
        if (conversation == null || conversation.getPartnerId() == null) {
            showToast("Không tìm thấy người cần nhắn tin");
            return;
        }

        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_ID, conversation.getPartnerId());
        intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_NAME, conversation.getPartnerName());
        intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_AVATAR, conversation.getPartnerAvatarUrl());
        startActivity(intent);
    }
}