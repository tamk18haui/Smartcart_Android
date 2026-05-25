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
            String type = data.get("type");

            if ("CHAT".equalsIgnoreCase(type)) {
                Intent intent = new Intent(context, ChatRoomActivity.class);
                putLongExtraIfExists(intent, "partnerId", data.get("senderId"));
                putLongExtraIfExists(intent, "conversationId", data.get("conversationId"));
                return intent;
            }

            if ("ORDER".equalsIgnoreCase(type) || "SELLER_ORDER".equalsIgnoreCase(type)) {
                Intent intent = new Intent(context, OrderDetailActivity.class);
                putLongExtraIfExists(intent, "orderId", data.get("orderId"));
                putLongExtraIfExists(intent, "shopOrderId", data.get("shopOrderId"));
                return intent;
            }
        }

        return new Intent(context, RoleRouterActivity.class);
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
