package ru.paymon.android;

import android.util.SparseArray;

import ru.paymon.android.net.RPC;

public class UsersManager {
    public SparseArray<RPC.UserObject> users = new SparseArray<>();
    public SparseArray<RPC.UserObject> userContacts = new SparseArray<>();
    public SparseArray<RPC.UserObject> searchUsers = new SparseArray<>();

    private static volatile UsersManager Instance = null;

    public static UsersManager getInstance() {
        UsersManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (UsersManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new UsersManager();
                }
            }
        }
        return localInstance;
    }

    public void dispose() {
        Instance = null;
    }

    public void putUser(RPC.UserObject user) {
        if (users.get(user.id) != null) return;

        users.put(user.id, user);

//        if (user.photoID == 0)
//            MediaManager.getInstance().userProfilePhotoIDs.put(user.gid, MediaManager.getInstance().generatePhotoID());
//        else
//            MediaManager.getInstance().userProfilePhotoIDs.put(user.gid, user.photoID);

//        if (MessagesManager.getInstance().dialogsMessages.get(user.id) != null)
//            userContacts.put(user.id, user);
    }

    public void putSearchUser(RPC.UserObject user) {
        if (searchUsers.get(user.id) != null) return;

        searchUsers.put(user.id, user);
//        if (user.photoID == 0)
//            MediaManager.getInstance().userProfilePhotoIDs.put(user.gid, MediaManager.getInstance().generatePhotoID());
//        else
//            MediaManager.getInstance().userProfilePhotoIDs.put(user.gid, user.photoID);
    }
}
