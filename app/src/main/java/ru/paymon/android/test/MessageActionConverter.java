package ru.paymon.android.test;

import android.arch.persistence.room.TypeConverter;

import ru.paymon.android.net.RPC;

public class MessageActionConverter {
    @TypeConverter
    public static RPC.MessageAction toMessageAction(String string){
//        String[] parts = string.split(";");
//        String message = parts[0];
//        for(int i = 1; i < parts.length; i++){
//            String userStr = parts[i];
//            String[] userParts = userStr.split(",");
//            ArrayList<RPC.UserObject> userObjects = new ArrayList<>();
//            for (String userPart:userParts) {
//                RPC.UserObject userObject = new RPC.UserObject();
//            }
//        }
        switch (Integer.parseInt(string)){
            case 0:
                return new RPC.PM_messageActionGroupCreate();
        }
        return null;
    }

    @TypeConverter
    public static String toString(RPC.MessageAction messageAction){
        return messageAction instanceof RPC.PM_messageActionGroupCreate ? "0" : "-1";
    }
}
