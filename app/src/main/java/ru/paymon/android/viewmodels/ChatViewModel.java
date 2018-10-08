package ru.paymon.android.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.net.RPC;

public class ChatViewModel extends ViewModel {
    private DataSource.Factory<Integer,RPC.Message>  factory;
    private LiveData<PagedList<RPC.Message>> messages;

    public ChatViewModel(){
        super();
    }

    public LiveData<PagedList<RPC.Message>> getMessages(int chatID){
        if(messages == null){
            factory = ApplicationLoader.db.chatMessageDao().getMessagesByChatID(chatID);
            final PagedList.Config config = new PagedList.Config.Builder()
                    .setInitialLoadSizeHint(100)
                    .setPageSize(100)
                    .setEnablePlaceholders(false)
                    .build();
            messages = new LivePagedListBuilder<Integer,RPC.Message>(factory, config).build();
        }
        return messages;
    }
}
