package ru.paymon.android;

import android.util.SparseArray;


import java.util.ArrayList;

import ru.paymon.android.net.RPC;

public class GroupsManager {
    public SparseArray<RPC.Group> groups = new SparseArray<>();
    public SparseArray<ArrayList<RPC.UserObject>> groupsUsers = new SparseArray<>();
    private static volatile GroupsManager Instance = null;

    public static GroupsManager getInstance() {
        GroupsManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (GroupsManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new GroupsManager();
                }
            }
        }
        return localInstance;
    }

    public void dispose() {
        Instance = null;
    }

    public void putGroup(RPC.Group group) {
        if (groups.get(group.id) != null) return;

        long pid = group.photo.id;
        if (pid == 0)
            MediaManager.getInstance().groupPhotoIDs.put(group.id, MediaManager.getInstance().generatePhotoID());
        else
            MediaManager.getInstance().groupPhotoIDs.put(group.id, pid);


        groups.put(group.id, group);
        groupsUsers.put(group.id, group.users);
        for (RPC.UserObject user : group.users) {
            UsersManager.getInstance().putUser(user);
        }
    }
}
