package ru.paymon.android.firebase;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ru.paymon.android.net.ConnectorService;
import ru.paymon.android.net.NetworkManager;

import static ru.paymon.android.net.ConnectorService.LIVE_TIME_KEY;

public class FcmService extends FirebaseMessagingService {
    public static String token;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        FcmService.token = token;
    }

    private boolean isConnectorServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ConnectorService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (!isConnectorServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), ConnectorService.class);
            intent.putExtra(LIVE_TIME_KEY, 6000);
            startService(intent);
            NetworkManager.getInstance().bindServices();
        }

        if (NetworkManager.getInstance().connectionState == NetworkManager.ConnectionState.CONNECTED)
            return;

        //TODO: получать ID пришедшего сообщения и при включении сервиса полуать сообщение от сервера

//        if (remoteMessage.getData().size() > 0) {
//            final Map<String, String> data = remoteMessage.getData();
//            final String b64string = data.get("packet");
//            final byte[] bytes = android.util.Base64.decode(b64string, android.util.Base64.DEFAULT);
//            final SerializedStream stream = new SerializedStream(bytes);
//            final int svuid = stream.readInt32(false);
//            switch (svuid) {
//                case RPC.PM_message.svuid:
//                case RPC.PM_messageItem.svuid:
//                    RPC.Message message = RPC.Message.deserialize(stream, svuid, false);
//                    showNotification(message);
//                    break;
//            }
//        }

//        long lastMessageTime = MessagesManager.getInstance().getLastMessageTime();
//        RPC.PM_getMessagesFromTime request = new RPC.PM_getMessagesFromTime(lastMessageTime);
//        NetworkManager.getInstance().sendRequest(request, ((response, error) -> {
//            if (response == null || error != null) return;
//            if (response instanceof RPC.PM_chat_messages) {
//                RPC.PM_chat_messages pm_chat_messages = (RPC.PM_chat_messages) response;
//                LinkedList<RPC.Message> messages = pm_chat_messages.messages;
//                MessagesManager.getInstance().putMessages(messages);
//            }
//        }));
    }

//    private void showNotification(RPC.Message message) {
//        final boolean isGroup = message.to_peer.user_id == 0;
//
//        if (!isGroup && message.from_id == MessagesManager.getInstance().currentChatID)
//            return;
//
//        if (isGroup && message.to_peer.group_id == MessagesManager.getInstance().currentChatID)
//            return;
//
//        final int cid = message.from_id == User.currentUser.id ? message.to_peer.user_id : message.from_id;
//
//        final Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra(INTENT_ACTION_OPEN_CHAT, true);
//        intent.putExtra(IS_GROUP, isGroup);
//        intent.putExtra(CHAT_ID_KEY, cid);
//        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        String text = "";
//        if (message instanceof RPC.PM_message) {
//            text = message.text;
//        } else if (message instanceof RPC.PM_messageItem) {
//            switch (message.itemType) {
//                case PHOTO:
//                    text = "Photo";
//                    break;
//                case STICKER:
//                    text = "Sticker";
//                    break;
//                case ACTION:
//                    text = "Action";
//                    break;
//                default:
//                    text = "Item";
//            }
//        }
//
//        final RPC.UserObject user = UsersManager.getInstance().getUser(message.from_id);
//
//        Bitmap bmp;
//        try {
//            bmp = Picasso.get().load(user.photoURL.url).get();
//        } catch (Exception e) {
//            bmp = BitmapFactory.decodeResource(getResources(), R.drawable.profile_photo_none);
//        }
//
//        final Notification.Builder builder = new Notification.Builder(this)
//                .setAutoCancel(true)
//                .setWhen(System.currentTimeMillis())
//                .setTicker(getString(R.string.message))
//                .setContentText(text)
//                .setLargeIcon(bmp);
//
//        if (!isGroup) {
//            final String title = Utils.formatUserName(user);
//
//            builder.setContentIntent(pendingIntent)
//                    .setSmallIcon(R.drawable.ic_notification)
//                    .setContentTitle(title);
//        } else {
//            final RPC.Group group = GroupsManager.getInstance().getGroup(message.to_peer.group_id);
//            final String title = GroupsManager.getInstance().getGroup(group.id).title;
//
//            builder.setContentIntent(pendingIntent)
//                    .setSmallIcon(R.drawable.ic_notification)
//                    .setContentTitle(title);
//        }
//
//        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            builder.setChannelId(Config.MESSAGES_NOTIFICATION_CHANNEL_ID);
//        } else {
//            builder.setVibrate(new long[]{100, 200, 100, 300});
//            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            builder.setSound(soundUri);
//        }
//
//        notificationManager.notify(message.from_id, builder.build());
//    }
}
