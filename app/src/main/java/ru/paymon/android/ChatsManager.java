package ru.paymon.android;

import android.arch.paging.DataSource;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.room.AppDatabase;

public class ChatsManager {
    private static volatile ChatsManager Instance = null;

    public static ChatsManager getInstance() {
        ChatsManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (ChatsManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new ChatsManager();
                }
            }
        }
        return localInstance;
    }

    public void putChat(ChatsItem chatsItem) {
        AppDatabase.dbQueue.postRunnable(() -> AppDatabase.getDatabase().chatDao().insert(chatsItem));
    }

    public void removeChat(ChatsItem chatsItem) {
        AppDatabase.dbQueue.postRunnable(() -> AppDatabase.getDatabase().chatDao().delete(chatsItem));
    }

    public void removeAllChats() {
        AppDatabase.dbQueue.postRunnable(() -> AppDatabase.getDatabase().chatDao().deleteAll());
    }

    public DataSource.Factory<Integer, ChatsItem> getChats() {
        Callable<DataSource.Factory<Integer, ChatsItem>> callable = new Callable<DataSource.Factory<Integer, ChatsItem>>() {
            @Override
            public DataSource.Factory<Integer, ChatsItem> call() {
                return AppDatabase.getDatabase().chatDao().getChats();
            }
        };

        Future<DataSource.Factory<Integer, ChatsItem>> future = Executors.newSingleThreadExecutor().submit(callable);

        DataSource.Factory<Integer, ChatsItem> chats = null;
        try {
            chats = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chats;
    }

    public DataSource.Factory<Integer, ChatsItem> getChatsByChatType(boolean isGroup) {
        Callable<DataSource.Factory<Integer, ChatsItem>> callable = new Callable<DataSource.Factory<Integer, ChatsItem>>() {
            @Override
            public DataSource.Factory<Integer, ChatsItem> call() {
                return AppDatabase.getDatabase().chatDao().getChatsByChatType(isGroup);
            }
        };

        Future<DataSource.Factory<Integer, ChatsItem>> future = Executors.newSingleThreadExecutor().submit(callable);

        DataSource.Factory<Integer, ChatsItem> chats = null;
        try {
            chats = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chats;
    }

    public DataSource.Factory<Integer, ChatsItem> getChatsBySearch(String searchQuery, boolean isGroup) {
        Callable<DataSource.Factory<Integer, ChatsItem>> callable = new Callable<DataSource.Factory<Integer, ChatsItem>>() {
            @Override
            public DataSource.Factory<Integer, ChatsItem> call() {
                return AppDatabase.getDatabase().chatDao().getChatsBySearch(searchQuery, isGroup);
            }
        };

        Future<DataSource.Factory<Integer, ChatsItem>> future = Executors.newSingleThreadExecutor().submit(callable);

        DataSource.Factory<Integer, ChatsItem> chats = null;
        try {
            chats = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chats;
    }

    public DataSource.Factory<Integer, ChatsItem> getChatsBySearch(String searchQuery) {
        Callable<DataSource.Factory<Integer, ChatsItem>> callable = new Callable<DataSource.Factory<Integer, ChatsItem>>() {
            @Override
            public DataSource.Factory<Integer, ChatsItem> call() {
                return AppDatabase.getDatabase().chatDao().getChatsBySearch(searchQuery);
            }
        };

        Future<DataSource.Factory<Integer, ChatsItem>> future = Executors.newSingleThreadExecutor().submit(callable);

        DataSource.Factory<Integer, ChatsItem> chats = null;
        try {
            chats = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chats;
    }

    public ChatsItem getChatByChatID(int cid) {
        Callable<ChatsItem> callable = new Callable<ChatsItem>() {
            @Override
            public ChatsItem call() {
                return AppDatabase.getDatabase().chatDao().getChatByChatID(cid);
            }
        };

        Future<ChatsItem> future = Executors.newSingleThreadExecutor().submit(callable);

        ChatsItem chat = null;
        try {
            chat = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chat;
    }
}
