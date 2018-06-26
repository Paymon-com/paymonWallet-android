package ru.paymon.android.models;


import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.FileManager;

public class ChatsGroupItem extends ChatsItem {
    public RPC.PM_photo lastMsgPhoto;

    public ChatsGroupItem(int chatID, RPC.PM_photo photo, RPC.PM_photo lastMsgPhoto, String name, String lastMessage, long time, FileManager.FileType fileType) {
        super(chatID, photo, name, lastMessage, time, fileType);
        this.lastMsgPhoto = lastMsgPhoto;
    }
}
