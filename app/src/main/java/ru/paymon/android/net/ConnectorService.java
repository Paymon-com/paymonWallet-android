package ru.paymon.android.net;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.utils.KeyGenerator;
import ru.paymon.android.utils.SerializedBuffer;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.MessagesManager.generateMessageID;
import static ru.paymon.android.net.RPC.ERROR_AUTH;
import static ru.paymon.android.net.RPC.ERROR_AUTH_TOKEN;
import static ru.paymon.android.net.RPC.ERROR_KEY;
import static ru.paymon.android.net.RPC.ERROR_SPAMMING;
import static ru.paymon.android.view.AbsFragmentChat.CHAT_GROUP_USERS;
import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;

public class ConnectorService extends Service implements NotificationManager.IListener {
    public HashMap<Long, Packet.OnResponseListener> requestsMap = new HashMap<>(2);
    public long lastKeepAlive;
    public String ACTION = "ACTION_NOTIFY_BUTTON";

    private static final String BROADCAST_ACTION = "START_NOTIFY_SERVICE";
    private static final int NOTIFY_ID = 101;
    private final IBinder binder = new LocalBinder();
    private BroadcastReceiver broadcastReceiver;
    private RPC.Message msg;
    private boolean receiverRegistered;
    private android.app.NotificationManager notificationManager;

    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(Config.TAG, "Notify accepted action: " + intent.getAction());
                String code = intent.getAction();

                if (code != null && code.equals(ACTION)) {
                    final boolean isGroup = intent.getBooleanExtra("IS_GROUP", false);
                    final int cid = intent.getIntExtra(CHAT_ID_KEY, 0);
                    Intent actIntent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                    actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Bundle bundle = new Bundle();
                    bundle.putInt(CHAT_ID_KEY, cid);
                    if (isGroup)
                        bundle.putParcelableArrayList(CHAT_GROUP_USERS, GroupsManager.getInstance().groupsUsers.get(cid));
                    actIntent.putExtra("OPEN_CHAT_BUNDLE", bundle);
                    notificationManager.cancel(NOTIFY_ID);
                    startActivity(actIntent);
                }
            }
        };
        IntentFilter filter = new IntentFilter(ACTION);
        registerReceiver(broadcastReceiver, filter);
        receiverRegistered = true;
    }

    private void unregisterReceiver() {
        unregisterReceiver(broadcastReceiver);
        receiverRegistered = false;
    }

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

    //region life cycle
    @Override
    public void onCreate() {
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didConnectedToServer);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED);
        registerReceiver();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (receiverRegistered)
            unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroy() {
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didConnectedToServer);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED);
        unregisterReceiver();
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

        }
    }

    private void showNotify(RPC.Message msg) {
        if (msg.to_peer.user_id != 0)
            if (msg.from_id == MessagesManager.getInstance().currentChatID)
                return;
            else if (msg.to_peer.group_id != 0)
                if (msg.to_peer.group_id == MessagesManager.getInstance().currentChatID)
                    return;

        if (!User.CLIENT_MESSAGES_NOTIFY_IS_DONT_WORRY) {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            RPC.UserObject user = UsersManager.getInstance().users.get(msg.from_id);

            String text = "";
            if (msg instanceof RPC.PM_message) {
                text = msg.text;
            } else if (msg instanceof RPC.PM_messageItem) {
                switch (msg.itemType) {
                    case PHOTO:
                        text = "Photo";
                        break;
                    case STICKER:
                        text = "Sticker";
                        break;
                    case ACTION:
                        text = msg.text;
                        break;
                    default:
                        text = "Item"; //"Вложение";
                }
            }

            Context context = getApplicationContext();

            if (user != null) {
                final boolean isGroup = msg.to_peer instanceof RPC.PM_peerGroup;
                final int cid = isGroup ? msg.to_peer.group_id : msg.from_id;
                Intent intentBtnReply = new Intent(ACTION);
                intentBtnReply.setAction(ACTION);
                intentBtnReply.putExtra("IS_GROUP", isGroup);
                intentBtnReply.putExtra(CHAT_ID_KEY, cid);
                PendingIntent pIntentBtnReply = PendingIntent.getBroadcast(this, 2, intentBtnReply, 0);

                Bitmap bmp = null;
                try {
                    Picasso.get().load(user.photoURL.url).get();
                } catch (Exception e) {
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.profile_photo_none);
                }

                Notification.Builder builder = new Notification.Builder(context);
                builder.setContentIntent(pIntentBtnReply)
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_notification)
                        .setLargeIcon(bmp)
                        .setSound(ringtoneUri)
                        .setTicker(getString(R.string.message))
                        .setContentTitle(Utils.formatUserName(user))
                        .setContentText(text)
                        .addAction(R.drawable.ic_answer_message, getString(R.string.replay_button), pIntentBtnReply);

                builder.setVibrate(new long[]{100, 200, 100, 300});

                Notification notification = builder.build();

                notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                if (notificationManager != null)
                    notificationManager.notify(NOTIFY_ID, notification);
            }
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

    // TODO: сделать возможным передачу сообщения вместе с ошибкой
    public void processServerResponse(Packet packet, long messageID) {
        lastKeepAlive = System.currentTimeMillis() / 1000L;
        RPC.PM_error error = null;
        Packet.OnResponseListener listener = requestsMap.get(messageID);
        requestsMap.remove(messageID);

        if (packet instanceof RPC.Message) {
            msg = (RPC.Message) packet;
            final LinkedList<RPC.Message> messages = new LinkedList<>();
            messages.add(msg);
            Utils.netQueue.postRunnable(() -> {
                NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.RECEIVED_NEW_MESSAGES, messages);
                RPC.UserObject user = UsersManager.getInstance().users.get(msg.from_id);
                if (user == null) {
                    RPC.PM_getUserInfo userInfo = new RPC.PM_getUserInfo();
                    userInfo.user_id = msg.from_id;
                    NetworkManager.getInstance().sendRequest(userInfo, (response, error1) -> {
                        if (response == null || error1 != null) return;
                        ApplicationLoader.applicationHandler.post(() -> {
                            RPC.UserObject userObject = (RPC.UserObject) response;
                            UsersManager.getInstance().users.put(userObject.id, userObject);
                            if (UsersManager.getInstance().userContacts.get(msg.from_id) == null)
                                UsersManager.getInstance().userContacts.put(userObject.id, userObject);
                        });
                    });
                } else {
                    if (UsersManager.getInstance().userContacts.get(msg.from_id) == null)
                        UsersManager.getInstance().userContacts.put(user.id, user);
                }
                MessagesManager.getInstance().putMessage(msg);
                if (msg.to_peer.user_id != 0) {
                    if (msg.to_peer.user_id == User.currentUser.id) {
                        MessagesManager.getInstance().lastMessages.put(msg.from_id, msg.id);
                    } else {
                        MessagesManager.getInstance().lastMessages.put(msg.to_peer.user_id, msg.id);
                    }
                } else {
                    MessagesManager.getInstance().lastGroupMessages.put(msg.to_peer.group_id, msg.id);
                }
                NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.dialogsNeedReload);
                showNotify(msg);
            });
        } else if (packet instanceof RPC.PM_photoURL) {
            final RPC.PM_photoURL update = (RPC.PM_photoURL) packet;
            if (update.peer instanceof RPC.PM_peerUser) {
                RPC.PM_peerUser peerUser = (RPC.PM_peerUser) ((RPC.PM_photoURL) packet).peer;
                UsersManager.getInstance().users.get(peerUser.user_id).photoURL.url = ((RPC.PM_photoURL) packet).url;
                UsersManager.getInstance().userContacts.get(peerUser.user_id).photoURL.url = ((RPC.PM_photoURL) packet).url;
                if (User.currentUser.id == peerUser.user_id) {
                    User.currentUser.photoURL.url = update.url;
                    User.saveConfig();
                }
                Log.e(Config.TAG, "UPDATE PHOTO URL USER");
            } else if (update.peer instanceof RPC.PM_peerGroup) {
                RPC.PM_peerGroup peerGroup = (RPC.PM_peerGroup) ((RPC.PM_photoURL) packet).peer;
                GroupsManager.getInstance().groups.get(peerGroup.group_id).photoURL.url = ((RPC.PM_photoURL) packet).url;
                Log.e(Config.TAG, "UPDATE PHOTO URL GROUP");
            }
//        } else if (packet instanceof RPC.PM_photo) {
//            RPC.PM_photo photoURL = (RPC.PM_photo) packet;
//            MediaManager.getInstance().updatePhoto(photoURL);
        } else if (packet instanceof RPC.PM_error) {
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
                // FIXME: logout
                NetworkManager.getInstance().authByToken();
            } else if (error.code == ERROR_AUTH) {
//                Looper.prepare();
                ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, getString(R.string.auth_wrong_login_or_password), Toast.LENGTH_LONG).show());
            } else if (error.code == ERROR_SPAMMING) {
//                Looper.prepare();
                ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, getString(R.string.error_spamming), Toast.LENGTH_LONG).show());
                return;
            }
        } else if (packet instanceof RPC.PM_postConnectionData) {
            KeyGenerator.getInstance().setPostConnectionData((RPC.PM_postConnectionData) packet);
            NetworkManager.getInstance().setHandshaked(true);
//            requestsMap.clear();
//            requestsMap.remove(messageID);
            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
        } else if (packet instanceof RPC.PM_file) {
//            requestsMap.remove(messageID);
            RPC.PM_file file = (RPC.PM_file) packet;
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
