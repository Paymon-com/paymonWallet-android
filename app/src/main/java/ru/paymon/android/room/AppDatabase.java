package ru.paymon.android.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import ru.paymon.android.models.ExchangeRate;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.RPC;

@Database(entities = {RPC.Message.class, ChatsItem.class, RPC.UserObject.class, RPC.Group.class,  ExchangeRate.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "address_book").allowMainThreadQueries().build();
                    //.addMigrations(MIGRATION_1_2)
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

//    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(final SupportSQLiteDatabase database) {
//            database.execSQL(
//                    "CREATE TABLE address_book_new (address TEXT NOT NULL, label TEXT NULL, PRIMARY KEY(address))");
//            database.execSQL(
//                    "INSERT OR IGNORE INTO address_book_new (address, label) SELECT address, label FROM address_book");
//            database.execSQL("DROP TABLE address_book");
//            database.execSQL("ALTER TABLE address_book_new RENAME TO address_book");
//        }
//    };
}
