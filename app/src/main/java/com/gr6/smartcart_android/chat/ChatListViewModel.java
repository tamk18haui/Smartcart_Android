package com.gr6.smartcart_android.chat;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.chat.repository.ChatRepository;
import com.gr6.smartcart_android.chat.response.ConversationResponse;

import java.util.List;

public class ChatListViewModel extends AndroidViewModel {

    private final ChatRepository repository;
    private final MutableLiveData<ChatListState> conversationsState = new MutableLiveData<>();

    public ChatListViewModel(@NonNull Application application) {
        super(application);
        repository = new ChatRepository(application);
    }

    public LiveData<ChatListState> getConversationsState() {
        return conversationsState;
    }

    public void loadConversations(boolean showLoading) {
        if (showLoading) {
            conversationsState.setValue(ChatListState.loading());
        }

        repository.getConversations(new ChatRepository.DataCallback<List<ConversationResponse>>() {
            @Override
            public void onSuccess(List<ConversationResponse> data, String message) {
                conversationsState.postValue(ChatListState.success(data, message));
            }

            @Override
            public void onError(String message) {
                conversationsState.postValue(ChatListState.error(message));
            }
        });
    }
}