package ru.paymon.android.firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.commons.codec.binary.Base64;

import java.util.Map;

import ru.paymon.android.GroupsManager;
import ru.paymon.android.R;
import ru.paymon.android.UsersManager;
import ru.paymon.android.activities.MainActivity;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.SerializedStream;
import ru.paymon.android.utils.Utils;

public class FcmService extends FirebaseMessagingService {
    public static String token;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        FcmService.token = token;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("AAA", "log " + remoteMessage.getFrom() + " " + remoteMessage.getMessageId());
        if (remoteMessage.getData().size() > 0) {
            Log.e("AAA", "SIZE > 0 " + remoteMessage.getData());
            final Map<String, String> data = remoteMessage.getData();
            final String b64string = data.get("base64");
            final byte[] bytes = Base64.decodeBase64(b64string);
            final SerializedStream stream = new SerializedStream(bytes);
            final int svuid = stream.readInt32(false);

            switch (svuid) {
                case RPC.PM_message.svuid:
                case RPC.PM_messageItem.svuid:
                    RPC.Message message = RPC.Message.deserialize(stream, svuid, false);
                    showNotification(message);
                    break;
            }
//            showNotification(remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            Log.e("AAA", "NOTIFY " + remoteMessage.getNotification().getBody());
//            showNotification(remoteMessage.getNotification());
        }
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }

    private void showNotification(RPC.Message message) {
        final Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        boolean isGroup = message.to_peer.user_id == 0;
        final RPC.UserObject user = UsersManager.getInstance().getUser(message.from_id);
        final String title = !isGroup ? Utils.formatUserName(user) : GroupsManager.getInstance().getGroup(message.to_peer.group_id).title;
        final String text = message.text;

        final Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setTicker(getString(R.string.message))
                .setContentTitle(title)
                .setContentText(text);

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    private void showNotification(RemoteMessage.Notification notification) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setTicker(getString(R.string.message))
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody());

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    private void showNotification(Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setTicker(getString(R.string.message))
                .setContentTitle(data.get("_nmt"))
                .setContentText("test");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}
