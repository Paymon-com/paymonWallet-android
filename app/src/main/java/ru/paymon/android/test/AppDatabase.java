package ru.paymon.android.test;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import ru.paymon.android.net.RPC;

@Database(entities = {RPC.Message.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ChatMessageDao chatMessageDao();
}
