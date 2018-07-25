package ru.paymon.android;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Base64;
import android.util.Log;
import android.util.LongSparseArray;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.SerializedStream;

import static ru.paymon.android.ApplicationLoader.db;

public class DBHelper extends SQLiteOpenHelper {
    public static final String TABlE_MESSAGES = "messages";
    public static final String TABlE_USERS = "users";
    public static final String TABlE_GROUPS = "groups";
    public static final String TABlE_CHATS = "chats";
    public static final String TABlE_CHANNELS = "channels";

    public static final String KEY_ID = "id";
    public static final String KEY_FROM_ID = "from_id";
    public static final String KEY_CHANNEL_ID = "channel_id";
    public static final String KEY_GROUP_ID = "group_id";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_REPLY_TO_ID = "reply_to_id";
    public static final String KEY_TEXT = "text";
    public static final String KEY_UNREAD = "unread";
    public static final String KEY_ITEM_TYPE = "item_type";
    public static final String KEY_ITEM_ID = "item_id";
    public static final String KEY_DATA = "data";
    public static final String KEY_FIRST_NAME = "f_name";
    public static final String KEY_LAST_NAME = "l_name";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CREATOR_ID = "creator_id";
    public static final String KEY_USERS_COUNT = "users_count";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "CacheDB";


    /*
        MESSAGES

    0   id              int
    1   from_id         int
    2   channel_id      int
    3   user_id         int
    4   group_id        int
    5   reply_to_id     int
    6   text            text
    7   unread          tinyint
    8   item_type       int
    9   item_id         int
    10  data            blob
     */

    /*
        Users

    0   id             int
    1   f_name         varchar
    2   l_name         varchar
    3  data            blob
     */

    /*
        Groups

    0   id             int
    1   title          varchar
    2   creator_id     int
    3   user_count     int
    4   data           blob
     */

    /*
        Channels

    0   id             int
    1   title          varchar
    2   user_count     int
    3   data           blob
     */

    /*
        Chats

    0   id              int
    1   channel_id      int
    2   user_id         int
    3   group_id        int
     */


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        updateDatabase(sqLiteDatabase, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int previousVersion, int currentVersion) {
        updateDatabase(sqLiteDatabase, previousVersion, currentVersion);
    }

    private void updateDatabase(SQLiteDatabase sqLiteDatabase, int previousVersion, int currentVersion) {
        switch (previousVersion) {
            case 0:
                sqLiteDatabase.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                        "%s INTEGER UNSIGNED NOT NULL PRIMARY KEY, " +
                        "%s INTEGER UNSIGNED NULL DEFAULT 0, " +
                        "%s INTEGER UNSIGNED NULL DEFAULT 0, " +
                        "%s INTEGER UNSIGNED NULL DEFAULT 0, " +
                        "%s INTEGER UNSIGNED NULL DEFAULT 0, " +
                        "%s INTEGER UNSIGNED NULL DEFAULT 0, " +
                        "%s LONGTEXT NOT NULL, " +
                        "%s TINYINT UNSIGNED NOT NULL DEFAULT 0, " +
                        "%s TINYINT UNSIGNED NOT NULL DEFAULT 0, " +
                        "%s INTEGER UNSIGNED NULL DEFAULT 0, " +
                        "%s BLOB NOT NULL" +
                        " );", TABlE_MESSAGES, KEY_ID, KEY_FROM_ID, KEY_CHANNEL_ID, KEY_GROUP_ID, KEY_USER_ID, KEY_REPLY_TO_ID, KEY_TEXT, KEY_UNREAD, KEY_ITEM_TYPE, KEY_ITEM_ID, KEY_DATA));
                sqLiteDatabase.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                        "%s INTEGER UNSIGNED NOT NULL PRIMARY KEY, " +
                        "%s VARCHAR(128), " +
                        "%s VARCHAR(128), " +
                        "%s BLOB NOT NULL" +
                        " );", TABlE_USERS, KEY_ID, KEY_FIRST_NAME, KEY_LAST_NAME, KEY_DATA));
                sqLiteDatabase.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                        "%s INTEGER UNSIGNED NOT NULL PRIMARY KEY, " +
                        "%s VARCHAR(128), " +
                        "%s INTEGER UNSIGNED NULL DEFAULT 0, " +
                        "%s INTEGER UNSIGNED NULL DEFAULT 0, " +
                        "%s BLOB NOT NULL" +
                        " );", TABlE_GROUPS, KEY_ID, KEY_TITLE, KEY_CREATOR_ID, KEY_USERS_COUNT, KEY_DATA));
                sqLiteDatabase.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                        "%s INTEGER UNSIGNED NOT NULL PRIMARY KEY, " +
                        "%s VARCHAR(128), " +
                        "%s INTEGER UNSIGNED NULL DEFAULT 0, " +
                        "%s BLOB NOT NULL" +
                        " );", TABlE_CHANNELS, KEY_ID, KEY_TITLE, KEY_USERS_COUNT, KEY_DATA));
                sqLiteDatabase.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                        "%s INTEGER UNSIGNED NOT NULL PRIMARY KEY, " +
                        "%s INTEGER UNSIGNED NULL DEFAULT 0, " +
                        "%s INTEGER UNSIGNED NULL DEFAULT 0, " +
                        "%s INTEGER UNSIGNED NULL DEFAULT 0 " +
                        " );", TABlE_CHATS, KEY_ID, KEY_USER_ID, KEY_GROUP_ID, KEY_CHANNEL_ID));

                sqLiteDatabase.setVersion(DATABASE_VERSION);
                break;
            case 1:
//                db.execSQL("ALTER TABLE CONTACT ADD COLUMN AGE INTEGER;");
//                sqLiteDatabase.setVersion(2);
//                onUpgrade(sqLiteDatabase, sqLiteDatabase.getVersion(), currentVersion);
                break;
        }
    }

    public static void putMessage(RPC.Message message) {
        SerializedStream data = new SerializedStream();
        message.serializeToStream(data);
        byte[] messageData = data.toByteArray();
        SQLiteStatement sqLiteStatement = ApplicationLoader.db.compileStatement(String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', ?, '%s', '%s', ?);",
                TABlE_MESSAGES,
                KEY_ID, KEY_FROM_ID, KEY_CHANNEL_ID, KEY_USER_ID, KEY_GROUP_ID, KEY_REPLY_TO_ID, KEY_TEXT, KEY_UNREAD, KEY_ITEM_TYPE, KEY_ITEM_ID, KEY_DATA,
                message.id, message.from_id, message.to_id.channel_id, message.to_id.user_id, message.to_id.group_id, message.reply_to_msg_id, message.text, message.unread, message.itemType, message.itemID));
        sqLiteStatement.bindBlob(1, messageData);
        sqLiteStatement.execute();
    }

    public static void putMessages(LongSparseArray<RPC.Message> messages) {
        for (int i = 0; i < messages.size(); i++) {
            RPC.Message message = messages.get(messages.keyAt(i));
            putMessage(message);
        }
    }

    public static void updateMessage(RPC.Message message) {
        SerializedStream data = new SerializedStream();
        message.serializeToStream(data);
        byte[] messageData = data.toByteArray();

        SQLiteStatement sqLiteStatement = ApplicationLoader.db.compileStatement("UPDATE " + TABlE_MESSAGES + " SET " +
                KEY_FROM_ID + "='" + message.from_id + "', " +
                KEY_CHANNEL_ID + "='" + message.to_id.channel_id + "', " +
                KEY_USER_ID + "='" + message.to_id.user_id + "', " +
                KEY_GROUP_ID + "='" + message.to_id.group_id + "', " +
                KEY_REPLY_TO_ID + "='" + message.reply_to_msg_id + "', " +
                KEY_TEXT + "='" + message.text + "', " +
                KEY_UNREAD + "='" + message.unread + "', " +
                KEY_ITEM_TYPE + "='" + message.itemType + "', " +
                KEY_ITEM_ID + "='" + message.itemID + "', " +
                KEY_DATA + "=? " +
                "WHERE " + KEY_ID + "='" + message.id + "'");
        sqLiteStatement.bindBlob(1, messageData);
        sqLiteStatement.execute();
    }

    public static void updateMessages(LongSparseArray<RPC.Message> messages) {
        for (int i = 0; i < messages.size(); i++) {
            RPC.Message message = messages.get(messages.keyAt(i));
            updateMessage(message);
        }
    }

    public static LongSparseArray<RPC.Message> getAllMessages() {
        LongSparseArray<RPC.Message> messages = new LongSparseArray<>();

        Cursor cursor = ApplicationLoader.db.rawQuery("SELECT * FROM " + TABlE_MESSAGES, null);

        while (cursor.moveToNext()) {
            byte[] messageData = cursor.getBlob(10);
            SerializedStream serializedStream = new SerializedStream(messageData);
            RPC.Message message = RPC.Message.deserialize(serializedStream, serializedStream.readInt32(false), false);
            messages.put(message.id, message);
        }
        cursor.close();

        return messages;
    }

    public static LongSparseArray<RPC.Message> getMessagesByChatId(int chatID, boolean isGroup) {
        LongSparseArray<RPC.Message> messages = new LongSparseArray<>();
        Cursor cursor;
        if (!isGroup)
            cursor = ApplicationLoader.db.rawQuery("SELECT * FROM " + TABlE_MESSAGES + " WHERE " + KEY_USER_ID + "='" + chatID + "'", null);
        else
            cursor = ApplicationLoader.db.rawQuery("SELECT * FROM " + TABlE_MESSAGES + " WHERE " + KEY_GROUP_ID + "='" + chatID + "'", null);

        while (cursor.moveToNext()) {
            byte[] messageData = cursor.getBlob(10);
            SerializedStream serializedStream = new SerializedStream(messageData);
            RPC.Message message = RPC.Message.deserialize(serializedStream, serializedStream.readInt32(false), false);
            messages.put(message.id, message);
        }
        cursor.close();

        return messages;
    }

    public static List<RPC.Message> getMessagesByText(String text) {
        List<RPC.Message> messages = new ArrayList<>();

        Cursor cursor = ApplicationLoader.db.rawQuery("SELECT * FROM " + TABlE_MESSAGES + " WHERE " + KEY_TEXT + "='" + text + "'", null);

        while (cursor.moveToNext()) {
            byte[] messageData = cursor.getBlob(10);
            SerializedStream serializedStream = new SerializedStream(messageData);
            RPC.Message message = RPC.Message.deserialize(serializedStream, serializedStream.readInt32(false), false);
            messages.add(message);
        }
        cursor.close();

        return messages;
    }

    public static void putUsers(List<RPC.UserObject> userObjectList) {
        for (RPC.UserObject userObject : userObjectList) {
            SerializedStream data = new SerializedStream();
            userObject.serializeToStream(data);
            byte[] userData = data.toByteArray();
            ApplicationLoader.db.execSQL(String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (%s, %s, %s, %s);",
                    TABlE_USERS,
                    KEY_ID, KEY_FIRST_NAME, KEY_LAST_NAME, KEY_DATA,
                    userObject.id, userObject.first_name, userObject.last_name, userData));
        }
    }

    public static List<RPC.UserObject> getAllUsers() {
        List<RPC.UserObject> userObjectList = new ArrayList<>();

        Cursor cursor = ApplicationLoader.db.rawQuery("SELECT * FROM " + TABlE_USERS, null);

        while (cursor.moveToNext()) {
            String data = cursor.getString(3);
            byte[] messageData = data.getBytes();
            SerializedStream serializedStream = new SerializedStream(messageData);
            RPC.UserObject userObject = RPC.UserObject.deserialize(serializedStream, serializedStream.readInt32(false), false);
            userObjectList.add(userObject);
        }
        cursor.close();

        return userObjectList;
    }


    public static void putGroups(List<RPC.Group> groupList) {
        for (RPC.Group group : groupList) {
            SerializedStream data = new SerializedStream();
            group.serializeToStream(data);
            byte[] groupData = data.toByteArray();
            ApplicationLoader.db.execSQL(String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (%s, %s, %s, %s, %s);",
                    TABlE_GROUPS,
                    KEY_ID, KEY_TITLE, KEY_CREATOR_ID, KEY_USERS_COUNT, KEY_DATA,
                    group.id, group.title, group.creatorID, group.users.size(), groupData));
        }
    }

    public static List<RPC.Group> getAllGroups() {
        List<RPC.Group> groupList = new ArrayList<>();

        Cursor cursor = ApplicationLoader.db.rawQuery("SELECT * FROM " + TABlE_GROUPS, null);

        while (cursor.moveToNext()) {
            String data = cursor.getString(3);
            byte[] groupData = data.getBytes();
            SerializedStream serializedStream = new SerializedStream(groupData);
            RPC.Group group = RPC.Group.PMdeserialize(serializedStream, serializedStream.readInt32(false), false);
            groupList.add(group);
        }
        cursor.close();

        return groupList;
    }
}
