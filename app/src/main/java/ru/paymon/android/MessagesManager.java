package ru.paymon.android;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
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
        ApplicationLoader.db.chatMessageDao().insert(message);
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
                    ChatsItem chatsItem = ApplicationLoader.db.chatDao().getChatByChatID(-cid);
                    if (chatsItem == null) {
                        chatsItem = new ChatsItem(cid, userObject.photoURL, Utils.formatUserName(userObject), message.text, message.date, message.itemType);
                    } else {
                        chatsItem.fileType = message.itemType;
                        chatsItem.lastMessageText = message.text;
                        chatsItem.photoURL = userObject.photoURL;
                        chatsItem.time = message.date;
                    }
                    ApplicationLoader.db.chatDao().insert(chatsItem);
                });
            } else {
                ChatsItem chatsItem = ApplicationLoader.db.chatDao().getChatByChatID(-cid);
                if (chatsItem == null) {
                    chatsItem = new ChatsItem(cid, user.photoURL, Utils.formatUserName(user), message.text, message.date, message.itemType);
                } else {
                    chatsItem.fileType = message.itemType;
                    chatsItem.lastMessageText = message.text;
                    chatsItem.photoURL = user.photoURL;
                    chatsItem.time = message.date;
                }
                ApplicationLoader.db.chatDao().insert(chatsItem);
            }
        } else {
            final int gid = message.to_peer.group_id;
            final RPC.Group group = GroupsManager.getInstance().getGroup(gid);

            if (group == null) {
//                final RPC.PM_getGroupInfo groupInfo = new RPC.PM_getGroupInfo(gid); //TODO:дописать
//                NetworkManager.getInstance().sendRequest(groupInfo, (response, error) -> {
//                    if (response == null || error != null) return;
//                    RPC.Group groupObject = (RPC.Group) response;
//                    ApplicationLoader.db.groupDao().insert(groupObject);
//                    RPC.UserObject lastMsgUser = ApplicationLoader.db.userDao().getUserById(message.from_id);
//                    ChatsItem newChatsItem = new ChatsItem(gid, groupObject.photoURL, groupObject.title, message.text, message.date, message.itemType, lastMsgUser.photoURL);
//                    ApplicationLoader.db.chatDao().insert(newChatsItem);
//                });
            } else {
                final RPC.UserObject lastMsgUser = UsersManager.getInstance().getUser(message.from_id);
                final ChatsItem newChatsItem = new ChatsItem(gid, group.photoURL, group.title, message.text, message.date, message.itemType, lastMsgUser.photoURL);
                ApplicationLoader.db.chatDao().insert(newChatsItem);
            }
        }
    }

    public void putMessages(List<RPC.Message> messageList) {
        for (RPC.Message message : messageList) {
            putMessage(message);
        }
    }

    public void deleteMessage(RPC.Message message) {
        ApplicationLoader.db.chatMessageDao().delete(message);
    }
}
