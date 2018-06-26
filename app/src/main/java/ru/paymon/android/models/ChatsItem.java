package ru.paymon.android.models;


import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.FileManager;

public class ChatsItem {
    public RPC.PM_photo photo;
    public String name;
    public String lastMessageText;
    public long time;
    public int chatID;
    public FileManager.FileType fileType;

    public ChatsItem(int chatID, RPC.PM_photo photo, String name, String lastMessageText, long time, FileManager.FileType fileType) {
        this.chatID = chatID;
        this.photo = photo;
        this.name = name;
        this.lastMessageText = lastMessageText;
        this.time = time;
        this.fileType = fileType;
    }
}
