package com.gr6.smartcart_android.seller.notification.api;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.seller.notification.response.SellerNotificationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface SellerNotificationApiService {

    @GET("api/v1/notifications")
    Call<BaseResponse<List<SellerNotificationResponse>>> getNotifications();

    @GET("api/v1/notifications/unread-count")
    Call<BaseResponse<Long>> getUnreadCount();

    @PUT("api/v1/notifications/{notificationId}/read")
    Call<BaseResponse<SellerNotificationResponse>> markAsRead(@Path("notificationId") Long notificationId);

    @PUT("api/v1/notifications/read-all")
    Call<BaseResponse<Object>> markAllAsRead();
}
