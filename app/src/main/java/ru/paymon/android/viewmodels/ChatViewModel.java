package ru.paymon.android.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.net.RPC;
import ru.paymon.android.test.ChatBoundaryCallback;

public class ChatViewModel extends ViewModel {
    private DataSource.Factory<Integer,RPC.Message>  factory;
    private LiveData<PagedList<RPC.Message>> messages;

    public ChatViewModel(){
        super();
    }

    public LiveData<PagedList<RPC.Message>> getMessages(int chatID){
        if(messages == null){
            factory = ApplicationLoader.db.chatMessageDao().messagesByChatID(chatID);
            final PagedList.Config config = new PagedList.Config.Builder()
                    .setInitialLoadSizeHint(100)
                    .setPageSize(100)
                    .setEnablePlaceholders(false)
                    .build();
            messages = new LivePagedListBuilder<Integer,RPC.Message>(factory, config).setBoundaryCallback(new ChatBoundaryCallback()).build();
        }
        return messages;
    }

    public void insert(RPC.Message message){
        ApplicationLoader.db.chatMessageDao().insert(message);
    }

    public void remove(RPC.Message message){
        ApplicationLoader.db.chatMessageDao().delete(message);
    }
}
