package ru.paymon.android.selection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import androidx.recyclerview.selection.ItemKeyProvider;
import ru.paymon.android.net.RPC;

public class MessageItemKeyProvider extends ItemKeyProvider<RPC.Message> {
    private final List<RPC.Message> items;

    public MessageItemKeyProvider(int scope, List<RPC.Message> items) {
        super(scope);
        this.items = items;
    }

    @Nullable
    @Override
    public RPC.Message getKey(int position) {
        return items.get(position);
    }

    @Override
    public int getPosition(@NonNull RPC.Message key) {
        return items.indexOf(key);
    }
}
