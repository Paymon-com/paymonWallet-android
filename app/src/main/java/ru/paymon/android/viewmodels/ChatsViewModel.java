package ru.paymon.android.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;
import android.util.Log;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
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
        chats = new LivePagedListBuilder<>(ApplicationLoader.db.chatDao().getChats(), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getGroupChats() {
        chats = new LivePagedListBuilder<>(ApplicationLoader.db.chatDao().getChatsByChatType(true), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getDialogsChats() {
        chats = new LivePagedListBuilder<>(ApplicationLoader.db.chatDao().getChatsByChatType(false), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getDialogsChatsBySearch(String searchQuery) {
        chats = new LivePagedListBuilder<>(ApplicationLoader.db.chatDao().getChatsBySearch("%" + searchQuery + "%", false), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getGroupsChatsBySearch(String searchQuery) {
        chats = new LivePagedListBuilder<>(ApplicationLoader.db.chatDao().getChatsBySearch("%" + searchQuery + "%", true), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getChatsBySearch(String searchQuery) {
        chats = new LivePagedListBuilder<>(ApplicationLoader.db.chatDao().getChatsBySearch("%" + searchQuery + "%"), config).build();
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

                    Log.e("AAA", packet.groups.size() + " " + packet.users.size() + " " + packet.messages.size());

//                    final ArrayList<ChatsItem> chatsItems = new ArrayList<>();
//                    final SparseArray<RPC.Message> usersMessagesMap = new SparseArray<>();
//                    final SparseArray<RPC.Message> groupsMessagesMap = new SparseArray<>();
//                    final SparseArray<RPC.UserObject> usersMap = new SparseArray<>();
//                    for (final RPC.Message msg : packet.messages) {
//                        if (msg.to_peer instanceof RPC.PM_peerUser)
//                            usersMessagesMap.put(msg.to_id, msg);
//                        else
//                            groupsMessagesMap.put(msg.to_id, msg);
//                    }

//                    for (final RPC.UserObject usr : packet.users) {
//                        usersMap.put(usr.id, usr);
//                        final RPC.Message msg = usersMessagesMap.get(usr.id);
//                        if (msg == null) continue;
//                        chatsItems.add(new ChatsItem(usr.id, usr.photoURL, Utils.formatUserName(usr), msg.text, msg.date, msg.itemType));
//                    }
//
//                    for (final RPC.Group grp : packet.groups) {
//                        UsersManager.getInstance().putUsers(grp.users);
//                        final RPC.Message msg = groupsMessagesMap.get(grp.id);
//                        if (msg == null) continue;
//                        final RPC.UserObject lastUsr = usersMap.get(msg.from_id);
//                        chatsItems.add(new ChatsItem(grp.id, grp.photoURL, grp.title, msg.text, msg.date, msg.itemType, lastUsr == null ? null : lastUsr.photoURL));
//                    }

                    UsersManager.getInstance().putUsers(packet.users);
                    GroupsManager.getInstance().putGroups(packet.groups);
                    MessagesManager.getInstance().putMessages(packet.messages);
//                    ApplicationLoader.db.chatDao().insertList(chatsItems);
//                    ApplicationLoader.db.userDao().insertList(packet.users);
//                    ApplicationLoader.db.groupDao().insertList(packet.groups);
                    isChatsLoaded = true;
//                    if (chatsItems.size() > 0)
//                        isChatsLoaded = true;

                    showProgress.postValue(false);
                });
            }
        });

    }

}
