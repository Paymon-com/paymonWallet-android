package ru.paymon.android.test;

import android.arch.persistence.room.TypeConverter;

import ru.paymon.android.net.RPC;

public class PeerConverter {
//    @TypeConverter
//    public static RPC.Peer toPeer(SerializableData stream) {
//        return RPC.Peer.PMdeserialize(stream, stream.readInt32(true), true);
//    }
//
//    @TypeConverter
//    public static SerializableData toStream(RPC.Peer peer) {
//        SerializedBuffer data = null;
//        try {
//            data = new SerializedBuffer(peer.getSize());
//            peer.serializeToStream(data);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return data;
//    }

    @TypeConverter
    public static RPC.Peer toPeer(String string) {
        String[] parts = string.split(";");
        int cid = Integer.parseInt(parts[0]);
        int gid = Integer.parseInt(parts[1]);
        int chid = Integer.parseInt(parts[2]);
        if (cid > 0)
            return new RPC.PM_peerUser(cid);
        else if (gid > 0)
            return new RPC.PM_peerGroup(gid);
        else if (chid > 0)
            return new RPC.PM_peerChannel(chid);
        return null;
    }

    @TypeConverter
    public static String toString(RPC.Peer peer) {
        return peer.user_id + ";" + peer.group_id + ";" + peer.channel_id;
    }
}
