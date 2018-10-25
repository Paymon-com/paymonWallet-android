package ru.paymon.android.pagedlib;

import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import java.util.List;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.User;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;

public class MessagesBoundaryCallback extends PagedList.BoundaryCallback<RPC.Message> implements NotificationManager.IListener {
    private int chatID;
    private boolean isGroup;
    private boolean isAllMessagedDownloaded;
    private final int pageSize = 100;

    public MessagesBoundaryCallback(final int chatID, final boolean isGroup) {
        this.chatID = chatID;
        this.isGroup = isGroup;
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.userAuthorized);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.userAuthorized);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
    }

    @Override
    public void onZeroItemsLoaded() {
        super.onZeroItemsLoaded();
        loadMessages(0, pageSize);
    }

    @Override
    public void onItemAtEndLoaded(@NonNull RPC.Message itemAtEnd) {
        super.onItemAtEndLoaded(itemAtEnd);

        List<RPC.Message> messages = ApplicationLoader.db.chatMessageDao().getMessagesByChatIDList(chatID);
        int offset = 0;
        for (int i = 0; i < messages.size(); i++) {
            RPC.Message message = messages.get(i);
            if (message.id == itemAtEnd.id)
                offset = i;
        }

        if (!isAllMessagedDownloaded)
            loadMessages(offset, pageSize);
    }

    @Override
    public void onItemAtFrontLoaded(@NonNull RPC.Message itemAtFront) {
        super.onItemAtFrontLoaded(itemAtFront);
    }

    private void loadMessages(final int offset, final int count) {
        if (User.currentUser == null || chatID == 0) return;

        RPC.PM_getChatMessages packet = new RPC.PM_getChatMessages();

        packet.chatID = !isGroup ? new RPC.PM_peerUser(chatID) : new RPC.PM_peerGroup(chatID);
        packet.offset = offset;
        packet.count = count;

        NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
            if (response == null || error != null) return;
            final RPC.PM_chat_messages receivedMessages = (RPC.PM_chat_messages) response;
            MessagesManager.getInstance().putMessages(receivedMessages.messages);
            if (receivedMessages.messages.size() == 0 || receivedMessages.messages.size() < pageSize) {
                isAllMessagedDownloaded = true;
            }
        });
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        if (event == NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED || event == NotificationManager.NotificationEvent.userAuthorized) {
            loadMessages(0, pageSize);
        }
    }
}
