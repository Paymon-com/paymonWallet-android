package ru.paymon.android.typeconverters;

import android.arch.persistence.room.TypeConverter;

import java.util.ArrayList;

public class UsersArrayConverter {

//    @TypeConverter
//    public static String toString(ArrayList<RPC.UserObject> userObjects) {
//        String result = "";
//        for (RPC.UserObject user : userObjects) {
//            result += user.id + ";";
//        }
//        UsersManager.getInstance().putUsers(userObjects);
//        return result;
//    }
//
//    @TypeConverter
//    public static ArrayList<RPC.UserObject> toArray(String string) {
//        String[] parts = string.split(";");
//        ArrayList<RPC.UserObject> result = new ArrayList<>();
//        for (String part : parts) {
//            int id = Integer.parseInt(part);
//            result.add(UsersManager.getInstance().getUser(id));
//        }
//        return result;
//    }

    @TypeConverter
    public static String toString(ArrayList<Integer> uids) {
        String result = "";
        for (Integer uid : uids) {
            result += uid + ";";
        }
        return result;
    }

    @TypeConverter
    public static ArrayList<Integer> toArray(String string) {
        String[] parts = string.split(";");
        ArrayList<Integer> result = new ArrayList<>();
        for (String part : parts) {
            int id = Integer.parseInt(part);
            result.add(id);
        }
        return result;
    }
}
