package ru.paymon.android.net;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.LinkedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.User;
import ru.paymon.android.utils.KeyGenerator;
import ru.paymon.android.utils.SerializedBuffer;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.Config.TAG;

public class NetworkManager {
    public ConnectionState connectionState;
    public NetworkState networkState;
    private boolean handshaked;
    private boolean authorized;
    private TimeHandler timeThread;
    private long lastOutgoingMessageId = 0;
    public LinkedList<WaitingRequest> waitingRequests = new LinkedList<>();
    private LinkedList<FutureRequest> futureRequests = new LinkedList<>();
    private static volatile NetworkManager Instance = null;

    public static NetworkManager getInstance() {
        NetworkManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (NetworkManager.class) {
                localInstance = Instance;
                if (localInstance == null)
                    Instance = localInstance = new NetworkManager();
            }
        }
        return localInstance;
    }

    //region enum's
    public enum ConnectionState {
        HZ,
        DISCONNECTED,
        CONNECTED,
    }

    public enum NetworkState {
        DISCONNECTED,
        CONNECTED,
    }
    //endregion

    //region connector service
    public ConnectorService connectorService;
    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            connectorService = ((ConnectorService.LocalBinder) service).getService();
            if (connectorService != null)
                connect();
        }

        public void onServiceDisconnected(ComponentName className) {
            ApplicationLoader.applicationHandler.post(() -> NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.didDisconnectedFromTheServer));
            connectorService = null;
        }
    };

    public void bindServices() {
        if (connectorService == null)
            ApplicationLoader.applicationContext.bindService(new Intent(ApplicationLoader.applicationContext, ConnectorService.class), connection, Context.BIND_AUTO_CREATE);
    }

    public void unbindServices() {
        if (connectorService != null) {
            ApplicationLoader.applicationContext.unbindService(connection);
            connectorService = null;
        }
    }
    //endregion

    //region jni func
    @SuppressWarnings("JniMissingFunction")
    private native void native_connect();

    @SuppressWarnings("JniMissingFunction")
    private native void native_reconnect();

    @SuppressWarnings("JniMissingFunction")
    public native void native_sendData(int addr, int pos, int limit);

    // called from jni
    public static void onConnectionStateChanged(int state) {
        Log.d(TAG, "ConnectionState=" + state);
        if (state == 3) {
            getInstance().connectionState = ConnectionState.CONNECTED;
            ApplicationLoader.applicationHandler.post(() -> NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.didConnectedToServer));
        } else {
            getInstance().connectionState = ConnectionState.DISCONNECTED;
            ApplicationLoader.applicationHandler.post(() -> NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.didDisconnectedFromTheServer));
        }
    }

    // called from jni
    public static void onConnectionDataReceived(int addr, int length) {
        getInstance().timeThread.lastKeepAliveTime = System.currentTimeMillis() / 1000;
        getInstance().connectorService.onConnectionDataReceived(SerializedBuffer.wrap(addr), length);
    }
    //endregion

    //region connect func
    private void reset() {
        handshaked = false;
        setAuthorized(false);
        KeyGenerator.getInstance().reset();
        waitingRequests.clear();
        futureRequests.clear();

        if (connectorService != null) {
            connectorService.requestsMap.clear();
            connectionState = ConnectionState.DISCONNECTED;
            connectorService.lastKeepAlive = System.currentTimeMillis() / 1000;
        }
    }

    public void connect() {
        if (connectionState != ConnectionState.CONNECTED) {
            reset();
            native_connect();
            if (NetworkManager.getInstance().timeThread != null) {
                NetworkManager.getInstance().timeThread.halt();
                try {
                    NetworkManager.getInstance().timeThread.join();
                } catch (InterruptedException e) {
                }
            }
            NetworkManager.getInstance().timeThread = new TimeHandler();
            NetworkManager.getInstance().timeThread.start();
            processRequest();
        }
    }

    public void reconnect() {
//        ApplicationLoader.applicationHandler.post(() -> NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.didDisconnectedFromTheServer));
        reset();
        native_reconnect();
    }
    //endregion

    //region static classes

    private static class WaitingRequest {
        public Packet packet;
        public Packet.OnResponseListener listener;
        public long messageID;
    }

    private static class FutureRequest {
        long id;
        Packet packet;
        Packet.OnResponseListener callback;

        public FutureRequest(long id, Packet packet, Packet.OnResponseListener callback) {
            this.id = id;
            this.packet = packet;
            this.callback = callback;
        }
    }

    //endregion

    public void handshake() {
        if (handshaked) return;

        Log.d(TAG, "Handshaking");
        RPC.PM_requestDHParams packet = new RPC.PM_requestDHParams();

        NetworkManager.getInstance().sendRequest(packet, new Packet.OnResponseListener() {
            @Override
            public void onResponse(Packet response, RPC.PM_error error) {
                if (error != null || response == null) return;

                RPC.PM_DHParams packet = (RPC.PM_DHParams) response;
                if (!KeyGenerator.getInstance().init(packet.p, packet.g)) {
                    KeyGenerator.getInstance().reset();
                    handshake();
                    return;
                }

                RPC.PM_clientDHdata dhData = new RPC.PM_clientDHdata();
                dhData.key = KeyGenerator.getInstance().getPublicKey();

                NetworkManager.getInstance().sendRequest(dhData, new Packet.OnResponseListener() {
                    @Override
                    public void onResponse(Packet response, RPC.PM_error error) {
                        if (error != null || response == null) return;

                        RPC.PM_serverDHdata packet = (RPC.PM_serverDHdata) response;

                        RPC.PM_DHresult packetDHresult = new RPC.PM_DHresult();
                        if (KeyGenerator.getInstance().createShared(packet.key)) {
                            packetDHresult.ok = true;
                            KeyGenerator.getInstance().setKeyID(packet.keyID);
                            NetworkManager.getInstance().sendRequest(packetDHresult);
                        } else {
                            NetworkManager.getInstance().setHandshaked(false);
                            packetDHresult.ok = false;
                            NetworkManager.getInstance().sendRequest(packetDHresult);
                        }
                    }
                });
            }
        });
    }

    public void setHandshaked(boolean flag) {
        handshaked = flag;
    }

    public void authByToken() {
        RPC.PM_userFull user = User.currentUser;
        if (user != null) {
            if (user.token != null) {
                RPC.PM_authToken packet = new RPC.PM_authToken();
                packet.token = user.token;

                NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
                    if (error != null || response == null) {
                        NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.authorizationFailed);
                        return;
                    }

                    User.currentUser = (RPC.PM_userFull) response;
                    User.saveConfig();

                    Log.d(TAG, "login successful, new token=" + Utils.bytesToHexString(User.currentUser.token));
                    setAuthorized(true);
                });
            }
        }
    }

    public void setAuthorized(boolean authorized) {
        if (authorized) {
            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.userAuthorized);
            if (!this.authorized) {
                sendFutureRequests();
            }
        }
        this.authorized = authorized;
    }

    private boolean canSendApiRequests(final Packet packet, final Packet.OnResponseListener onComplete, final long messageID) {
        boolean needToAdd = false;
        if (!isConnected()) {
            needToAdd = true;
        } else {
            if (!isAuthorized()) {
                needToAdd = !(packet instanceof RPC.PM_requestDHParams || packet instanceof RPC.PM_DHParams ||
                        packet instanceof RPC.PM_DHresult || packet instanceof RPC.PM_clientDHdata ||
                        packet instanceof RPC.PM_serverDHdata || packet instanceof RPC.PM_auth ||
                        packet instanceof RPC.PM_authToken || packet instanceof RPC.PM_error ||
                        packet instanceof RPC.PM_keepAlive || packet instanceof RPC.PM_register ||
                        packet instanceof RPC.PM_user || packet instanceof RPC.PM_userFull);
            }
        }

        if (needToAdd) {
            WaitingRequest request = new WaitingRequest();
            request.packet = packet;
            request.listener = onComplete;
            if (messageID == 0) {
                request.messageID = generateMessageID();
            } else {
                request.messageID = messageID;
            }
            waitingRequests.add(request);
        }
        return !needToAdd;
    }

    public long sendRequestNowOrLater(final Packet packet) {
        return sendRequestNowOrLater(packet, null, generateMessageID());
    }

    public long sendRequestNowOrLater(final Packet packet, final Packet.OnResponseListener onComplete) {
        return sendRequestNowOrLater(packet, onComplete, generateMessageID());
    }

    public long sendRequestNowOrLater(final Packet packet, final Packet.OnResponseListener onComplete, final long messageID) {
        if (isAuthorized()) {
            return sendRequest(packet, onComplete, messageID);
        } else {
            futureRequests.add(new FutureRequest(messageID, packet, onComplete));
            return messageID;
        }
    }

    public long sendRequest(final Packet packet) {
        return sendRequest(packet, null, generateMessageID());
    }

    public long sendRequest(final Packet packet, final Packet.OnResponseListener onComplete) {
        return sendRequest(packet, onComplete, generateMessageID());
    }

    public long sendRequest(final Packet packet, final Packet.OnResponseListener onComplete, final long messageID) {
        WaitingRequest request = new WaitingRequest();
        request.packet = packet;
        request.listener = onComplete;
        if (messageID == 0)
            request.messageID = generateMessageID();
        else
            request.messageID = messageID;

        waitingRequests.offer(request);
        if (connectorService != null && connectorService.requestsMap != null)
            processRequest();
        else
            Log.e(TAG, "sendRequest: failed " + packet.toString() + ", connector==" + connectorService);
        return request.messageID;
    }

    public void sendFutureRequests() {
        if (futureRequests.isEmpty()) return;
        FutureRequest request = futureRequests.removeFirst();
        sendRequest(request.packet, (p, e) -> {
            if (request.callback != null) {
                request.callback.onResponse(p, e);
                sendFutureRequests();
            }
        }, request.id);
    }

    public void processRequest() {
        if (waitingRequests.size() <= 0) return;
        WaitingRequest request = waitingRequests.poll();
        if (request == null) return;
        if (connectorService != null)
            connectorService.sendRequest(request.packet, request.listener, request.messageID);
    }

    public void cancelRequest(final long messageID, boolean error) {
        if (connectorService != null)
            connectorService.cancelRequest(messageID, error);
    }

    public long generateMessageID() {
        long messageId = (long) ((((double) System.currentTimeMillis() + 1000) * 4294967296.0) / 1000.0);
        if (messageId <= lastOutgoingMessageId)
            messageId = ++lastOutgoingMessageId;
        while (messageId % 4 != 0)
            messageId++;
        lastOutgoingMessageId = messageId;
        return messageId;
    }

    public boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED;
    }

    public boolean isNetworkConnected() {
        return networkState == NetworkState.CONNECTED;
    }

    public boolean isAuthorized() {
        return authorized;
    }
}
