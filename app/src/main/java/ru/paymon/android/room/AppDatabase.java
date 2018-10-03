package ru.paymon.android.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.RPC;

@Database(entities = {RPC.Message.class, ChatsItem.class, RPC.UserObject.class, RPC.Group.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ChatMessageDao chatMessageDao();
    public abstract ChatDao chatsDao();
    public abstract UserDao userDao();
    public abstract GroupDao groupDao();
}
