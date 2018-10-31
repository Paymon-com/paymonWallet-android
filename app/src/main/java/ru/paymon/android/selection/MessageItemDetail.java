package ru.paymon.android.selection;

import android.support.annotation.Nullable;

import androidx.recyclerview.selection.ItemDetailsLookup;
import ru.paymon.android.net.RPC;

public class MessageItemDetail extends ItemDetailsLookup.ItemDetails<RPC.Message> {
    private final int position;
    private final RPC.Message item;

    public MessageItemDetail(int position, RPC.Message item) {
        this.position = position;
        this.item = item;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Nullable
    @Override
    public RPC.Message getSelectionKey() {
        return item;
    }
}
