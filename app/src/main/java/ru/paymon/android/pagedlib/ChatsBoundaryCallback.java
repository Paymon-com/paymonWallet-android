package ru.paymon.android.pagedlib;

import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import ru.paymon.android.models.ChatsItem;

public class ChatsBoundaryCallback extends PagedList.BoundaryCallback<ChatsItem> {
    @Override
    public void onZeroItemsLoaded() {
        super.onZeroItemsLoaded();
    }

    @Override
    public void onItemAtFrontLoaded(@NonNull ChatsItem itemAtFront) {
        super.onItemAtFrontLoaded(itemAtFront);
    }

    @Override
    public void onItemAtEndLoaded(@NonNull ChatsItem itemAtEnd) {
        super.onItemAtEndLoaded(itemAtEnd);
    }
}