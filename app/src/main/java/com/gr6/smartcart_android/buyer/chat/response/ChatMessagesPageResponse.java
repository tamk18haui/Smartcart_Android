package com.gr6.smartcart_android.buyer.chat.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ChatMessagesPageResponse {

    @SerializedName("content")
    private List<ChatMessageResponse> content;

    @SerializedName("totalPages")
    private Integer totalPages;

    @SerializedName("totalElements")
    private Long totalElements;

    @SerializedName("number")
    private Integer number;

    @SerializedName("size")
    private Integer size;

    @SerializedName("last")
    private Boolean last;

    public List<ChatMessageResponse> getContent() {
        return content == null ? new ArrayList<>() : content;
    }

    public int getTotalPages() {
        return totalPages == null ? 0 : totalPages;
    }

    public long getTotalElements() {
        return totalElements == null ? 0 : totalElements;
    }

    public int getNumber() {
        return number == null ? 0 : number;
    }

    public int getSize() {
        return size == null ? 0 : size;
    }

    public boolean isLast() {
        return last != null && last;
    }
}