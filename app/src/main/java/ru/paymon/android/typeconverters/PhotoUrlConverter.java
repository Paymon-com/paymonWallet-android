package ru.paymon.android.typeconverters;

import android.arch.persistence.room.TypeConverter;

import ru.paymon.android.net.RPC;

public class PhotoUrlConverter {

    @TypeConverter
    public static RPC.PM_photoURL toPhotoURL(String string) {
        if (!string.isEmpty()) {
            String[] parts = string.split(";");
            RPC.Peer peer = null;
            for (int i = 1; i < parts.length; i++) {
                int id = Integer.parseInt(parts[i]);
                if (id > 0) {
                    switch (i) {
                        case 1:
                            peer = new RPC.PM_peerUser(id);
                            break;
                        case 2:
                            peer = new RPC.PM_peerGroup(id);
                            break;
                        case 3:
                            peer = new RPC.PM_peerChannel(id);
                            break;
                    }
                }
            }
            return new RPC.PM_photoURL(peer, parts[0]);
        } else {
            return null;
        }
    }

    @TypeConverter
    public static String toString(RPC.PM_photoURL photoURL) {
        if (photoURL == null || photoURL.peer == null)
            return "";
        else
            return photoURL.url + ";" + photoURL.peer.user_id + ";" + photoURL.peer.group_id + ";" + photoURL.peer.channel_id;
    }
}
