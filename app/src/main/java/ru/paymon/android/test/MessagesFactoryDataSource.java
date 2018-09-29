package ru.paymon.android.test;

import android.arch.paging.DataSource;

import ru.paymon.android.net.RPC;

public class MessagesFactoryDataSource extends DataSource.Factory<Integer,RPC.Message> {
    private int chatID;
    private boolean isGroup;

    public MessagesFactoryDataSource(int chatID, boolean isGroup) {
        this.chatID = chatID;
        this.isGroup = isGroup;
    }

    @Override
    public DataSource<Integer,RPC.Message> create() {
        return new ChatDataSource(chatID, isGroup);
    }

    public int getChatID() {
        return chatID;
    }

    public boolean isGroup() {
        return isGroup;
    }
}
