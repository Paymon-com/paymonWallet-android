package ru.paymon.android.models;

import ru.paymon.android.net.RPC;

public class CreateGroupItem {
    public int uid;
    public String name;
    public RPC.PM_photoURL photo;
    public boolean checked;

    public CreateGroupItem(int uid, String name, RPC.PM_photoURL photo){
        this.uid = uid;
        this.name = name;
        this.photo = photo;
        checked = false;
    }
}
