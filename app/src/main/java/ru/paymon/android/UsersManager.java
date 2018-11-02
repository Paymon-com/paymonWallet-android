package ru.paymon.android;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ru.paymon.android.net.RPC;
import ru.paymon.android.room.AppDatabase;

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
        Executors.newSingleThreadExecutor().submit(() -> {
            if (user instanceof RPC.PM_userFull) {
                AppDatabase.getDatabase().userDao().insert(user);
            } else if (user instanceof RPC.PM_user) {
                RPC.UserObject cachedUser = UsersManager.getInstance().getUser(user.id);
                if (cachedUser == null) {
                    AppDatabase.getDatabase().userDao().insert(user);
                } else {
                    cachedUser.photoURL = user.photoURL;
                    cachedUser.first_name = user.first_name;
                    cachedUser.last_name = user.last_name;
                    cachedUser.id = user.id;
                    cachedUser.login = user.login;
                    AppDatabase.getDatabase().userDao().insert(cachedUser);
                }
            }
        });
    }

    public void putUsers(List<RPC.UserObject> userObjects){
        Executors.newSingleThreadExecutor().submit(() -> {
            for (RPC.UserObject user : userObjects) {
                putUser(user);
            }
        });
    }

    public RPC.UserObject getUser(int uid) {
        Callable<RPC.UserObject> callable = new Callable<RPC.UserObject>() {
            @Override
            public RPC.UserObject call() {
                return AppDatabase.getDatabase().userDao().getUserById(uid);
            }
        };

        Future<RPC.UserObject> future = Executors.newSingleThreadExecutor().submit(callable);

        RPC.UserObject user = null;
        try {
            user = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }
}
