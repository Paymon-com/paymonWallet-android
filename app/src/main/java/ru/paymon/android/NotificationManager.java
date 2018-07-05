package ru.paymon.android;

import android.util.SparseArray;

import java.util.ArrayList;

public class NotificationManager {
    private static int totalEvents = 1;

    public static final int userAuthorized = totalEvents++;
    public static final int didCreatedEthereumWallet = totalEvents++;
    public static final int didReceivedNewMessages = totalEvents++;
    public static final int updateInterfaces = totalEvents++;
    public static final int dialogsNeedReload = totalEvents++;
    public static final int chatAddMessages = totalEvents++;
    public static final int messagesDeleted = totalEvents++;
    public static final int messagesIDdidupdated = totalEvents++;
    public static final int messagesRead = totalEvents++;
    public static final int messagesDidLoaded = totalEvents++;
    public static final int draftMessagesDidLoaded = totalEvents++;
    public static final int messageReceivedByServer = totalEvents++;
    public static final int messageSendError = totalEvents++;
    public static final int chatDidCreated = totalEvents++;
    public static final int chatDidFailCreate = totalEvents++;
    public static final int chatInfoDidLoaded = totalEvents++;
    public static final int chatInfoCantLoad = totalEvents++;
    public static final int removeAllMessagesFromDialog = totalEvents++;
    public static final int didReceivedContactsSearchResult = totalEvents++;
    public static final int userDidLoggedIn = totalEvents++;
    public static final int mediaLoaded = totalEvents++;
    public static final int profileUpdated = totalEvents++;
    public static final int photoUpdated = totalEvents++;
    public static final int needRestartFragmentView = totalEvents++;
    public static final int prostocashApiKeyUpdated = totalEvents++;
    public static final int cancelDialog = totalEvents++;
    public static final int didPhotoUpdate = totalEvents++;
    public static final int closeLoader = totalEvents++;

    public static final int screenStateChanged = totalEvents++;
    public static final int newSessionReceived = totalEvents++;
    public static final int userInfoDidLoaded = totalEvents++;
    public static final int didUpdatedMessagesViews = totalEvents++;
    public static final int didConnectedToServer = totalEvents++;
    public static final int didEstablishedSecuredConnection = totalEvents++;
    public static final int didDisconnectedFromTheServer = totalEvents++;

    public static final int doLoadChatMessages = totalEvents++;
    public static final int didLoadedStickerPack = totalEvents++;

    public static final int didLoadEthereumWallet = totalEvents++;
    public static final int didCreateETHWallet = totalEvents++;

    private SparseArray<ArrayList<Object>> observers = new SparseArray<>();
    private SparseArray<ArrayList<Object>> removeAfterBroadcast = new SparseArray<>();
    private SparseArray<ArrayList<Object>> addAfterBroadcast = new SparseArray<>();
    private ArrayList<DelayedPost> delayedPosts = new ArrayList<>(10);

    private int broadcasting = 0;
    private boolean animationInProgress;

    private int[] allowedNotifications;

    public interface IListener {
        void didReceivedNotification(int id, Object... args);
    }

    private class DelayedPost {
        private DelayedPost(int id, Object[] args) {
            this.id = id;
            this.args = args;
        }

        private int id;
        private Object[] args;
    }

    private static volatile NotificationManager Instance = null;

    public static NotificationManager getInstance() {
        NotificationManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (NotificationManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new NotificationManager();
                }
            }
        }
        return localInstance;
    }

    public void setAllowedNotificationsDutingAnimation(int notifications[]) {
        allowedNotifications = notifications;
    }

    public void setAnimationInProgress(boolean flag) {
        animationInProgress = flag;
        if (!animationInProgress && !delayedPosts.isEmpty()) {
            for (DelayedPost delayedPost : delayedPosts) {
                postNotificationNameInternal(delayedPost.id, true, delayedPost.args);
            }
            delayedPosts.clear();
        }
    }

    public boolean isAnimationInProgress() {
        return animationInProgress;
    }

    public void postNotificationName(int id, Object... args) {
        boolean allowDuringAnimation = false;
        if (allowedNotifications != null) {
            for (int a = 0; a < allowedNotifications.length; a++) {
                if (allowedNotifications[a] == id) {
                    allowDuringAnimation = true;
                    break;
                }
            }
        }
        postNotificationNameInternal(id, allowDuringAnimation, args);
    }

    public void postNotificationNameInternal(int id, boolean allowDuringAnimation, Object... args) {
        if (!allowDuringAnimation && animationInProgress) {
            DelayedPost delayedPost = new DelayedPost(id, args);
            delayedPosts.add(delayedPost);
            return;
        }
        broadcasting++;
        ArrayList<Object> objects = observers.get(id);
        if (objects != null && !objects.isEmpty()) {
            for (int a = 0; a < objects.size(); a++) {
                Object obj = objects.get(a);
                ((IListener) obj).didReceivedNotification(id, args);
            }
        }
        broadcasting--;
        if (broadcasting == 0) {
            if (removeAfterBroadcast.size() != 0) {
                for (int a = 0; a < removeAfterBroadcast.size(); a++) {
                    int key = removeAfterBroadcast.keyAt(a);
                    ArrayList<Object> arrayList = removeAfterBroadcast.get(key);
                    for (int b = 0; b < arrayList.size(); b++) {
                        removeObserver(arrayList.get(b), key);
                    }
                }
                removeAfterBroadcast.clear();
            }
            if (addAfterBroadcast.size() != 0) {
                for (int a = 0; a < addAfterBroadcast.size(); a++) {
                    int key = addAfterBroadcast.keyAt(a);
                    ArrayList<Object> arrayList = addAfterBroadcast.get(key);
                    for (int b = 0; b < arrayList.size(); b++) {
                        addObserver(arrayList.get(b), key);
                    }
                }
                addAfterBroadcast.clear();
            }
        }
    }

    public void addObserver(Object observer, int id) {
        if (broadcasting != 0) {
            ArrayList<Object> arrayList = addAfterBroadcast.get(id);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                addAfterBroadcast.put(id, arrayList);
            }
            arrayList.add(observer);
            return;
        }
        ArrayList<Object> objects = observers.get(id);
        if (objects == null) {
            observers.put(id, (objects = new ArrayList<>()));
        }
        if (objects.contains(observer)) {
            return;
        }
        objects.add(observer);
    }

    public void removeObserver(Object observer, int id) {
        if (broadcasting != 0) {
            ArrayList<Object> arrayList = removeAfterBroadcast.get(id);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                removeAfterBroadcast.put(id, arrayList);
            }
            arrayList.add(observer);
            return;
        }
        ArrayList<Object> objects = observers.get(id);
        if (objects != null) {
            objects.remove(observer);
        }
    }
}
