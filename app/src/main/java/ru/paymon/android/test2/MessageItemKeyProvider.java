package ru.paymon.android.test2;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import androidx.recyclerview.selection.ItemKeyProvider;
import ru.paymon.android.net.RPC;

public class MessageItemKeyProvider extends ItemKeyProvider {
    private final List<RPC.Message> itemList;

    public MessageItemKeyProvider(int scope, List<RPC.Message> itemList) {
        super(scope);
        this.itemList = itemList;
    }

    @Nullable
    @Override
    public Object getKey(int position) {
        return itemList.get(position);
    }

    @Override
    public int getPosition(@NonNull Object key) {
        return itemList.indexOf(key);
    }
}
