package ru.paymon.android.net;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;


import java.util.HashMap;
import java.util.LinkedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.User;
import ru.paymon.android.utils.KeyGenerator;
import ru.paymon.android.utils.SerializedBuffer;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.Config.TAG;


public class NetworkManager {
    public int connectionState;
    //    private BlockchainServiceImpl blockchainService;
    private ConnectorService connectorService;
    private boolean connectorServiceIsBound;
    private boolean handshaked;
    private boolean authorized;
    private TimeHandler timeThread;
    private long lastOutgoingMessageId = 0;
    private boolean willConnect = false;
    private HashMap<Long, Byte[]> keys = new HashMap<>();
    private LinkedList<WaitingRequest> waitingRequests = new LinkedList<>();
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

    private NetworkManager() {
        handshaked = false;
        authorized = false;
    }

    @SuppressWarnings("JniMissingFunction")
    private native void native_connect();

    @SuppressWarnings("JniMissingFunction")
    private native void native_reconnect();

    @SuppressWarnings("JniMissingFunction")
    public native void native_sendData(int addr, int pos, int limit);

    private static class WaitingRequest {
        public Packet packet;
        public Packet.OnResponseListener listener;
        public long messageID;
    }

    private static class TimeHandler extends Thread implements Runnable {
        long lastKeepAliveTime;
        boolean keepAliveSent = false;
        boolean running = true;

        @Override
        public void run() {
            final NetworkManager networkManager = NetworkManager.getInstance();
            networkManager.connectorService.lastKeepAlive = System.currentTimeMillis() / 1000L;
            lastKeepAliveTime = System.currentTimeMillis() / 1000L;

            while (running) {
                long curtime = System.currentTimeMillis() / 1000L;

                if (networkManager.isConnected() && networkManager.connectorService != null) {
                    if (curtime - lastKeepAliveTime >= 10 && !keepAliveSent) {
                        lastKeepAliveTime = System.currentTimeMillis() / 1000L;
                        networkManager.sendRequest(new RPC.PM_keepAlive(), new Packet.OnResponseListener() {
                            @Override
                            public void onResponse(Packet response, RPC.PM_error error) {
                                networkManager.connectorService.lastKeepAlive = System.currentTimeMillis() / 1000L;
                                keepAliveSent = false;
                            }
                        });
                        keepAliveSent = true;
                    }
                    if (networkManager.connectorService != null && (curtime - networkManager.connectorService.lastKeepAlive > 30)) {
                        networkManager.reconnect();
                        keepAliveSent = false;
                    }
                }
                try {
                    Thread.sleep(750);
                } catch (InterruptedException e) {

                }
            }
        }

        public void halt() {
            running = false;
        }
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

    public void setHandshaked(boolean flag) {
        handshaked = flag;
    }

    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            connectorService = ((ConnectorService.LocalBinder) service).getService();
            if (connectorService != null && willConnect) {
                connect();
            }
            connectorServiceIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            ApplicationLoader.applicationHandler.post(new Runnable() {
                @Override
                public void run() {
                    NotificationManager.getInstance().postNotificationName(NotificationManager.didDisconnectedFromTheServer);
                }
            });
            connectorService = null;
            connectorServiceIsBound = false;
        }
    };

//    private ServiceConnection blockchainConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            blockchainService = ((BlockchainServiceImpl.LocalBinder) service).getService();
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            blockchainService = null;
//        }
//    };

    public void connect() {
        if (connectorService != null) {
            if (connectionState != 3) {
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
        } else {
            willConnect = true;
        }
    }

    public void bindServices() {
        if (!connectorServiceIsBound && connectorService == null) {
            ApplicationLoader.applicationContext.bindService(new Intent(ApplicationLoader.applicationContext, ConnectorService.class), connection, Context.BIND_AUTO_CREATE);
        }
//        ApplicationLoader.applicationContext.bindService(new Intent(ApplicationLoader.applicationContext, BlockchainServiceImpl.class), blockchainConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindServices() {
        if (connectorServiceIsBound && connectorService != null) {
            ApplicationLoader.applicationContext.unbindService(connection);
            connectorService = null;
        }
    }

    public ConnectorService getConnector() {
        return connectorService;
    }

    public void reconnect() {
        ApplicationLoader.applicationHandler.post(new Runnable() {
            @Override
            public void run() {
                NotificationManager.getInstance().postNotificationName(NotificationManager.didDisconnectedFromTheServer);
            }
        });

        reset();
        native_reconnect();
    }

    public void reset() {
        handshaked = false;
        setAuthorized(false);
        KeyGenerator.getInstance().reset();
        keys.clear();
        waitingRequests.clear();
        futureRequests.clear();
        if (connectorService != null) {
            connectorService.requestsMap.clear();
            connectionState = 0;
            connectorService.lastKeepAlive = System.currentTimeMillis() / 1000;
        }
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

    public void authByToken() {
        RPC.PM_userFull user = User.currentUser;
        if (user != null) {
            if (user.token != null) {
                RPC.PM_authToken packet = new RPC.PM_authToken();
                packet.token = user.token;

                NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
                    if (error != null || response == null) return;

                    RPC.PM_userFull newUser = (RPC.PM_userFull) response;
                    User.currentUser = newUser;
                    User.saveConfig();

//                        MainActivity.walletApplication.createWallet();
//                        if (!BlockchainServiceImpl.isStarted)
//                            blockchainService.init();

                    Log.d(TAG, "login successful, new token=" + Utils.bytesToHexString(newUser.token));
                    ApplicationLoader.applicationHandler.post(() ->
                            NotificationManager.getInstance().postNotificationName(NotificationManager.userInfoDidLoaded)
                    );
                    setAuthorized(true);
//                    MessagesManager.getInstance().loadChats(false);
                });
            }
        }
    }

    // called from jni
    public static void onConnectionDataReceived(int addr, int length) {
        NetworkManager.getInstance().timeThread.lastKeepAliveTime = System.currentTimeMillis() / 1000;
        NetworkManager.getInstance().getConnector().onConnectionDataReceived(SerializedBuffer.wrap(addr), length);
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


    public static void onConnectionStateChanged(int state) {
        Log.d(TAG, "ConnectionState=" + state);
        if (state == 3) {
            NotificationManager.getInstance().postNotificationName(NotificationManager.didConnectedToServer);
        } else {
            ApplicationLoader.applicationHandler.post(new Runnable() {
                @Override
                public void run() {
                    NotificationManager.getInstance().postNotificationName(NotificationManager.didDisconnectedFromTheServer);
                }
            });
        }
        NetworkManager.getInstance().connectionState = state;
    }

    public boolean isConnected() {
        return connectionState == 3;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        if (authorized) {
            NotificationManager.getInstance().postNotificationName(NotificationManager.userAuthorized);
            if (!this.authorized) {
                sendFutureRequests();
            }
        }
        this.authorized = authorized;
    }
}
