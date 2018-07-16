package ru.paymon.android;

import android.util.LongSparseArray;
import android.util.SparseArray;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;

public class MessagesManager implements NotificationManager.IListener {
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

    private MessagesManager() {
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.RECEIVED_NEW_MESSAGES);
    }

    public void dispose() {
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.RECEIVED_NEW_MESSAGES);
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

    public void deleteMessage(RPC.Message msg) {
        if (messages.get(msg.id) == null) return;

        int chatID;

        int to_id = msg.to_id.user_id;
        if (to_id == 0) {
            chatID = to_id;
        } else {
            if (msg.from_id == User.currentUser.id)
                chatID = to_id;
            else
                chatID = msg.from_id;
        }

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
                    ApplicationLoader.applicationHandler.post(() -> NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.dialogsNeedReload));
                    return;
                }

                RPC.PM_chatsAndMessages packet = (RPC.PM_chatsAndMessages) response;

                if (packet == null) return;

                for (RPC.Message msg : packet.messages)
                    putMessage(msg);

                for (RPC.Group grp : packet.groups)
                    GroupsManager.getInstance().putGroup(grp);

                for (RPC.UserObject usr : packet.users)
                    UsersManager.getInstance().putUser(usr);

                for (int i = 0; i < dialogsMessages.size(); i++) {
                    LinkedList<RPC.Message> array = dialogsMessages.get(dialogsMessages.keyAt(i));
                    Collections.sort(array, (chatItem1, chatItem2) -> Long.compare(chatItem1.date, chatItem2.date) * -1);
                    RPC.Message msg = array.getFirst();
                    if (msg.to_id.user_id == User.currentUser.id)
                        lastMessages.put(msg.from_id, msg.id);
                    else
                        lastMessages.put(msg.to_id.user_id, msg.id);
                }

                for (int i = 0; i < groupsMessages.size(); i++) {
                    LinkedList<RPC.Message> array = groupsMessages.get(groupsMessages.keyAt(i));
                    Collections.sort(array, (chatItem1, chatItem2) -> Long.compare(chatItem1.date, chatItem2.date) * -1);
                    RPC.Message msg = array.getFirst();
                    if (msg.to_id.group_id == User.currentUser.id)
                        lastGroupMessages.put(msg.from_id, msg.id);
                    else
                        lastGroupMessages.put(msg.to_id.group_id, msg.id);
                }

                ApplicationLoader.applicationHandler.post(() ->
                        NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.dialogsNeedReload)
                );
            });
        }
    }

    public void loadMessages(int chatID, int count, int offset, boolean isGroup) {
        if (User.currentUser == null || chatID == 0) return;

        RPC.PM_getChatMessages packet = new RPC.PM_getChatMessages();

        if (!isGroup) {
            packet.chatID = new RPC.PM_peerUser();
            packet.chatID.user_id = chatID;
        } else {
            packet.chatID = new RPC.PM_peerGroup();
            packet.chatID.group_id = chatID;
        }

        packet.count = count;
        packet.offset = offset;

        NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
            if (response == null) return;
            final RPC.PM_chat_messages receivedMessages = (RPC.PM_chat_messages) response;
            if (receivedMessages.messages.size() == 0) return;

            final LinkedList<Long> messagesToAdd = new LinkedList<>();
            for (RPC.Message msg : receivedMessages.messages) {
                putMessage(msg);
                messagesToAdd.add(msg.id);
            }

            ApplicationLoader.applicationHandler.post(() -> NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.chatAddMessages, messagesToAdd, true, receivedMessages.messages.size()));
        });
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent id, Object... args) {
        if (id == NotificationManager.NotificationEvent.RECEIVED_NEW_MESSAGES) {
            LinkedList<RPC.Message> messages = (LinkedList<RPC.Message>) args[0];
            LinkedList<Long> messagesToShow = new LinkedList<>();

            for (RPC.Message msg : messages) {
                putMessage(msg);
                int to_id = msg.to_id.user_id;
                boolean isGroup = false;
                if (to_id == 0) {
                    isGroup = true;
                    to_id = msg.to_id.group_id;
                }
                if (!isGroup)
                    if ((to_id == currentChatID && msg.from_id == User.currentUser.id) || (to_id == User.currentUser.id && msg.from_id == currentChatID)) {
                        messagesToShow.add(msg.id);
                    } else {
                        if (to_id == currentChatID)
                            messagesToShow.add(msg.id);
                    }
            }
            if (messagesToShow.size() > 0) {
                Collections.sort(messagesToShow, Long::compareTo);
                NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.chatAddMessages, messagesToShow, false);
            }
        }
    }
}
