package ru.paymon.android.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ru.paymon.android.models.UserItem;
import ru.paymon.android.net.RPC;

@Dao
public interface UserDao {

    @Query("SELECT * FROM userobject")
    List<RPC.UserObject> getAll();

    @Query("SELECT * FROM userobject WHERE id = :uid")
    RPC.UserObject getById(int uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<RPC.UserObject> userObjects);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RPC.UserObject userObject);

    @Delete
    void delete(RPC.UserObject userObject);
}
