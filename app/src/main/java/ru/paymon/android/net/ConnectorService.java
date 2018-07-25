package ru.paymon.android.net;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.MediaManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.KeyGenerator;
import ru.paymon.android.utils.SerializedBuffer;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.utils.cache.lruramcache.LruRamCache;

import static ru.paymon.android.MessagesManager.generateMessageID;
import static ru.paymon.android.net.RPC.ERROR_AUTH;
import static ru.paymon.android.net.RPC.ERROR_AUTH_TOKEN;
import static ru.paymon.android.net.RPC.ERROR_KEY;
import static ru.paymon.android.net.RPC.ERROR_SPAMMING;


public class ConnectorService extends Service implements NotificationManager.IListener {
    public HashMap<Long, Packet.OnResponseListener> requestsMap = new HashMap<>(2);
    public long lastKeepAlive;
    public String ACTION = "ACTION_NOTIFY_BUTTON";

    private static final String BROADCAST_ACTION = "START_NOTIFY_SERVICE";
    private static final int NOTIFY_ID = 101;
    private final IBinder binder = new LocalBinder();
    private BroadcastReceiver broadcastReceiver;
    private android.app.NotificationManager notificationManager;
    private RPC.Message msg;
    private boolean receiverRegistered;


    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent id, Object... args) {
        if (id == NotificationManager.NotificationEvent.didConnectedToServer) {
            NetworkManager.getInstance().handshake();
        } else if (id == NotificationManager.NotificationEvent.didEstablishedSecuredConnection) {
            NetworkManager.getInstance().authByToken();
//            if (Utils.isNetworkConnected(this)) {
//                if (User.ethereumPassword != null && !User.ethereumPassword.isEmpty()) {
//                    Log.d(Config.TAG, "onCreate: " + "Load Ethereum wallet");
//                    Utils.stageQueue.postRunnable(() -> {
//                        if (User.currentUser != null) {
//                            boolean loaded = NewEthereumLibrary.getInstance().createOrLoadWallet(User.currentUser.id, User.ethereumPassword);
//                            loadEthereumWallet();
//                        }
//                    });
//                }
//            } else {
//                Toast.makeText(this, R.string.check_connected_to_server, Toast.LENGTH_SHORT).showCD();
//            }
        }
    }

    private void loadEthereumWallet() {
        Log.d(Config.TAG, "loadEthereumWallet");
        ApplicationLoader.applicationHandler.post(() -> NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.didLoadEthereumWallet));
//        User.ethereumRestoreBackupOrLoad = false;
    }

    @Override
    public void onCreate() {
        Log.d(Config.TAG, "Connector service created");
        super.onCreate();
//        ApplicationLoader.init();
        notificationManager = (android.app.NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        registerReceiver();
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didConnectedToServer);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
    }

    @Override
    public void onDestroy() {
        Log.d(Config.TAG, "Connector service destroyed");
        unregisterReceiver();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didConnectedToServer);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Config.TAG, "Connector service started");
        getConnection();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Config.TAG, "Connector service bound");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(Config.TAG, "Connector service unbound");
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(Config.TAG, "Connector service task removed");
        if (receiverRegistered)
            unregisterReceiver(broadcastReceiver);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didConnectedToServer);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
        super.onTaskRemoved(rootIntent);
    }

    public static void getConnection() {
        new ConnectorTask().execute();
    }

    public static class ConnectorTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (NetworkManager.getInstance().getConnector() == null) {
                NetworkManager.getInstance().bindServices();
            } else if (NetworkManager.getInstance().connectionState != 3 && !NetworkManager.getInstance().isConnected()) {
                NetworkManager.getInstance().connect();
            }
            try {
                TimeUnit.SECONDS.sleep(1);
                getConnection();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(Config.TAG, "Notify accepted action: " + intent.getStringExtra(ACTION));
                String code = intent.getStringExtra(ACTION);

                if (code.equals("close")) {
                    Log.d(Config.TAG, "Notify: " + NOTIFY_ID + " clicked to reply button");
                    Intent actIntent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                    actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    actIntent.putExtra("open_dialog", "open");
                    Bundle bundle = new Bundle();
                    if (msg.to_id.user_id != 0) {
                        bundle.putInt("chat_id", msg.from_id);
                    } else if (msg.to_id.group_id != 0) {
                        bundle.putInt("chat_id", msg.to_id.group_id);
                        bundle.putBoolean("isGroup", true);
                    }
                    actIntent.putExtra("bundleChatID", bundle);
                    notificationManager.cancel(NOTIFY_ID);
                    startActivity(actIntent);
                }
            }
        };
        IntentFilter filter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(broadcastReceiver, filter);
        receiverRegistered = true;
    }

    void unregisterReceiver() {
        unregisterReceiver(broadcastReceiver);
        receiverRegistered = false;
    }

    private void showNotify(RPC.Message msg) {
        if (msg.to_id.user_id != 0)
            if (msg.from_id == MessagesManager.getInstance().currentChatID)
                return;
            else if (msg.to_id.group_id != 0)
                if (msg.to_id.group_id == MessagesManager.getInstance().currentChatID)
                    return;

        if (!User.CLIENT_MESSAGES_NOTIFY_IS_DONT_WORRY) {
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), User.CLIENT_MESSAGES_NOTIFY_SOUND_FILE);
            Bitmap bitmap = LruRamCache.getInstance().getBitmap(R.drawable.profile_photo_none);
            RPC.UserObject user = UsersManager.getInstance().users.get(msg.from_id);
            RPC.UserObject fromUser = user;
            if (fromUser != null) {
                // TODO: make ObservableImage
                bitmap = MediaManager.getInstance().loadPhotoBitmap(fromUser.id, fromUser.photoID);
            }
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

            Intent notificationIntent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            if (user != null) {
                Intent intentBtnReply = new Intent(ConnectorService.BROADCAST_ACTION);
                intentBtnReply.putExtra(ACTION, "close");
                PendingIntent pIntentBtnReply = PendingIntent.getBroadcast(this, 2, intentBtnReply, 0);

                Notification.Builder builder = new Notification.Builder(context);
                builder.setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_notification)
                        .setLargeIcon(bitmap)
                        .setTicker(getString(R.string.message))
                        .setContentTitle(Utils.formatUserName(user))
                        .setContentText(text)
                        .addAction(R.drawable.ic_answer_message, getString(R.string.replay_button), pIntentBtnReply);

                if (User.CLIENT_MESSAGES_NOTIFY_IS_VIBRATION) {
                    builder.setVibrate(new long[]{100, 200, 100, 300});
                }

                Notification notification;

                if (android.os.Build.VERSION.SDK_INT <= 15) {
                    notification = builder.getNotification(); // API-15 and lower
                } else {
                    notification = builder.build();
                }

                notificationManager.notify(NOTIFY_ID, notification);

                ringtone.play();
                Log.d(Config.TAG, "Notify with ID " + NOTIFY_ID + " showed");
            }
        }

    }

    public class LocalBinder extends Binder {
        public ConnectorService getService() {
            return ConnectorService.this;
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

    // TODO: сделать возможным передачу сообщения вместе с ошибкой
    public void processServerResponse(Packet packet, long messageID) {
        lastKeepAlive = System.currentTimeMillis() / 1000L;
        RPC.PM_error error = null;
        Packet.OnResponseListener listener = requestsMap.get(messageID);
        requestsMap.remove(messageID);

        if (packet instanceof RPC.Message) {
            msg = (RPC.Message) packet;
            showNotify(msg);
            final LinkedList<RPC.Message> messages = new LinkedList<>();
            messages.add(msg);
            ApplicationLoader.applicationHandler.post(() -> {
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
                if (msg.to_id.user_id != 0) {
                    if (msg.to_id.user_id == User.currentUser.id) {
                        MessagesManager.getInstance().lastMessages.put(msg.from_id, msg.id);
                    } else {
                        MessagesManager.getInstance().lastMessages.put(msg.to_id.user_id, msg.id);
                    }
                } else {
                    MessagesManager.getInstance().lastGroupMessages.put(msg.to_id.group_id, msg.id);
                }
                NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.dialogsNeedReload);
            });
        } else if (packet instanceof RPC.PM_updatePhotoID) {
            Log.e(Config.TAG, "UPDATE PHOTO ID");
            final RPC.PM_updatePhotoID update = (RPC.PM_updatePhotoID) packet;
            MediaManager.getInstance().updatePhotoID(update.oldID, update.newID); //TODO:proverit'
//            User.currentUser.photoID = update.newID;

//            ApplicationLoader.applicationHandler.post(() -> ObservableMediaManager.getInstance().postPhotoUpdateIDNotification(update.oldID, update.newID));
        } else if (packet instanceof RPC.PM_photo) {
//            RPC.PM_photo photo = (RPC.PM_photo) packet;
//            MediaManager.getInstance().updatePhoto(photo);
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
//            file.id;
            if (file.type == FileManager.FileType.PHOTO) {
                FileManager.getInstance().acceptFileDownload(file, messageID);
            } else if (file.type == FileManager.FileType.STICKER) {
                FileManager.getInstance().acceptStickerDownload(file, messageID);
            }
        } else if (packet instanceof RPC.PM_filePart) {
//            requestsMap.remove(messageID);
            FileManager.getInstance().continueFileDownload((RPC.PM_filePart) packet, messageID);
        }

        if (listener != null) {
            listener.onResponse(packet, error);
//            requestsMap.remove(messageID);
        }
//        requestsMap.clear();
        NetworkManager.getInstance().processRequest();
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

    public boolean isConnected() {
        return false;
    }
}