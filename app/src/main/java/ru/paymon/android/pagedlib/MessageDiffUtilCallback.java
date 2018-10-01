package ru.paymon.android.pagedlib;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import ru.paymon.android.net.RPC;

public class MessageDiffUtilCallback extends DiffUtil.ItemCallback<RPC.Message> {
    @Override
    public boolean areItemsTheSame(@NonNull RPC.Message oldMessage, @NonNull RPC.Message newMessage) {
        return oldMessage == newMessage;
    }

    @Override
    public boolean areContentsTheSame(@NonNull RPC.Message oldMessage, @NonNull RPC.Message newMessage) {
        return oldMessage.text.equals(newMessage.text) && oldMessage.unread == newMessage.unread && oldMessage.id == newMessage.id;
    }
}
