package ru.paymon.android;

import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;


import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.Packet;
import ru.paymon.android.net.RPC;

public class MessagesManager {
    public int currentChatID = 0;
    public LongSparseArray<RPC.Message> messages = new LongSparseArray<>();
    public SparseArray<LinkedList<RPC.Message>> dialogsMessages = new SparseArray<>();
    public SparseArray<LinkedList<RPC.Message>> groupsMessages = new SparseArray<>();
    public LongSparseArray<Long> lastMessages = new LongSparseArray<>();
    public LongSparseArray<Long> lastGroupMessages = new LongSparseArray<>();
    private static AtomicInteger lastMessageID = new AtomicInteger(0);
    private static volatile MessagesManager Instance = null;

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

    public void dispose() {
        Instance = null;
    }

    public static int generateMessageID() {
        return lastMessageID.incrementAndGet();
    }

    public void putMessage(RPC.Message msg) {
        if (messages.get(msg.id) != null) return;

        messages.put(msg.id, msg);

        RPC.PM_userFull currentUser = User.currentUser;

        SparseArray<LinkedList<RPC.Message>> chatsMessages;
        int chatID;

        int to_id = msg.to_id.user_id;
        if (to_id == 0) {
            to_id = msg.to_id.group_id;
            chatID = to_id;
            chatsMessages = groupsMessages;
        } else {
            if (msg.from_id == currentUser.id) {
                chatID = to_id;
            } else {
                chatID = msg.from_id;
            }
            chatsMessages = dialogsMessages;
        }

        LinkedList<RPC.Message> chatMessages = chatsMessages.get(chatID);
        if (chatMessages == null) {
            chatMessages = new LinkedList<>();
        }
        chatMessages.add(msg);
        chatsMessages.put(chatID, chatMessages);
    }

    public void deleteMessage(RPC.Message msg, int chatID) {
        if (messages.get(msg.id) == null) return;

        messages.remove(msg.id);

        if (msg.to_id.user_id != 0) {
            dialogsMessages.get(chatID).remove(msg);
            lastMessages.remove(msg.id);
        } else {
            groupsMessages.get(chatID).remove(msg);
            lastGroupMessages.remove(msg.id);
        }
    }

    public void loadChats(boolean fromCache) {
        if (fromCache) {

        } else {
            if (User.currentUser == null) return;

            NetworkManager.getInstance().sendRequest(new RPC.PM_chatsAndMessages(), (response, error) -> {
                if (response == null && error != null) {
                    ApplicationLoader.applicationHandler.post(() ->
                        NotificationManager.getInstance().postNotificationName(NotificationManager.dialogsNeedReload)
                    );
                    return;
                }

                RPC.PM_chatsAndMessages packet = (RPC.PM_chatsAndMessages) response;

                for (RPC.Message msg : packet.messages)
                    putMessage(msg);

                for (RPC.Group grp : packet.groups)
                    GroupsManager.getInstance().putGroup(grp);

                for (RPC.UserObject usr : packet.users)
                    UsersManager.getInstance().putUser(usr);


                for (int i = 0; i < dialogsMessages.size(); i++) {//TODO: оптимизировать поиск последнего сообщения
                    LinkedList<RPC.Message> dialogMessages = dialogsMessages.get(dialogsMessages.keyAt(i));
                    RPC.Message msg = dialogMessages.getFirst();
                    for (int j = 0; j < dialogMessages.size(); j++) {
                        if (msg.date < dialogMessages.get(j).date)
                            msg = dialogMessages.get(j);
                    }
                    if (msg.to_id.user_id == User.currentUser.id)
                        lastMessages.put(msg.from_id, msg.id);
                    else
                        lastMessages.put(msg.to_id.user_id, msg.id);
                }


                for (int i = 0; i < groupsMessages.size(); i++) {//TODO: оптимизировать поиск последнего сообщения
                    RPC.Message msg = groupsMessages.get(groupsMessages.keyAt(i)).getFirst();
                    LinkedList<RPC.Message> messages = groupsMessages.get(groupsMessages.keyAt(i));
                    for (int j = 0; j < messages.size(); j++) {
                        if (msg.date < messages.get(j).date)
                            msg = messages.get(j);
                    }
                    if (msg.to_id.group_id == User.currentUser.id)
                        lastGroupMessages.put(msg.from_id, msg.id);
                    else
                        lastGroupMessages.put(msg.to_id.group_id, msg.id);
                }

                ApplicationLoader.applicationHandler.post(() ->
                    NotificationManager.getInstance().postNotificationName(NotificationManager.dialogsNeedReload)
                );
            });
        }
    }

    public void loadMessages(int chatID, int count, int offset, boolean isGroup) {
//        if (User.currentUser == null || chatID == 0) return;
//
//        RPC.PM_getChatMessages packet = new RPC.PM_getChatMessages();
//
//        if (!isGroup) {
//            packet.chatID = new RPC.PM_peerUser();
//            packet.chatID.user_id = chatID;
//        } else {
//            packet.chatID = new RPC.PM_peerGroup();
//            packet.chatID.group_id = chatID;
//        }
//
//        packet.count = count;
//        packet.offset = offset;
//
//        NetworkManager.getInstance().sendRequest(packet, new Packet.OnResponseListener() {
//            @Override
//            public void onResponse(Packet response, RPC.PM_error error) {
//                if (response == null) return;
//                final RPC.PM_chat_messages packet = (RPC.PM_chat_messages) response;
//                if (packet.messages.size() == 0) return;
//
//                final LinkedList<Long> messagesToAdd = new LinkedList<>();
//                for (RPC.Message msg : packet.messages) {
//                    putMessage(msg);
//                    messagesToAdd.add(msg.id);
//                }
//
//                ApplicationLoader.applicationHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        NotificationManager.getInstance().postNotificationName(NotificationManager.chatAddMessages, messagesToAdd, true, packet.messages.size());
//                    }
//                });
//            }
//        });
    }

}
