package ru.paymon.android.models;

import ru.paymon.android.net.RPC;

public class ChatsSearchItem {
    public long id;
    public String name;
    public RPC.PM_photo photo;

    public ChatsSearchItem(long id, String name, RPC.PM_photo photo) {
        this.id = id;
        this.name = name;
        this.photo = photo;
    }
}
