package ru.paymon.android.test;

import android.arch.paging.PositionalDataSource;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Collections;

import ru.paymon.android.MessagesManager;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;

public class ChatDataSource extends PositionalDataSource<RPC.Message> {
    private int chatID;
    private boolean isGroup;

    public ChatDataSource(int chatID, boolean isGroup) {
        super();
        this.chatID = chatID;
        this.isGroup = isGroup;
    }


    private void fetchInitialMessages(int from, int count, LoadInitialCallback<RPC.Message> callback) {
        Log.e("AAA", "AAA");
//        List<RPC.Message> messagesFromDB = ApplicationLoader.db.chatMessageDao().messagesByChatID(chatID);
//        callback.onResult(messagesFromDB, 0);

//        ChatMessageDao chatMessageDao = ApplicationLoader.db.chatMessageDao();
//        List<RPC.Message> messagesFromDB = chatMessageDao.messagesByChatID(chatID);
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
//        packet.offset = from;
//
//        if(messagesFromDB.isEmpty()) {
//            Log.e("AAA", "isEmpty");
//            NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
//                if (response == null) return;
//                final RPC.PM_chat_messages receivedMessages = (RPC.PM_chat_messages) response;
//                if (receivedMessages.messages.size() == 0) return;
//                for (RPC.Message msg : receivedMessages.messages)
//                    MessagesManager.getInstance().putMessage(msg);
//                Collections.sort(receivedMessages.messages, (messageItem1, messageItem2) -> Long.compare(messageItem1.date, messageItem2.date) * -1);
//                chatMessageDao.insertList(receivedMessages.messages);
//                callback.onResult(receivedMessages.messages, 0);
//            });
//        }else{
//            Log.e("AAA", "Loaded from db");
//            callback.onResult(messagesFromDB,0);
//            NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
//                if (response == null) return;
//                final RPC.PM_chat_messages receivedMessages = (RPC.PM_chat_messages) response;
//                if (receivedMessages.messages.size() == 0) return;
//                for (RPC.Message msg : receivedMessages.messages)
//                    MessagesManager.getInstance().putMessage(msg);
//                Collections.sort(receivedMessages.messages, (messageItem1, messageItem2) -> Long.compare(messageItem1.date, messageItem2.date) * -1);
//                chatMessageDao.insertList(receivedMessages.messages);
//                callback.onResult(receivedMessages.messages, 0);
//                invalidate();
//            });
//        }
    }

    private void fetchMessages(int from, int count, LoadRangeCallback<RPC.Message> callback) {
        RPC.PM_getChatMessages packet = new RPC.PM_getChatMessages();

        if (!isGroup) {
            packet.chatID = new RPC.PM_peerUser();
            packet.chatID.user_id = chatID;
        } else {
            packet.chatID = new RPC.PM_peerGroup();
            packet.chatID.group_id = chatID;
        }

        packet.count = count;
        packet.offset = from;

        NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
            if (response == null) return;
            final RPC.PM_chat_messages receivedMessages = (RPC.PM_chat_messages) response;
            if (receivedMessages.messages.size() == 0) return;
            for (RPC.Message msg : receivedMessages.messages)
                MessagesManager.getInstance().putMessage(msg);
            Collections.sort(receivedMessages.messages, (messageItem1, messageItem2) -> Long.compare(messageItem1.date, messageItem2.date) * -1);
            callback.onResult(receivedMessages.messages);
        });
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<RPC.Message> callback) {
        fetchInitialMessages(params.requestedStartPosition, params.requestedLoadSize, callback);
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<RPC.Message> callback) {
//        fetchMessages(params.startPosition, params.loadSize, callback);
    }
}
