package ru.paymon.android.models;

import ru.paymon.android.net.RPC;

public class AddAdministratorsItem {
    public int uid;
    public String name;
    public RPC.PM_photo photo;
    public boolean checked;

    public AddAdministratorsItem(int uid, String name, RPC.PM_photo photo){
        this.uid = uid;
        this.name = name;
        this.photo = photo;
        checked = false;
    }
}
