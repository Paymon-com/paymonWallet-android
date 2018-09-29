package ru.paymon.android.test;

import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import ru.paymon.android.net.RPC;

public class ChatBoundaryCallback extends PagedList.BoundaryCallback<RPC.Message> {
    @Override
    public void onZeroItemsLoaded() {
        super.onZeroItemsLoaded();
    }

    @Override
    public void onItemAtFrontLoaded(@NonNull RPC.Message itemAtFront) {
        super.onItemAtFrontLoaded(itemAtFront);
    }

    @Override
    public void onItemAtEndLoaded(@NonNull RPC.Message itemAtEnd) {
        super.onItemAtEndLoaded(itemAtEnd);
    }
}
