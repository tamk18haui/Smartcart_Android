package com.gr6.smartcart_android.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.chat.adapter.ConversationAdapter;
import com.gr6.smartcart_android.chat.model.ConversationResponse;
import com.gr6.smartcart_android.chat.repository.ChatRepository;
import com.gr6.smartcart_android.common.base.BaseResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerConversationsFragment extends Fragment {

    private RecyclerView rvConversations;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView btnRetry;

    private ConversationAdapter adapter;
    private ChatRepository repository;
    private final List<ConversationResponse> conversations = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_conversations, container, false);

        rvConversations = view.findViewById(R.id.rvConversations);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        btnRetry = view.findViewById(R.id.btnRetry);

        repository = new ChatRepository(requireContext());

        adapter = new ConversationAdapter(conversations, conversation -> {
            Intent intent = new Intent(requireContext(), ChatRoomActivity.class);
            intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_ID, conversation.getPartnerId());
            intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_NAME, conversation.getPartnerName());
            intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_AVATAR, conversation.getPartnerAvatarUrl());
            startActivity(intent);
        });

        rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvConversations.setAdapter(adapter);

        btnRetry.setOnClickListener(v -> loadConversations());

        loadConversations();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadConversations();
    }

    private void loadConversations() {
        showLoading(true);

        repository.getConversations().enqueue(new Callback<BaseResponse<List<ConversationResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<List<ConversationResponse>>> call,
                                   @NonNull Response<BaseResponse<List<ConversationResponse>>> response) {
                if (!isAdded()) return;

                showLoading(false);

                BaseResponse<List<ConversationResponse>> body = response.body();
                if (response.isSuccessful() && body != null && body.getData() != null) {
                    conversations.clear();
                    conversations.addAll(body.getData());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                } else {
                    showError(body == null ? "Không tải được cuộc trò chuyện" : body.getSafeMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<List<ConversationResponse>>> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;

                showLoading(false);
                showError(t.getMessage() == null ? "Không kết nối được máy chủ" : t.getMessage());
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            tvEmpty.setVisibility(View.GONE);
            btnRetry.setVisibility(View.GONE);
        }
    }

    private void updateEmptyState() {
        boolean empty = conversations.isEmpty();
        tvEmpty.setText("Chưa có cuộc trò chuyện nào");
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        btnRetry.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvConversations.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        conversations.clear();
        adapter.notifyDataSetChanged();

        rvConversations.setVisibility(View.GONE);
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
    }
}
