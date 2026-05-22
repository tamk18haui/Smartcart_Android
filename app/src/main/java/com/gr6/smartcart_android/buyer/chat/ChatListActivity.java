package com.gr6.smartcart_android.buyer.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.chat.response.ConversationResponse;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.util.List;

public class ChatListActivity extends BaseActivity {

    private ImageView imgBack;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rcvConversations;
    private TextView txtEmpty;

    private ChatConversationAdapter adapter;
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

        viewModel.loadConversations();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (viewModel != null) {
            viewModel.refreshConversations();
        }
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rcvConversations = findViewById(R.id.rcvConversations);
        txtEmpty = findViewById(R.id.txtEmpty);
    }

    private void setupRecyclerView() {
        adapter = new ChatConversationAdapter();

        adapter.setListener(conversation -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_PARTNER_ID, conversation.getPartnerId());
            intent.putExtra(ChatActivity.EXTRA_PARTNER_NAME, conversation.getPartnerName());
            intent.putExtra(ChatActivity.EXTRA_PARTNER_AVATAR, conversation.getPartnerAvatarUrl());
            startActivity(intent);
        });

        rcvConversations.setLayoutManager(new LinearLayoutManager(this));
        rcvConversations.setAdapter(adapter);
    }

    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());

        setupSwipeRefresh(swipeRefresh, () -> viewModel.refreshConversations());
    }

    private void observeData() {
        viewModel.getConversationsState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();
            stopSwipeRefresh(swipeRefresh);

            if (state.isSuccess()) {
                bindConversations(state.getData());
            } else {
                txtEmpty.setVisibility(View.VISIBLE);
                showLongToast(state.getMessage());
            }
        });
    }

    private void bindConversations(List<ConversationResponse> conversations) {
        adapter.setData(conversations);

        if (conversations == null || conversations.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
        } else {
            txtEmpty.setVisibility(View.GONE);
        }
    }
}