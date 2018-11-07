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
import ru.paymon.android.ChatsManager;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.room.AppDatabase;
import ru.paymon.android.utils.Utils;

public class ChatsViewModel extends AndroidViewModel {
    public int chatsScrollY;
    public int sortedBy = 2;
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
        chats = new LivePagedListBuilder<>(ChatsManager.getInstance().getChats(), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getGroupChats() {
        chats = new LivePagedListBuilder<>(ChatsManager.getInstance().getChatsByChatType(true), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getDialogsChats() {
        chats = new LivePagedListBuilder<>(ChatsManager.getInstance().getChatsByChatType(false), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getDialogsChatsBySearch(String searchQuery) {
        chats = new LivePagedListBuilder<>(ChatsManager.getInstance().getChatsBySearch("%" + searchQuery + "%", false), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getGroupsChatsBySearch(String searchQuery) {
        chats = new LivePagedListBuilder<>(ChatsManager.getInstance().getChatsBySearch("%" + searchQuery + "%", true), config).build();
        return chats;
    }

    public LiveData<PagedList<ChatsItem>> getChatsBySearch(String searchQuery) {
        chats = new LivePagedListBuilder<>(ChatsManager.getInstance().getChatsBySearch("%" + searchQuery + "%"), config).build();
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

                    ChatsManager.getInstance().removeAllChats(); //TODO:после написания синхронизатора удалить

                    UsersManager.getInstance().putUsers(packet.users);
                    GroupsManager.getInstance().putGroups(packet.groups);
                    MessagesManager.getInstance().putMessages(packet.messages);

                    showProgress.postValue(false);
                });
            }
        });

    }

}
