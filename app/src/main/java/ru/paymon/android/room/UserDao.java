package ru.paymon.android.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import ru.paymon.android.net.RPC;

@Dao
public interface UserDao {

    @Query("SELECT * FROM UserObject")
    List<RPC.UserObject> getUsers();

    @Query("SELECT * FROM UserObject WHERE id = :uid")
    RPC.UserObject getUserById(int uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<RPC.UserObject> userObjects);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RPC.UserObject userObject);

    @Delete
    void delete(RPC.UserObject userObject);
}
