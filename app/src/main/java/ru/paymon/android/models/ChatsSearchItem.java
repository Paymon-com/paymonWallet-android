package ru.paymon.android.models;

import ru.paymon.android.net.RPC;

public class ChatsSearchItem {
    public long id;
    public String name;
    public String photo;
    public boolean isGroup;

    public ChatsSearchItem(long id, String name, String photo, boolean isGroup) {
        this.id = id;
        this.name = name;
        this.photo = photo;
        this.isGroup = isGroup;
    }
}
