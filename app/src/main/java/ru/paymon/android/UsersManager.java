package ru.paymon.android;

import java.util.List;

import ru.paymon.android.net.RPC;

public class UsersManager {
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


    public void putUser(RPC.UserObject user) {
        if (user instanceof RPC.PM_userFull) {
            ApplicationLoader.db.userDao().insert(user);
        } else if (user instanceof RPC.PM_user) {
            RPC.UserObject cachedUser = UsersManager.getInstance().getUser(user.id);
            if (cachedUser == null) {
                ApplicationLoader.db.userDao().insert(user);
            } else {
                cachedUser.photoURL = user.photoURL;
                cachedUser.first_name = user.first_name;
                cachedUser.last_name = user.last_name;
                cachedUser.id = user.id;
                cachedUser.login = user.login;
                ApplicationLoader.db.userDao().insert(cachedUser);
            }
        }
    }

    public void putUsers(List<RPC.UserObject> userObjects){
        for (RPC.UserObject user:userObjects) {
            putUser(user);
        }
    }

    public RPC.UserObject getUser(int uid) {
        return ApplicationLoader.db.userDao().getUserById(uid);
    }
}
