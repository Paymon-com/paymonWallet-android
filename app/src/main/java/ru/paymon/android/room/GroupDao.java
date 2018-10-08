package ru.paymon.android.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import ru.paymon.android.net.RPC;

@Dao
public interface GroupDao {
    @Query("SELECT * FROM `Group`")
    List<RPC.Group> getGroups();

    @Query("SELECT * FROM `Group` WHERE id = :id")
    RPC.Group getGroupById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<RPC.Group> groups);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RPC.Group group);

    @Delete
    void delete(RPC.Group group);
}
