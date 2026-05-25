package com.gr6.smartcart_android.buyer.notification.api;

import com.gr6.smartcart_android.buyer.notification.response.BuyerNotificationResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface BuyerNotificationApiService {

    @GET("api/v1/notifications")
    Call<BaseResponse<List<BuyerNotificationResponse>>> getNotifications();

    @GET("api/v1/notifications/unread-count")
    Call<BaseResponse<Long>> getUnreadCount();

    @PUT("api/v1/notifications/{notificationId}/read")
    Call<BaseResponse<BuyerNotificationResponse>> markAsRead(@Path("notificationId") Long notificationId);

    @PUT("api/v1/notifications/read-all")
    Call<BaseResponse<Object>> markAllAsRead();
}
