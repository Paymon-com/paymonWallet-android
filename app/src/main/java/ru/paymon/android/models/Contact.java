//package ru.paymon.android.models;
//
//import ru.paymon.android.net.RPC;
//import ru.paymon.android.utils.Utils;
//
//public class Contact {
//    public int id;
//    public String name;
//    public String phone;
//    public boolean isChecked;
//    public RPC.PM_photoURL photo;
//
//    public Contact(RPC.UserObject user) {
//        this.id = user.id;
//        this.name = Utils.formatUserName(user);
//        this.phone = Utils.formatPhone(user.phoneNumber);
//        this.photo = user.photoURL;
//    }
//
//    public Contact(int id, String name, String phone) {
//        this.id = id;
//        this.name = name;
//        this.phone = Utils.formatPhone(phone);
//    }
//}
//
