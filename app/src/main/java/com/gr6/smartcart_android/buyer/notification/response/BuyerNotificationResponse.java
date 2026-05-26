package com.gr6.smartcart_android.buyer.notification.response;

public class BuyerNotificationResponse {
    private Long notificationId;
    private String title;
    private String content;
    private String type;
    private Boolean isRead;
    private String routeKey;
    private Long targetId;
    private String routeParams;
    private String createdAt;

    public Long getNotificationId() {
        return notificationId;
    }

    public String getTitle() {
        return title == null ? "Thông báo" : title;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public String getType() {
        return type == null ? "SYSTEM" : type;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public boolean isRead() {
        return Boolean.TRUE.equals(isRead);
    }

    public String getRouteKey() {
        return routeKey == null ? "" : routeKey;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getRouteParams() {
        return routeParams;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
