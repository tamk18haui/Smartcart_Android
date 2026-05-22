package com.gr6.smartcart_android.buyer.chat.api;

import com.gr6.smartcart_android.buyer.chat.request.ChatMessageRequest;
import com.gr6.smartcart_android.buyer.chat.response.ChatMessageResponse;
import com.gr6.smartcart_android.buyer.chat.response.ChatMessagesPageResponse;
import com.gr6.smartcart_android.buyer.chat.response.ConversationResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatApiService {

    @GET("api/v1/chat/conversations")
    Call<BaseResponse<List<ConversationResponse>>> getConversations();

    @GET("api/v1/chat/messages/{partnerId}")
    Call<BaseResponse<ChatMessagesPageResponse>> getMessages(
            @Path("partnerId") Long partnerId,
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("api/v1/chat/messages")
    Call<BaseResponse<ChatMessageResponse>> sendMessage(@Body ChatMessageRequest request);

    @PATCH("api/v1/chat/messages/{partnerId}/read")
    Call<BaseResponse<Integer>> markAsRead(@Path("partnerId") Long partnerId);
}