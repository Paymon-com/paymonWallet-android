package ru.paymon.android.test;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import ru.paymon.android.net.RPC;

public class DiffUtilCallback extends DiffUtil.ItemCallback<RPC.Message> {
    @Override
    public boolean areItemsTheSame(@NonNull RPC.Message message, @NonNull RPC.Message t1) {
        return message == t1;
    }

    @Override
    public boolean areContentsTheSame(@NonNull RPC.Message message, @NonNull RPC.Message t1) {
        return message.text.equals(t1.text);
    }
}
