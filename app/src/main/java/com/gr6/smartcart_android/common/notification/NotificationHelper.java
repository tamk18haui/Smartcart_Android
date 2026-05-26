package com.gr6.smartcart_android.common.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.order.OrderDetailActivity;
import com.gr6.smartcart_android.chat.ChatRoomActivity;
import com.gr6.smartcart_android.navigation.RoleRouterActivity;
import com.gr6.smartcart_android.seller.order.SellerOrderDetailActivity;

import java.util.Map;
import java.util.Random;

public class NotificationHelper {

    public static final String CHANNEL_ID = "smartcart_push_channel";
    public static final String CHANNEL_NAME = "SmartCart thông báo";

    private NotificationHelper() {
    }

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Thông báo tin nhắn, đơn hàng và ví SmartCart");

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context, String title, String body, Map<String, String> data) {
        createChannel(context);

        Intent intent = buildOpenIntent(context, data);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                new Random().nextInt(100000),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title == null || title.trim().isEmpty() ? "SmartCart" : title)
                .setContentText(body == null ? "" : body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body == null ? "" : body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(context)
                .notify(new Random().nextInt(100000), builder.build());
    }

    private static Intent buildOpenIntent(Context context, Map<String, String> data) {
        if (data != null) {
            String type = get(data, "type");
            String routeKey = get(data, "routeKey");

            if ("CHAT".equalsIgnoreCase(type) || "CHAT_ROOM".equalsIgnoreCase(routeKey)) {
                Intent intent = new Intent(context, ChatRoomActivity.class);
                putLongExtraIfExists(intent, ChatRoomActivity.EXTRA_PARTNER_ID,
                        firstNotBlank(get(data, "partnerId"), get(data, "senderId"), get(data, "targetId")));
                putLongExtraIfExists(intent, "conversation_id", get(data, "conversationId"));
                intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_NAME,
                        firstNotBlank(get(data, "partnerName"), get(data, "senderName")));
                intent.putExtra(ChatRoomActivity.EXTRA_PARTNER_AVATAR,
                        firstNotBlank(get(data, "partnerAvatarUrl"), get(data, "senderAvatarUrl")));
                return intent;
            }

            if ("SELLER_ORDER_DETAIL".equalsIgnoreCase(routeKey)) {
                Intent intent = new Intent(context, SellerOrderDetailActivity.class);
                putLongExtraIfExists(intent, SellerOrderDetailActivity.EXTRA_ORDER_ID,
                        firstNotBlank(get(data, "targetId"), get(data, "shopOrderId"), get(data, "orderId")));
                return intent;
            }

            if ("BUYER_ORDER_DETAIL".equalsIgnoreCase(routeKey)
                    || "ORDER_DETAIL".equalsIgnoreCase(routeKey)
                    || "SHOP_ORDER_DETAIL".equalsIgnoreCase(routeKey)
                    || "ORDER".equalsIgnoreCase(type)) {
                Intent intent = new Intent(context, OrderDetailActivity.class);
                putLongExtraIfExists(intent, OrderDetailActivity.EXTRA_SHOP_ORDER_ID,
                        firstNotBlank(get(data, "targetId"), get(data, "shopOrderId"), get(data, "orderId")));
                return intent;
            }
        }

        return new Intent(context, RoleRouterActivity.class);
    }

    private static String get(Map<String, String> data, String key) {
        if (data == null || key == null) {
            return "";
        }

        String value = data.get(key);
        return value == null ? "" : value.trim();
    }

    private static String firstNotBlank(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return "";
    }

    private static void putLongExtraIfExists(Intent intent, String key, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        try {
            intent.putExtra(key, Long.parseLong(value.trim()));
        } catch (Exception ignored) {
        }
    }
}
