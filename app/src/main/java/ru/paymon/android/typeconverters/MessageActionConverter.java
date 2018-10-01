package ru.paymon.android.typeconverters;

import android.arch.persistence.room.TypeConverter;

import ru.paymon.android.net.RPC;

public class MessageActionConverter {
    @TypeConverter
    public static RPC.MessageAction toMessageAction(String string){
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
