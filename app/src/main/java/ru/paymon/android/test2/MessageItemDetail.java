package ru.paymon.android.test2;

import android.support.annotation.Nullable;

import androidx.recyclerview.selection.ItemDetailsLookup;
import ru.paymon.android.net.RPC;

public class MessageItemDetail extends ItemDetailsLookup.ItemDetails {
    private final int adapterPosition;
    private final RPC.Message selectionKey;

    public MessageItemDetail(int adapterPosition, RPC.Message selectionKey) {
        this.adapterPosition = adapterPosition;
        this.selectionKey = selectionKey;
    }

    @Override
    public int getPosition() {
        return adapterPosition;
    }

    @Nullable
    @Override
    public Object getSelectionKey() {
        return selectionKey;
    }
}
