package ru.paymon.android.models;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import ru.paymon.android.net.RPC;
import ru.paymon.android.typeconverters.FileTypeConverter;
import ru.paymon.android.typeconverters.PhotoUrlConverter;
import ru.paymon.android.utils.FileManager;

@Entity
public class ChatsItem {
    @PrimaryKey
    public long id;
    @TypeConverters(PhotoUrlConverter.class)
    public RPC.PM_photoURL photoURL;
    public String name;
    public String lastMessageText;
    public long time;
    public int chatID;
    @TypeConverters(FileTypeConverter.class)
    public FileManager.FileType fileType;
    @TypeConverters(PhotoUrlConverter.class)
    public RPC.PM_photoURL lastMsgPhotoURL;
    public boolean isGroup;

    public ChatsItem(long id, int chatID, boolean isGroup, RPC.PM_photoURL photoURL, String name, String lastMessageText, long time, FileManager.FileType fileType, RPC.PM_photoURL lastMsgPhotoURL) {
        this.id = id;
        this.chatID = chatID;
        this.isGroup = isGroup;
        this.photoURL = photoURL;
        this.name = name;
        this.lastMessageText = lastMessageText;
        this.time = time;
        this.fileType = fileType;
        this.lastMsgPhotoURL = lastMsgPhotoURL;
    }

    @Ignore
    public ChatsItem(int chatID, RPC.PM_photoURL photoURL, String name, String lastMessageText, long time, FileManager.FileType fileType) {
        this.id = -chatID;
        this.chatID = chatID;
        this.isGroup = false;
        this.photoURL = photoURL;
        this.name = name;
        this.lastMessageText = lastMessageText;
        this.time = time;
        this.fileType = fileType;
    }

    @Ignore
    public ChatsItem(int chatID, RPC.PM_photoURL photoURL, String name, String lastMessageText, long time, FileManager.FileType fileType, RPC.PM_photoURL lastMsgPhotoURL) {
        this.id = chatID;
        this.chatID = chatID;
        this.isGroup = true;
        this.photoURL = photoURL;
        this.name = name;
        this.lastMessageText = lastMessageText;
        this.time = time;
        this.fileType = fileType;
        this.lastMsgPhotoURL = lastMsgPhotoURL;
    }
}
