package ru.paymon.android.pagedlib;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import ru.paymon.android.models.ChatsItem;

public class ChatDiffUtilCallback extends DiffUtil.ItemCallback<ChatsItem> {
    @Override
    public boolean areItemsTheSame(@NonNull ChatsItem oldChatItem, @NonNull ChatsItem newChatItem) {
        return oldChatItem == newChatItem;
    }

    @Override
    public boolean areContentsTheSame(@NonNull ChatsItem oldChatItem, @NonNull ChatsItem newChatItem) {
        return oldChatItem.id == newChatItem.id && oldChatItem.name.equals(newChatItem.name) &&
                oldChatItem.lastMessageText.equals(newChatItem.lastMessageText) && oldChatItem.chatID == newChatItem.chatID &&
                oldChatItem.time == newChatItem.time && oldChatItem.lastMsgPhotoURL == newChatItem.lastMsgPhotoURL &&
                oldChatItem.photoURL == newChatItem.photoURL;
    }
}
