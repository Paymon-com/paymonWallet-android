package ru.paymon.android.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.User;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class ChatsViewModel extends AndroidViewModel {
    public int chatsScrollY;
    public int sortedBy = 2;
    public boolean isChatsLoaded;
    public boolean isSearchActivated;
    public String searchText;
    private final PagedList.Config config = new PagedList.Config.Builder().setInitialLoadSizeHint(50).setPageSize(10).setEnablePlaceholders(false).build();
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();
    private LiveData<PagedList<ChatsItem>> chats;

    public ChatsViewModel(@NonNull Application application) {
        super(application);
    }

    public void updateChatsData() {
        loadChatsData();
    }

    public LiveData<PagedList<ChatsItem>> getChats() {
        chats = new LivePagedListBuilder<>(ApplicationLoader.db.chatsDao().chats(), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getGroupChats() {
        chats = new LivePagedListBuilder<>(ApplicationLoader.db.chatsDao().chatsByChatType(true), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getDialogsChats() {
        chats = new LivePagedListBuilder<>(ApplicationLoader.db.chatsDao().chatsByChatType(false), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getDialogsChatsBySearch(String searchQuery) {
        chats = new LivePagedListBuilder<>(ApplicationLoader.db.chatsDao().chatsBySearch("%" + searchQuery + "%", false), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getGroupsChatsBySearch(String searchQuery) {
        chats = new LivePagedListBuilder<>(ApplicationLoader.db.chatsDao().chatsBySearch("%" + searchQuery + "%", true), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getChatsBySearch(String searchQuery) {
        chats = new LivePagedListBuilder<>(ApplicationLoader.db.chatsDao().chatsBySearch("%" + searchQuery + "%"), config).build();
        return chats;
    }


    public LiveData<Boolean> getProgressState() {
        return showProgress;
    }

    private void loadChatsData() {
        Utils.netQueue.postRunnable(() -> {
            if (showProgress.getValue() != null && showProgress.getValue())
                return;

            if (NetworkManager.getInstance().isAuthorized() && User.currentUser != null) {
                showProgress.postValue(true);

                NetworkManager.getInstance().sendRequest(new RPC.PM_chatsAndMessages(), (response, error) -> {
                    if (error != null || !(response instanceof RPC.PM_chatsAndMessages)) {
                        showProgress.postValue(false);
                        return;
                    }

                    final RPC.PM_chatsAndMessages packet = (RPC.PM_chatsAndMessages) response;
                    Log.e("AAA", packet.count + " " + packet.users.size() + " " + packet.messages.size());
                    final ArrayList<ChatsItem> chatsItems = new ArrayList<>();
                    final SparseArray<RPC.Message> usersMessagesMap = new SparseArray<>();
                    final SparseArray<RPC.Message> groupsMessagesMap = new SparseArray<>();
                    final SparseArray<RPC.UserObject> usersMap = new SparseArray<>();
//                    final SparseArray<RPC.Group> groupsMap = new SparseArray<>();
                    for (final RPC.Message msg : packet.messages) {
                        if (msg.to_peer instanceof RPC.PM_peerUser)
                            usersMessagesMap.put(msg.to_id, msg);
                        else
                            groupsMessagesMap.put(msg.to_id, msg);
                    }

                    for (final RPC.UserObject usr : packet.users) {
                        usersMap.put(usr.id, usr);
                        final RPC.Message msg = usersMessagesMap.get(usr.id);
                        if (msg == null) continue;
                        chatsItems.add(new ChatsItem(usr.id, usr.photoURL, Utils.formatUserName(usr), msg.text, msg.date, msg.itemType));
                    }

                    for (final RPC.Group grp : packet.groups) {
//                        groupsMap.put(grp.id, grp);
                        ApplicationLoader.db.userDao().insertList(grp.users);
                        final RPC.Message msg = groupsMessagesMap.get(grp.id);
                        if (msg == null) continue;
                        final RPC.UserObject lastUsr = usersMap.get(msg.from_id);
                        chatsItems.add(new ChatsItem(grp.id, grp.photoURL, grp.title, msg.text, msg.date, msg.itemType, lastUsr == null ? null : lastUsr.photoURL));
                    }

                    ApplicationLoader.db.chatsDao().insertList(chatsItems);
                    ApplicationLoader.db.userDao().insertList(packet.users);
                    ApplicationLoader.db.groupDao().insertList(packet.groups);

                    if (chatsItems.size() > 0)
                        isChatsLoaded = true;

                    showProgress.postValue(false);

//                    for (RPC.Message msg : packet.messages)
//                        MessagesManager.getInstance().putMessage(msg);

//                    for (RPC.UserObject usr : packet.users) {
//                        UsersManager.getInstance().putUser(usr);
//                        Log.e("AAA", (usr.email == null ? "null" : usr.email) + " qqq"); //TODO: delete this shitty log
//                    }
//
//                    for (RPC.Group grp : packet.groups)
//                        GroupsManager.getInstance().putGroup(grp);
//
//                    for (int i = 0; i < MessagesManager.getInstance().dialogsMessages.size(); i++) {
//                        LinkedList<RPC.Message> array = MessagesManager.getInstance().dialogsMessages.get(MessagesManager.getInstance().dialogsMessages.keyAt(i));
//                        Collections.sort(array, (chatItem1, chatItem2) -> Long.compare(chatItem1.date, chatItem2.date) * -1);
//                        RPC.Message msg = array.getFirst();
//                        if (msg.to_peer.user_id == User.currentUser.id)
//                            MessagesManager.getInstance().lastMessages.put(msg.from_id, msg.id);
//                        else
//                            MessagesManager.getInstance().lastMessages.put(msg.to_peer.user_id, msg.id);
//                    }
//
//                    for (int i = 0; i < MessagesManager.getInstance().groupsMessages.size(); i++) {
//                        LinkedList<RPC.Message> array = MessagesManager.getInstance().groupsMessages.get(MessagesManager.getInstance().groupsMessages.keyAt(i));
//                        Collections.sort(array, (chatItem1, chatItem2) -> Long.compare(chatItem1.date, chatItem2.date) * -1);
//                        RPC.Message msg = array.getFirst();
//                        if (msg.to_peer.group_id == User.currentUser.id)
//                            MessagesManager.getInstance().lastGroupMessages.put(msg.from_id, msg.id);
//                        else
//                            MessagesManager.getInstance().lastGroupMessages.put(msg.to_peer.group_id, msg.id);
//                    }
//
//                    LinkedList<ChatsItem> dialogsItems = new LinkedList<>();
//                    for (int i = 0; i < UsersManager.getInstance().userContacts.size(); i++) {
//                        RPC.UserObject user = UsersManager.getInstance().userContacts.get(UsersManager.getInstance().userContacts.keyAt(i));
//
//                        String username = Utils.formatUserName(user);
//                        String lastMessageText = "";
//
//                        RPC.Message lastMsg = MessagesManager.getInstance().messages.get(MessagesManager.getInstance().lastMessages.get(user.id));
//                        if (lastMsg != null) {
//                            if (lastMsg instanceof RPC.PM_message) {
//                                lastMessageText = lastMsg.text;
//                            } else if (lastMsg instanceof RPC.PM_messageItem) {
//                                switch (lastMsg.itemType) {
//                                    case STICKER:
//                                        lastMessageText = getApplication().getString(R.string.sticker);
//                                        break;
//
//                                    case ACTION:
//                                        lastMessageText = lastMsg.text;
//                                        break;
//                                }
//                            }
//                            dialogsItems.add(new ChatsItem(user.id, user.photoURL, username, lastMessageText, lastMsg.date, lastMsg.itemType));
//                        }
//                    }


//                    LinkedList<ChatsItem> groupItems = new LinkedList<>();
//                    for (int i = 0; i < GroupsManager.getInstance().groups.size(); i++) {
//                        RPC.Group group = GroupsManager.getInstance().groups.get(GroupsManager.getInstance().groups.keyAt(i));
//                        String title = group.title;
//                        String lastMessageText = "";

//                        Long lastMsgId = MessagesManager.getInstance().lastGroupMessages.get(group.id);
//                        if (lastMsgId != null) {
//                            RPC.PM_photoURL lastMsgPhoto = null;
//                            RPC.Message lastMessage = MessagesManager.getInstance().messages.get(lastMsgId);
//                            if (lastMessage != null) {
//                                if (lastMessage instanceof RPC.PM_message) {
//                                    lastMessageText = lastMessage.text;
//                                } else if (lastMessage instanceof RPC.PM_messageItem) {
//                                    switch (lastMessage.itemType) {
//                                        case STICKER:
//                                            lastMessageText = getApplication().getString(R.string.sticker);
//                                            break;
//
//                                        case ACTION:
//                                            lastMessageText = lastMessage.text;
//                                            break;
//                                    }
//                                }
//
//                                RPC.UserObject user = UsersManager.getInstance().users.get(lastMessage.from_id);
//                                if (user != null)
//                                    lastMsgPhoto = user.photoURL;
//
//                                groupItems.add(new ChatsItem(group.id, group.photoURL, title, lastMessageText, lastMessage.date, lastMessage.itemType, lastMsgPhoto));
//                            }
//                        }
//                    }

//                    if (!dialogsItems.isEmpty()) {
//                        Collections.sort(dialogsItems, (chatItem1, chatItem2) -> Long.compare(chatItem1.time, chatItem2.time) * -1);
//                        dialogsItemsData.postValue(dialogsItems);
//                    }

//                    if (!groupItems.isEmpty()) {
//                        Collections.sort(groupItems, (chatItem1, chatItem2) -> Long.compare(chatItem1.time, chatItem2.time) * -1);
//                        groupsItemsData.postValue(groupItems);
//                    }

//                    if (!dialogsItems.isEmpty() || !groupItems.isEmpty()) {
//                        LinkedList<ChatsItem> chatsItems = new LinkedList<>();
//                        chatsItems.addAll(dialogsItems);
//                        chatsItems.addAll(groupItems);
//                        ApplicationLoader.db.chatsDao().insertList(chatsItems);
//                        isChatsLoaded = true;
////                        chatsItemsData.postValue(chatsItems);
//                    }
//
//                    showProgress.postValue(false);
                });
            }
        });

    }

}
