package ru.paymon.android.pagedlib;

import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import ru.paymon.android.net.RPC;

public class MessagesBoundaryCallback extends PagedList.BoundaryCallback<RPC.Message> {
    @Override
    public void onZeroItemsLoaded() {
        super.onZeroItemsLoaded();
        //TODO:Скачивать первые 15 сообщений (offset 0, count 15)
    }

    @Override
    public void onItemAtEndLoaded(@NonNull RPC.Message itemAtEnd) {
        super.onItemAtEndLoaded(itemAtEnd);
        //TODO:Догружать с сервера сообщения, которые идут до itemAtEnd.id
    }

    @Override
    public void onItemAtFrontLoaded(@NonNull RPC.Message itemAtFront) {
        super.onItemAtFrontLoaded(itemAtFront);
    }
}
