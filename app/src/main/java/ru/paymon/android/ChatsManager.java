package ru.paymon.android;

import android.arch.paging.DataSource;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.RPC;
import ru.paymon.android.room.AppDatabase;
import ru.paymon.android.utils.Utils;

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

    public List<ChatsItem> getChatsList() {
        Callable<List<ChatsItem>> callable = new Callable<List<ChatsItem>>() {
            @Override
            public List<ChatsItem> call() {
                return AppDatabase.getDatabase().chatDao().getChatsList();
            }
        };

        Future<List<ChatsItem>> future = Executors.newSingleThreadExecutor().submit(callable);

        List<ChatsItem> chats = null;
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

    public void removeWasteChats(final List<RPC.Message> messages) {
        AppDatabase.dbQueue.postRunnable(() -> {
            final List<ChatsItem> cachedChats = getChatsList();
            final List<ChatsItem> freshChats = new ArrayList<>();

            for (RPC.Message message : messages) {
                AppDatabase.getDatabase().chatMessageDao().insert(message);
                boolean isGroup = !(message.to_peer instanceof RPC.PM_peerUser);

                ChatsItem chatsItem;
                if (!isGroup) {
                    final int cid = message.from_id == User.currentUser.id ? message.to_peer.user_id : message.from_id;
                    final RPC.UserObject user = UsersManager.getInstance().getUser(cid);
                    chatsItem = ChatsManager.getInstance().getChatByChatID(-cid);
                    if (chatsItem == null) {
                        chatsItem = new ChatsItem(cid, user.photoURL, Utils.formatUserName(user), message.text, message.date, message.itemType);
                    } else {
                        chatsItem.fileType = message.itemType;
                        chatsItem.lastMessageText = message.text;
                        chatsItem.photoURL = user.photoURL;
                        chatsItem.time = message.date;
                    }
                } else {
                    final int gid = message.to_peer.group_id;
                    final RPC.Group group = GroupsManager.getInstance().getGroup(gid);
                    final RPC.UserObject creator = UsersManager.getInstance().getUser(group.creatorID);
                    final String text = message.action instanceof RPC.PM_messageActionGroupCreate ? String.format("%s %s \"%s\"", Utils.formatUserName(creator), ApplicationLoader.applicationContext.getString(R.string.created_group), group.title) : message.text;
                    final RPC.UserObject lastMsgUser = UsersManager.getInstance().getUser(message.from_id);
                    chatsItem = new ChatsItem(gid, group.photoURL, group.title, text, message.date, message.itemType, lastMsgUser.photoURL);
                }

                freshChats.add(chatsItem);
            }

            if (cachedChats.size() > 0) {
                for (final ChatsItem cachedChatsItem : cachedChats) {
                    boolean found = false;
                    for (final ChatsItem freshChatItem : freshChats) {
                        if (cachedChatsItem.id == freshChatItem.id) {
                            putChat(freshChatItem);
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        removeChat(cachedChatsItem);
                }
            }else{
                for (final ChatsItem freshChatItem : freshChats)
                    putChat(freshChatItem);
            }
        });
    }
}
