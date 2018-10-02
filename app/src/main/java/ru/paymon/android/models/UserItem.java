package ru.paymon.android.models;

import ru.paymon.android.net.RPC;

public class UserItem {
    public int uid;
    public String name;
    public RPC.PM_photoURL photo;
    public boolean checked;
    public boolean isHidden;

    public UserItem(int uid, String name, RPC.PM_photoURL photo){
        this.uid = uid;
        this.name = name;
        this.photo = photo;
        checked = false;
    }
}
