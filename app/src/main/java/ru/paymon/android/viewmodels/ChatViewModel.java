package ru.paymon.android.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import ru.paymon.android.MessagesManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.pagedlib.MessagesBoundaryCallback;

public class ChatViewModel extends ViewModel {
    private DataSource.Factory<Integer,RPC.Message>  factory;
    private LiveData<PagedList<RPC.Message>> messages;

    public ChatViewModel(){
        super();
    }

    public LiveData<PagedList<RPC.Message>> getMessages(int chatID, boolean isGroup){
//        if(messages == null){
            factory = MessagesManager.getInstance().getMessagesByChatID(chatID);
            final PagedList.Config config = new PagedList.Config.Builder()
                    .setInitialLoadSizeHint(15)
                    .setPageSize(15)
                    .setEnablePlaceholders(false)
                    .build();
            messages = new LivePagedListBuilder<Integer,RPC.Message>(factory, config).setBoundaryCallback(new MessagesBoundaryCallback(chatID, isGroup)).build();
//        }
        return messages;
    }
}
