package ru.paymon.android.room;

import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import ru.paymon.android.models.ChatsItem;

@Dao
public interface ChatDao {

    @Query("SELECT * FROM ChatsItem  WHERE isGroup = :isGroup ORDER BY time DESC")
    DataSource.Factory<Integer, ChatsItem> chatsByChatType(boolean isGroup);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<ChatsItem> chatsItems);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatsItem chatsItem);

    @Delete
    void delete(ChatsItem chatsItem);

    @Query("SELECT * FROM ChatsItem ORDER BY time DESC")
    DataSource.Factory<Integer, ChatsItem> chats();

    @Query("SELECT * FROM ChatsItem WHERE name LIKE :searchQuery ORDER BY time DESC")
    DataSource.Factory<Integer, ChatsItem> chatsBySearch(String searchQuery);

    @Query("SELECT * FROM ChatsItem WHERE name LIKE :searchQuery AND isGroup = :isGroup ORDER BY time DESC")
    DataSource.Factory<Integer, ChatsItem> chatsBySearch(String searchQuery, boolean isGroup);
}