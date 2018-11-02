package ru.paymon.android.firebase;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FcmService extends FirebaseMessagingService {
    public static String token;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        FcmService.token = token;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

    }
}
