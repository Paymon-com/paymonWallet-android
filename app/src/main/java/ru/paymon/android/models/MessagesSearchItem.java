package ru.paymon.android.models;

import ru.paymon.android.net.RPC;

public class MessagesSearchItem {
    public long id;
    public String name;
    public String message;
    public RPC.PM_photoURL photo;

    public MessagesSearchItem(long id, String name, String message, RPC.PM_photoURL photo){
        this.id = id;
        this.name = name;
        this.message = message;
        this.photo = photo;
    }
}
