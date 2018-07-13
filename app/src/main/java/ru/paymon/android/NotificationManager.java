package ru.paymon.android;

import android.util.SparseArray;

import java.util.ArrayList;

public class NotificationManager {
    private static volatile NotificationManager Instance = null;

    public enum NotificationEvent {
        USER_AUTHORIZED,
        RECEIVED_NEW_MESSAGES,
        PROFILE_PHOTO_UPDATED,

        //
        PHOTO_ID_CHANGED,
        //

        didLoadedStickerPack,
        profileUpdated,
        userAuthorized,
        chatAddMessages,
        dialogsNeedReload,
        didConnectedToServer,
        didEstablishedSecuredConnection,
        didLoadEthereumWallet,
        didDisconnectedFromTheServer,
        userInfoDidLoaded,
    }

    private SparseArray<ArrayList<Object>> observers = new SparseArray<>();
    private SparseArray<ArrayList<Object>> removeAfterBroadcast = new SparseArray<>();
    private SparseArray<ArrayList<Object>> addAfterBroadcast = new SparseArray<>();
    private int broadcasting = 0;

    public interface IListener {
        void didReceivedNotification(NotificationEvent event, Object... args);
    }

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

    public void postNotificationName(NotificationEvent event, Object... args) {
        broadcasting++;
        ArrayList<Object> objects = observers.get(event.ordinal());
        if (objects != null && !objects.isEmpty()) {
            for (int a = 0; a < objects.size(); a++) {
                Object obj = objects.get(a);
                ((IListener) obj).didReceivedNotification(event, args);
            }
        }
        broadcasting--;
        if (broadcasting == 0) {
            if (removeAfterBroadcast.size() != 0) {
                for (int a = 0; a < removeAfterBroadcast.size(); a++) {
                    int key = removeAfterBroadcast.keyAt(a);
                    ArrayList<Object> arrayList = removeAfterBroadcast.get(key);
                    for (int b = 0; b < arrayList.size(); b++) {
                        removeObserver(arrayList.get(b), NotificationEvent.values()[key]);
                    }
                }
                removeAfterBroadcast.clear();
            }
            if (addAfterBroadcast.size() != 0) {
                for (int a = 0; a < addAfterBroadcast.size(); a++) {
                    int key = addAfterBroadcast.keyAt(a);
                    ArrayList<Object> arrayList = addAfterBroadcast.get(key);
                    for (int b = 0; b < arrayList.size(); b++) {
                        addObserver(arrayList.get(b), NotificationEvent.values()[key]);
                    }
                }
                addAfterBroadcast.clear();
            }
        }
    }

    public void addObserver(Object observer, NotificationEvent event) {
        int id = event.ordinal();
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
        if (objects == null)
            observers.put(id, (objects = new ArrayList<>()));

        if (!objects.contains(observer))
            objects.add(observer);
    }

    public void removeObserver(Object observer, NotificationEvent event) {
        int id = event.ordinal();
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
        if (objects != null)
            objects.remove(observer);
    }

    public void dispose(){
        Instance = null;
    }
}
