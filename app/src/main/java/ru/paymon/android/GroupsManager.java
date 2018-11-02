package ru.paymon.android;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ru.paymon.android.net.RPC;
import ru.paymon.android.room.AppDatabase;
import ru.paymon.android.utils.Utils;

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
        Executors.newSingleThreadExecutor().submit(() -> AppDatabase.getDatabase().groupDao().insert(group));
    }

    public void putGroups(List<RPC.Group> groups) {
        Executors.newSingleThreadExecutor().submit(() -> {
            for (RPC.Group group : groups)
                AppDatabase.getDatabase().groupDao().insert(group);
        });
    }

    public RPC.Group getGroup(int gid) {
        Callable<RPC.Group> callable = new Callable<RPC.Group>() {
            @Override
            public RPC.Group call() {
                return AppDatabase.getDatabase().groupDao().getGroupById(gid);
            }
        };

        Future<RPC.Group> future = Executors.newSingleThreadExecutor().submit(callable);

        RPC.Group group = null;
        try {
            group = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return group;
    }

    public ArrayList<RPC.UserObject> getGroupUsers(int gid) {
        Callable<ArrayList<RPC.UserObject>> callable = new Callable<ArrayList<RPC.UserObject>>() {
            @Override
            public ArrayList<RPC.UserObject> call() {
                ArrayList<RPC.UserObject> usersList = new ArrayList<>();
                RPC.Group group = AppDatabase.getDatabase().groupDao().getGroupById(gid);
                for (Integer uid : group.users) {
                    RPC.UserObject user = AppDatabase.getDatabase().userDao().getUserById(uid);
                    usersList.add(user);
                }
                return usersList;
            }
        };

        Future<ArrayList<RPC.UserObject>> future = Executors.newSingleThreadExecutor().submit(callable);

        ArrayList<RPC.UserObject> result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
