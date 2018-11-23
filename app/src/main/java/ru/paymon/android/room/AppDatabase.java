package ru.paymon.android.room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.models.ExchangeRate;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.DispatchQueue;

@Database(entities = {RPC.Message.class, ChatsItem.class, RPC.UserObject.class, RPC.Group.class, ExchangeRate.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public static DispatchQueue dbQueue = new DispatchQueue("dbQueue");
    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase() {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(ApplicationLoader.applicationContext, AppDatabase.class, "database")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ChatMessageDao chatMessageDao();

    public abstract ChatDao chatDao();

    public abstract UserDao userDao();

    public abstract GroupDao groupDao();

    public abstract ExchangeRatesDao exchangeRatesDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(final SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE UserObject");
        }
    };
}
