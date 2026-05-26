package com.gr6.smartcart_android.common.notification;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.gr6.smartcart_android.common.repository.FcmTokenRepository;

public class SmartCartFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_TOKEN";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.d(TAG, "onNewToken: " + token);

        FcmTokenRepository.getInstance(getApplicationContext()).sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = "SmartCart";
        String body = "";

        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        }

        if ((title == null || title.trim().isEmpty()) && message.getData().containsKey("title")) {
            title = message.getData().get("title");
        }

        if ((body == null || body.trim().isEmpty()) && message.getData().containsKey("body")) {
            body = message.getData().get("body");
        }

        NotificationHelper.showNotification(
                getApplicationContext(),
                title,
                body,
                message.getData()
        );
    }
}