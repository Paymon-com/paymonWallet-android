package ru.paymon.android;

import android.arch.paging.DataSource;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.room.AppDatabase;
import ru.paymon.android.utils.Utils;


public class MessagesManager {
    private static AtomicInteger lastMessageID = new AtomicInteger(0);
    private static volatile MessagesManager Instance = null;
    public int currentChatID;

    public static MessagesManager getInstance() {
        MessagesManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (MessagesManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new MessagesManager();
                }
            }
        }
        return localInstance;
    }

    public static int generateMessageID() {
        return lastMessageID.incrementAndGet();
    }


    public void putMessage(final RPC.Message message) {
        Executors.newSingleThreadExecutor().submit(() -> {
            AppDatabase.getDatabase().chatMessageDao().insert(message);
            boolean isGroup = !(message.to_peer instanceof RPC.PM_peerUser);

            if (!isGroup) {
                final int cid = message.from_id == User.currentUser.id ? message.to_peer.user_id : message.from_id;
                final RPC.UserObject user = UsersManager.getInstance().getUser(cid);

                if (user == null) {
                    final RPC.PM_getUserInfo userInfo = new RPC.PM_getUserInfo(cid);
                    NetworkManager.getInstance().sendRequest(userInfo, (response, error) -> {
                        if (response == null || error != null) return;
                        final RPC.UserObject userObject = (RPC.UserObject) response;
                        UsersManager.getInstance().putUser(userObject);
                        ChatsItem chatsItem = ChatsManager.getInstance().getChatByChatID(-cid);
                        if (chatsItem == null) {
                            chatsItem = new ChatsItem(cid, userObject.photoURL, Utils.formatUserName(userObject), message.text, message.date, message.itemType);
                        } else {
                            chatsItem.fileType = message.itemType;
                            chatsItem.lastMessageText = message.text;
                            chatsItem.photoURL = userObject.photoURL;
                            chatsItem.time = message.date;
                        }
                        ChatsManager.getInstance().putChat(chatsItem);
                    });
                } else {
                    ChatsItem chatsItem = ChatsManager.getInstance().getChatByChatID(-cid);
                    if (chatsItem == null) {
                        chatsItem = new ChatsItem(cid, user.photoURL, Utils.formatUserName(user), message.text, message.date, message.itemType);
                    } else {
                        chatsItem.fileType = message.itemType;
                        chatsItem.lastMessageText = message.text;
                        chatsItem.photoURL = user.photoURL;
                        chatsItem.time = message.date;
                    }
                    ChatsManager.getInstance().putChat(chatsItem);
                }
            } else {
                final int gid = message.to_peer.group_id;
                final RPC.Group group = GroupsManager.getInstance().getGroup(gid);

                if (group == null) {
//                final RPC.PM_getGroupInfo groupInfo = new RPC.PM_getGroupInfo(gid);
//                NetworkManager.getInstance().sendRequest(groupInfo, (response, error) -> {
//                    if (response == null || error != null) return;
//                    RPC.Group groupObject = (RPC.Group) response;
//                    AppDatabase.getDatabase().groupDao().insert(groupObject);
//                    RPC.UserObject lastMsgUser = AppDatabase.getDatabase().userDao().getUserById(message.from_id);
//                    ChatsItem newChatsItem = new ChatsItem(gid, groupObject.photoURL, groupObject.title, message.text, message.date, message.itemType, lastMsgUser.photoURL);
//                    ChatsManager.getInstance().getChatByChatID.putChat(newChatsItem);
//                });
                } else {
                    final RPC.UserObject lastMsgUser = UsersManager.getInstance().getUser(message.from_id);
                    final ChatsItem newChatsItem = new ChatsItem(gid, group.photoURL, group.title, message.text, message.date, message.itemType, lastMsgUser.photoURL);
                    ChatsManager.getInstance().putChat(newChatsItem);
                }
            }
        });
    }

    public void putMessages(List<RPC.Message> messageList) {
        Executors.newSingleThreadExecutor().submit(() -> {
            for (RPC.Message message : messageList)
                putMessage(message);
        });
    }

    public void deleteMessage(RPC.Message message) {
        Executors.newSingleThreadExecutor().submit(() -> AppDatabase.getDatabase().chatMessageDao().delete(message));
    }

    public void deleteMessages(List<Long> ids) {
        Executors.newSingleThreadExecutor().submit(() -> {
            for (long id : ids) {
                final RPC.Message message = AppDatabase.getDatabase().chatMessageDao().getMessageByMessageId(id);
                deleteMessage(message);
            }
        });
    }

    public List<RPC.Message> getMessagesByChatIDList(int cid){
        Callable<List<RPC.Message>> callable = new Callable<List<RPC.Message>>() {
            @Override
            public List<RPC.Message> call() {
                return AppDatabase.getDatabase().chatMessageDao().getMessagesByChatIDList(cid);
            }
        };

        Future<List<RPC.Message>> future = Executors.newSingleThreadExecutor().submit(callable);

        List<RPC.Message> messagesList = null;
        try {
            messagesList = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messagesList;
    }

    public DataSource.Factory<Integer, RPC.Message> getMessagesByChatID(int cid){
        Callable<DataSource.Factory<Integer,RPC.Message>> callable = new Callable<DataSource.Factory<Integer,RPC.Message>>() {
            @Override
            public DataSource.Factory<Integer,RPC.Message> call() {
                return AppDatabase.getDatabase().chatMessageDao().getMessagesByChatID(cid);
            }
        };

        Future<DataSource.Factory<Integer,RPC.Message>> future = Executors.newSingleThreadExecutor().submit(callable);

        DataSource.Factory<Integer,RPC.Message> user = null;
        try {
            user = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }
}
