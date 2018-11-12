package ru.paymon.android.net;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.activities.MainActivity;
import ru.paymon.android.utils.KeyGenerator;
import ru.paymon.android.utils.SerializedBuffer;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.MessagesManager.generateMessageID;
import static ru.paymon.android.activities.MainActivity.INTENT_ACTION_OPEN_CHAT;
import static ru.paymon.android.activities.MainActivity.IS_GROUP;
import static ru.paymon.android.net.RPC.ERROR_AUTH;
import static ru.paymon.android.net.RPC.ERROR_AUTH_TOKEN;
import static ru.paymon.android.net.RPC.ERROR_KEY;
import static ru.paymon.android.net.RPC.ERROR_SPAMMING;
import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;

public class ConnectorService extends Service implements NotificationManager.IListener {
    public static final String LIVE_TIME_KEY = "LIVE_TIME_KEY";
    private final IBinder binder = new LocalBinder();
    public HashMap<Long, Packet.OnResponseListener> requestsMap = new HashMap<>(2);
    public long lastKeepAlive;
    public String ACTION = "ACTION_NOTIFY_BUTTON";
    private int liveTime;


//    public static class ConnectorTask extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... params) {
//            if (NetworkManager.getInstance().connectorService == null) {
//                NetworkManager.getInstance().bindServices();
//            } else if (NetworkManager.getInstance().connectionState != NetworkManager.ConnectionState.CONNECTED && !NetworkManager.getInstance().isConnected()) {
//                NetworkManager.getInstance().connect();
//            }
//            try {
//                TimeUnit.SECONDS.sleep(10);
//                getConnection();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//        }
//    }

//    private static void getConnection() {
//        new ConnectorTask().execute();
//    }

    //region binding
    public class LocalBinder extends Binder {
        public ConnectorService getService() {
            return ConnectorService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
    //endregion

    @Override
    public ComponentName startService(Intent service) {
        liveTime = service.getIntExtra(LIVE_TIME_KEY, 0);
        return super.startService(service);
    }

    //region life cycle
    @Override
    public void onCreate() {
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didConnectedToServer);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED);

        if (liveTime != 0) {
            Handler handler = new Handler();
            handler.postDelayed(() -> stopSelf(), liveTime);
        }

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        getConnection();
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didConnectedToServer);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED);
        super.onDestroy();
    }
    //endregion

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        if (event == NotificationManager.NotificationEvent.didConnectedToServer) {
            NetworkManager.getInstance().handshake();
        } else if (event == NotificationManager.NotificationEvent.didEstablishedSecuredConnection) {
            NetworkManager.getInstance().authByToken();
        } else if (event == NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED) {
            NetworkManager.getInstance().connect();
        } else if (event == NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED) {
            NetworkManager.getInstance().reconnect();
        }
    }

    private void showNotification(RPC.Message message) {
        try {
            if (User.CLIENT_MESSAGES_NOTIFY_IS_DONT_WORRY) return;

            final boolean isGroup = message.to_peer.user_id == 0;

            if (!isGroup && message.from_id == MessagesManager.getInstance().currentChatID)
                return;

            if (isGroup && message.to_peer.group_id == MessagesManager.getInstance().currentChatID)
                return;

            final int cid = message.from_id == User.currentUser.id ? message.to_peer.user_id : message.from_id;

            final Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(INTENT_ACTION_OPEN_CHAT, true);
            intent.putExtra(IS_GROUP, isGroup);
            intent.putExtra(CHAT_ID_KEY, cid);
            final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            String text = "";
            if (message instanceof RPC.PM_message) {
                text = message.text;
            } else if (message instanceof RPC.PM_messageItem) {
                switch (message.itemType) {
                    case PHOTO:
                        text = getString(R.string.notification_type_photo);
                        break;
                    case STICKER:
                        text = getString(R.string.notification_type_sticker);
                        break;
                    case ACTION:
                        text = "Action";
                        break;
                    default:
                        text = getString(R.string.notification_type_file);
                }
            }

            final RPC.UserObject user = UsersManager.getInstance().getUser(message.from_id);

            Bitmap bmp;
            try {
                bmp = Picasso.get().load(user.photoURL.url).get();
            } catch (Exception e) {
                bmp = BitmapFactory.decodeResource(getResources(), R.drawable.profile_photo_none);
            }

            final Notification.Builder builder = new Notification.Builder(this)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setTicker(getString(R.string.other_message))
                    .setContentText(text)
                    .setLargeIcon(bmp);

            if (!isGroup) {
                final String title = Utils.formatUserName(user);

                builder.setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title);
            } else {
                final RPC.Group group = GroupsManager.getInstance().getGroup(message.to_peer.group_id);
                final String title = GroupsManager.getInstance().getGroup(group.id).title;

                builder.setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title);
            }

            final android.app.NotificationManager notificationManager = (android.app.NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (User.CLIENT_MESSAGES_NOTIFY_IS_VIBRATION)
                    ((android.app.NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).getNotificationChannel(Config.MESSAGES_NOTIFICATION_CHANNEL_ID).setVibrationPattern(new long[]{100, 200, 100, 300});
                else
                    ((android.app.NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).getNotificationChannel(Config.MESSAGES_NOTIFICATION_CHANNEL_ID).setVibrationPattern(null);

                builder.setChannelId(Config.MESSAGES_NOTIFICATION_CHANNEL_ID);
            } else {
                if (User.CLIENT_MESSAGES_NOTIFY_IS_VIBRATION)
                    builder.setVibrate(new long[]{100, 200, 100, 300});
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(soundUri);
            }

            notificationManager.notify(message.from_id, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long sendRequest(final Packet packet) {
        return sendRequest(packet, null, generateMessageID());
    }

    public long sendRequest(final Packet packet, final Packet.OnResponseListener onComplete) {
        return sendRequest(packet, onComplete, generateMessageID());
    }

    public synchronized long sendRequest(final Packet packet, final Packet.OnResponseListener onComplete, final long messageID) {
        if (requestsMap.containsKey(messageID)) {
            Log.e(Config.TAG, "Packet with same ID has already been sent");
            return -1;
        }
        Utils.netQueue.postRunnable(() -> {
            Log.d(Config.TAG, String.format("send request %s with ID=%d", packet, messageID));
            try {
                final int n = packet.getSize();
                SerializedBuffer buffer;

                SerializedBuffer data = new SerializedBuffer(n);
                packet.serializeToStream(data);
                data.position(0);
                byte[] btp = new byte[data.buffer.limit()];
                System.arraycopy(data.buffer.array(), data.buffer.arrayOffset(), btp, 0, data.buffer.limit());
                int addr = KeyGenerator.getInstance().wrapData(messageID, data);
                buffer = SerializedBuffer.wrap(addr);
                buffer.position(0);
                btp = new byte[buffer.buffer.limit()];
                System.arraycopy(buffer.buffer.array(), buffer.buffer.arrayOffset(), btp, 0, buffer.buffer.limit());

                packet.freeResources();
                if (onComplete != null) {
                    requestsMap.put(messageID, onComplete);
                }
                sendData(buffer, messageID);
            } catch (Exception e) {
                Log.e(Config.TAG, "Error generating request");
                requestsMap.remove(messageID);
            }
        });
        return messageID;
    }

    private synchronized void sendData(SerializedBuffer buffer, long messageID) {
        NetworkManager.getInstance().native_sendData(buffer.getAddress(), 0, buffer.limit());
    }

    private void receivedMessage(final RPC.Message msg) {
        Utils.netQueue.postRunnable(() -> {
            MessagesManager.getInstance().putMessage(msg);
            showNotification(msg);
        });
    }

    private void receivedPhotoURL(final RPC.PM_photoURL photoURL) {
        if (photoURL.peer instanceof RPC.PM_peerUser) {
            RPC.PM_peerUser peerUser = (RPC.PM_peerUser) photoURL.peer;
//            AppDatabase.getDatabase().userDao().getGroupById(peerUser.user_id).photoURL.url = photoURL.url;
//            UsersManager.getInstance().userContacts.get(peerUser.user_id).photoURL.url = photoURL.url;
            if (User.currentUser.id == peerUser.user_id) {
                User.currentUser.photoURL.url = photoURL.url;
                User.saveConfig();
            }
        } else if (photoURL.peer instanceof RPC.PM_peerGroup) {
            RPC.PM_peerGroup peerGroup = (RPC.PM_peerGroup) photoURL.peer;
            RPC.Group group = GroupsManager.getInstance().getGroup(peerGroup.group_id);
            group.photoURL.url = photoURL.url;
            GroupsManager.getInstance().putGroup(group);
        }
    }


    private void receivedPostConnectionData(final RPC.PM_postConnectionData data) {
        KeyGenerator.getInstance().setPostConnectionData(data);
        NetworkManager.getInstance().setHandshaked(true);
        NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
    }

    public void processServerResponse(Packet packet, long messageID) {
        lastKeepAlive = System.currentTimeMillis() / 1000L;
        RPC.PM_error error = null;
        Packet.OnResponseListener listener = requestsMap.get(messageID);
        requestsMap.remove(messageID);

        if (packet instanceof RPC.Message) {
            receivedMessage((RPC.Message) packet);
        } else if (packet instanceof RPC.PM_photoURL) {
            receivedPhotoURL((RPC.PM_photoURL) packet);
        } else if (packet instanceof RPC.PM_error) {
//            receivedError((RPC.PM_error) packet);
//            packet = null;
            Log.e(Config.TAG, "ERROR");

            error = (RPC.PM_error) packet;
            packet = null;

            Log.w(Config.TAG, String.format(Locale.getDefault(), "PM_error(%d): %s", error.code, error.text));
            final String text = error.text;
            ApplicationLoader.applicationHandler.post(() -> Log.d("paymon_error_response", text));

            if (error.code == ERROR_KEY) {
                Log.e(Config.TAG, "ERROR_KEY, reconnecting");
                NetworkManager.getInstance().reconnect();
            } else if (error.code == ERROR_AUTH_TOKEN) {
                Log.e(Config.TAG, "ERROR_AUTH_TOKEN, auth");
                NetworkManager.getInstance().authByToken();
            } else if (error.code == ERROR_AUTH) {
//                Looper.prepare();
                ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, getString(R.string.error_auth_wrong_login_or_password), Toast.LENGTH_LONG).show());
            } else if (error.code == ERROR_SPAMMING) {
//                Looper.prepare();
                ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, getString(R.string.error_spamming), Toast.LENGTH_LONG).show());
                return;
            }
        } else if (packet instanceof RPC.PM_postConnectionData) {
            receivedPostConnectionData((RPC.PM_postConnectionData) packet);
        } else if (packet instanceof RPC.PM_file) {
//            requestsMap.remove(messageID);
//            RPC.PM_file file = (RPC.PM_file) packet;
//            requestsMap.get(3);
//            file.gid;
//            if (file.type == FileManager.FileType.PHOTO) {
//                FileManager.getInstance().acceptFileDownload(file, messageID);
//            } else if (file.type == FileManager.FileType.STICKER) {
//                FileManager.getInstance().acceptStickerDownload(file, messageID);
//            }
        } else if (packet instanceof RPC.PM_filePart) {
//            requestsMap.remove(messageID);
//            FileManager.getInstance().continueFileDownload((RPC.PM_filePart) packet, messageID);
        }

        if (listener != null) {
            listener.onResponse(packet, error);
//            requestsMap.remove(messageID);
        }
//        requestsMap.clear();
        NetworkManager.getInstance().processRequest();
    }

    public void onConnectionDataReceived(SerializedBuffer data, int length) {
        if (length == 4) {
            int code = data.readInt32(true);
            Log.e(Config.TAG, "error = " + code);
            NetworkManager.getInstance().reconnect();
            return;
        }
        int mark = data.position();
        long keyId;

        try {
            keyId = data.readInt64(true);
        } catch (RuntimeException e) {
            Log.e(Config.TAG, "onConnectionDataReceived: ERROR 1");
            NetworkManager.getInstance().reconnect();
            return;
        }
        if (keyId == 0) {
            long messageID;
            int messageLength;

            try {
                messageID = data.readInt64(true);
                messageLength = data.readInt32(true);
            } catch (RuntimeException e) {
                Log.e(Config.TAG, "onConnectionDataReceived: ERROR 2");
                NetworkManager.getInstance().reconnect();
                return;
            }

//            if (NetworkManager.getInstance().isHandshaked()) {
//                if (data.remaining() - messageLength >= 0) {
//                    data.skip(messageLength);
////                    NetworkManager.getInstance().reconnect();
//                    return;
//                }
//            }
            if (messageLength != data.remaining()) {
                Log.e(Config.TAG, "connection received incorrect message length");
                NetworkManager.getInstance().reconnect();
                return;
            }

            final Packet object = ClassStore.Instance().TLdeserialize(data, data.readInt32(true), true);
            if (object != null) {
                Log.d(Config.TAG, "connection received object " + object.getClass().getName() + ", len " + messageLength + ", ID=" + messageID);
                processServerResponse(object, messageID/*, messageSeqNo, messageServerSalt, connection, 0, 0*/);
            }
        } else {
//            byte[] ba = new byte[data.limit()];
//            System.arraycopy(data.buffer.array(), data.buffer.arrayOffset(), ba, 0, data.buffer.limit());
//            Log.d("paymon-dbg-ocdr", Utils.bytesToHexString(ba));
            int i = 24 + 12;
            boolean b = KeyGenerator.getInstance().decryptMessage(keyId, data.getAddress(), length - 24, mark);
            Log.d(Config.TAG, "onConnectionDataReceived: " + i + " " + b);
            if (length < i || !b) {
                Log.e(Config.TAG, "Can't decrypt packet");
                NetworkManager.getInstance().reconnect();
                return;
            }

//            data.position(mark + 20 + 8 + 4);
            data.position(mark + 24);
            long messageID = 0;
            int messageLength = 0;

            try {
                messageID = data.readInt64(true);
                messageLength = data.readInt32(true);
            } catch (RuntimeException e) {
//                NetworkManager.getInstance().reconnect();
                return;
            }
//            Packet object = deserialize(nullptr, messageLength, data);
            final Packet object = ClassStore.Instance().TLdeserialize(data, data.readInt32(true), true);
            if (object != null) {
                Log.d(Config.TAG, "connection received object " + object + ", len " + messageLength + ", ID=" + messageID);
                processServerResponse(object, messageID/*, messageSeqNo, messageServerSalt, connection, 0, 0*/);
            }
        }
    }

    public void cancelRequest(long messageID, boolean error) {
        Packet.OnResponseListener listener = requestsMap.get(messageID);
        String errorText = "";

        if (listener != null) {
            if (error) {
                RPC.PM_error errorPacket = new RPC.PM_error();
                errorPacket.text = errorText;
                errorPacket.code = 0x10000;
                listener.onResponse(null, errorPacket);
            }

            requestsMap.remove(listener);
        }
        if (error) {
            Toast.makeText(ApplicationLoader.applicationContext, errorText, Toast.LENGTH_SHORT).show();
        }
    }
}
