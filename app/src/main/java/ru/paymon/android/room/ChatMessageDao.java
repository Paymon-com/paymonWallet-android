package ru.paymon.android.room;

import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import ru.paymon.android.net.RPC;

@Dao
public interface ChatMessageDao {

    @Query("SELECT * FROM message WHERE to_id = :cid ORDER BY date DESC")
    DataSource.Factory<Integer,RPC.Message> messagesByChatID(int cid);

    @Query("SELECT * FROM message WHERE id = :id")
    RPC.Message messageByChatID(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<RPC.Message> messageList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RPC.Message message);

    @Delete
    void delete(RPC.Message message);

    @Query("SELECT * FROM message ORDER BY date DESC")
    List<RPC.Message> messages();
}
