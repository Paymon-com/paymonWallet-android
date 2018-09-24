package ru.paymon.android.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.models.ChatsGroupItem;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class ChatsViewModel extends AndroidViewModel {
    private MutableLiveData<LinkedList<ChatsItem>> chatsItemsData;
    private MutableLiveData<LinkedList<ChatsItem>> dialogsItemsData;
    private MutableLiveData<LinkedList<ChatsItem>> groupsItemsData;
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();

    public ChatsViewModel(@NonNull Application application) {
        super(application);
    }

    public void updateChatsData(){
        loadChatsData();
    }

    public LiveData<LinkedList<ChatsItem>> getChatsData() {
        if (chatsItemsData == null)
            chatsItemsData = new MutableLiveData<>();
        return chatsItemsData;
    }

    public LiveData<LinkedList<ChatsItem>> getDialogsData() {
        if (dialogsItemsData == null)
            dialogsItemsData = new MutableLiveData<>();
        return dialogsItemsData;
    }

    public LiveData<LinkedList<ChatsItem>> getGroupsData() {
        if (groupsItemsData == null)
            groupsItemsData = new MutableLiveData<>();
        return groupsItemsData;
    }

    public LiveData<Boolean> getProgressState() {
        return showProgress;
    }

    private void loadChatsData() {
        if (NetworkManager.getInstance().isAuthorized()) {

            if (showProgress.getValue() != null && showProgress.getValue())
                return;

                Utils.netQueue.postRunnable(() -> {
                    if (User.currentUser == null) return;

                    showProgress.postValue(true);

                    NetworkManager.getInstance().sendRequest(new RPC.PM_chatsAndMessages(), (response, error) -> {
                        if (response == null && error != null) {
                            ApplicationLoader.applicationHandler.post(() -> NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.dialogsNeedReload));
                            return;
                        }

                        RPC.PM_chatsAndMessages packet = (RPC.PM_chatsAndMessages) response;

                        if (packet == null) return;

                        for (RPC.Message msg : packet.messages)
                            MessagesManager.getInstance().putMessage(msg);

                        for (RPC.UserObject usr : packet.users)
                            UsersManager.getInstance().putUser(usr);

                        for (RPC.Group grp : packet.groups)
                            GroupsManager.getInstance().putGroup(grp);

                        for (int i = 0; i < MessagesManager.getInstance().dialogsMessages.size(); i++) {
                            LinkedList<RPC.Message> array = MessagesManager.getInstance().dialogsMessages.get(MessagesManager.getInstance().dialogsMessages.keyAt(i));
                            Collections.sort(array, (chatItem1, chatItem2) -> Long.compare(chatItem1.date, chatItem2.date) * -1);
                            RPC.Message msg = array.getFirst();
                            if (msg.to_id.user_id == User.currentUser.id)
                                MessagesManager.getInstance().lastMessages.put(msg.from_id, msg.id);
                            else
                                MessagesManager.getInstance().lastMessages.put(msg.to_id.user_id, msg.id);
                        }

                        for (int i = 0; i < MessagesManager.getInstance().groupsMessages.size(); i++) {
                            LinkedList<RPC.Message> array = MessagesManager.getInstance().groupsMessages.get(MessagesManager.getInstance().groupsMessages.keyAt(i));
                            Collections.sort(array, (chatItem1, chatItem2) -> Long.compare(chatItem1.date, chatItem2.date) * -1);
                            RPC.Message msg = array.getFirst();
                            if (msg.to_id.group_id == User.currentUser.id)
                                MessagesManager.getInstance().lastGroupMessages.put(msg.from_id, msg.id);
                            else
                                MessagesManager.getInstance().lastGroupMessages.put(msg.to_id.group_id, msg.id);
                        }

                        LinkedList<ChatsItem> dialogsItems = new LinkedList<>();
                        for (int i = 0; i < UsersManager.getInstance().userContacts.size(); i++) {
                            RPC.UserObject user = UsersManager.getInstance().userContacts.get(UsersManager.getInstance().userContacts.keyAt(i));

                            String username = Utils.formatUserName(user);
                            String lastMessageText = "";

                            RPC.Message lastMsg = MessagesManager.getInstance().messages.get(MessagesManager.getInstance().lastMessages.get(user.id));
                            if (lastMsg != null) {
                                if (lastMsg instanceof RPC.PM_message) {
                                    lastMessageText = lastMsg.text;
                                } else if (lastMsg instanceof RPC.PM_messageItem) {
                                    switch (lastMsg.itemType) {
                                        case STICKER:
                                            lastMessageText = getApplication().getString(R.string.sticker);
                                            break;

                                        case ACTION:
                                            lastMessageText = lastMsg.text;
                                            break;
                                    }
                                }
                                dialogsItems.add(new ChatsItem(user.id, user.photoURL, username, lastMessageText, lastMsg.date, lastMsg.itemType));
                            }
                        }


                        LinkedList<ChatsItem> groupItems = new LinkedList<>();
                        for (int i = 0; i < GroupsManager.getInstance().groups.size(); i++) {
                            RPC.Group group = GroupsManager.getInstance().groups.get(GroupsManager.getInstance().groups.keyAt(i));
                            String title = group.title;
                            String lastMessageText = "";

                            Long lastMsgId = MessagesManager.getInstance().lastGroupMessages.get(group.id);
                            if (lastMsgId != null) {
                                RPC.PM_photoURL lastMsgPhoto = null;
                                RPC.Message lastMessage = MessagesManager.getInstance().messages.get(lastMsgId);
                                if (lastMessage != null) {
                                    if (lastMessage instanceof RPC.PM_message) {
                                        lastMessageText = lastMessage.text;
                                    } else if (lastMessage instanceof RPC.PM_messageItem) {
                                        switch (lastMessage.itemType) {
                                            case STICKER:
                                                lastMessageText = getApplication().getString(R.string.sticker);
                                                break;

                                            case ACTION:
                                                lastMessageText = lastMessage.text;
                                                break;
                                        }
                                    }

                                    RPC.UserObject user = UsersManager.getInstance().users.get(lastMessage.from_id);
                                    if (user != null)
                                        lastMsgPhoto = user.photoURL;

                                    groupItems.add(new ChatsGroupItem(group.id, group.photoURL, lastMsgPhoto, title, lastMessageText, lastMessage.date, lastMessage.itemType));
                                }
                            }
                        }

                        if (!dialogsItems.isEmpty()) {
                            Collections.sort(dialogsItems, (chatItem1, chatItem2) -> Long.compare(chatItem1.time, chatItem2.time) * -1);
                            dialogsItemsData.postValue(dialogsItems);
                        }

                        if (!groupItems.isEmpty()) {
                            Collections.sort(groupItems, (chatItem1, chatItem2) -> Long.compare(chatItem1.time, chatItem2.time) * -1);
                            groupsItemsData.postValue(groupItems);
                        }

                        if (!dialogsItems.isEmpty() || !groupItems.isEmpty()) {
                            LinkedList<ChatsItem> chatsItems = new LinkedList<>();
                            chatsItems.addAll(dialogsItems);
                            chatsItems.addAll(groupItems);
                            Collections.sort(chatsItems, (chatItem1, chatItem2) -> Long.compare(chatItem1.time, chatItem2.time) * -1);
                            chatsItemsData.postValue(chatsItems);
                        }

                        showProgress.postValue(false);
                    });
                });
        }
    }

}
