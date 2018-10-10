package ru.paymon.android;

import java.util.ArrayList;
import java.util.List;

import ru.paymon.android.net.RPC;

public class GroupsManager {
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

    public void putGroup(RPC.Group group) {
        ApplicationLoader.db.groupDao().insert(group);
    }

    public void putGroups(List<RPC.Group> groups) {
        for (RPC.Group group : groups) {
            ApplicationLoader.db.groupDao().insert(group);
        }
    }

    public RPC.Group getGroup(int gid) {
        return ApplicationLoader.db.groupDao().getGroupById(gid);
    }

    public ArrayList<RPC.UserObject> getGroupUsers(int gid){
        ArrayList<RPC.UserObject> result = new ArrayList<>();
        RPC.Group group = ApplicationLoader.db.groupDao().getGroupById(gid);
        for (Integer uid : group.users){
            RPC.UserObject user = ApplicationLoader.db.userDao().getUserById(uid);
            result.add(user);
        }
        return result;
    }
}
