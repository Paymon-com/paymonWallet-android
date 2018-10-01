package ru.paymon.android.typeconverters;

import android.arch.persistence.room.TypeConverter;

import ru.paymon.android.net.RPC;

public class PhotoUrlConverter {

    @TypeConverter
    public static RPC.PM_photoURL toPhotoURL(String string) {
        if (!string.equals("")) {
            String[] parts = string.split(";");
            int cid = Integer.parseInt(parts[1]);
            int gid = Integer.parseInt(parts[2]);
            int chid = Integer.parseInt(parts[3]);
            RPC.Peer peer = null;
            peer = cid > 0 ? new RPC.PM_peerUser() : peer;
            peer = gid > 0 ? new RPC.PM_peerGroup() : peer;
            peer = chid > 0 ? new RPC.PM_peerChannel() : peer;
            return new RPC.PM_photoURL(peer, parts[0]);
        } else {
            return null;
        }
    }

    @TypeConverter
    public static String toString(RPC.PM_photoURL photoURL) {
        if (photoURL == null)
            return "";
        else
            return photoURL.url + ";" + photoURL.peer.user_id + ";" + photoURL.peer.group_id + ";" + photoURL.peer.channel_id;
    }
}
