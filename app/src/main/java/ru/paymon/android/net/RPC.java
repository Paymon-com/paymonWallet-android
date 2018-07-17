package ru.paymon.android.net;

import android.os.Parcel;
import android.os.Parcelable;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.SerializableData;


public class RPC {
    public static final int SVUID_ARRAY = 1550732454;
    public static final int SVUID_TRUE = 1606318489;
    public static final int SVUID_FALSE = 1541450798;

    public static final int ERROR_INTERNAL = 0x1;
    public static final int ERROR_KEY = 0x2;
    public static final int ERROR_REGISTER = 0x3;
    public static final int ERROR_AUTH = 0x4;
    public static final int ERROR_AUTH_TOKEN = 0x5;
    public static final int ERROR_ADD_FRIEND = 0x6;
    public static final int ERROR_LOAD_USERS = 0x7;
    public static final int ERROR_LOAD_CHATS_AND_MESSAGES = 0x8;
    public static final int ERROR_LOAD_CHAT_MESSAGES = 0x9;
    public static final int ERROR_UPLOAD_PHOTO = 0xA;
    public static final int ERROR_GET_WALLET_KEY = 0xB;
    public static final int ERROR_SET_WALLET_KEY = 0xC;
    public static final int ERROR_REGISTER_LOGIN_OR_PASS_OR_EMAIL_EMPTY = 0xD;
    public static final int ERROR_REGISTER_INVALID_LOGIN = 0xE;
    public static final int ERROR_REGISTER_INVALID_EMAIL = 0xF;
    public static final int ERROR_REGISTER_USER_EXISTS = 0x10;
    public static final int ERROR_GROUP_CREATE = 0x11;
    public static final int ERROR_GROUP = 0x12;
    public static final int ERROR_SPAMMING = 0x13;
    public static final int ERROR_ETH_TO_FIAT = 0x14;
    public static final int ERROR_ETH_GET_BALANCE = 0x15;
    public static final int ERROR_ETH_GET_PUBLIC_KEY = 0x16;
    public static final int ERROR_ETH_GET_TX_INFO = 0x17;
    public static final int ERROR_ETH_SEND = 0x18;
    public static final int ERROR_ETH_CREATE_WALLET = 0x19;
    public static final int ERROR_RESTORE_PASSWORD_EMAIL_NOT_EXISTS = 0x1A;
    public static final int ERROR_RESTORE_PASSWORD_LOGIN_NOT_EXISTS = 0x1B;
    public static final int ERROR_RESTORE_PASSWORD_INVALID_CODE = 0x1C;

    /**
     * Чтобы генерировать svuid (Serial Version Unique ID), нужно зайти в File->Settings->Editor->Live Templates,
     * затем нажать '+' и добавить свой шаблон как на скриншоте: http://imgur.com/a/aUM1V
     * (при написании svuid будет автоматически вызываться этот шаблон)
     */

    public static class PM_json extends Packet {
        public static int svuid = 1363708171;
        public String text;

        public static PM_json PMdeserialize(SerializableData stream, int constructor, boolean exception) {
            if (PM_json.svuid != constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in PM_json", constructor));
                } else {
                    return null;
                }
            }
            PM_json result = new PM_json();
            result.readParams(stream, exception);
            return result;
        }

        public void readParams(SerializableData stream, boolean exception) {
            text = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(text);
        }
    }

    public static class PM_error extends Packet {
        public static int svuid = 384728714;
        public int code;
        public String text;

        public static PM_error PMdeserialize(SerializableData stream, int constructor, boolean exception) {
            if (PM_error.svuid != constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in PM_error", constructor));
                } else {
                    return null;
                }
            }
            PM_error result = new PM_error();
            result.readParams(stream, exception);
            return result;
        }

        public void readParams(SerializableData stream, boolean exception) {
            code = stream.readInt32(exception);
            text = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(code);
            stream.writeString(text);
        }
    }

    public static class PM_test extends Packet {
        public static int svuid = 0x55555555;
        public int val;

        public static PM_test PMdeserialize(SerializableData stream, int constructor, boolean exception) {
            if (PM_test.svuid != constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in PM_test", constructor));
                } else {
                    return null;
                }
            }
            PM_test result = new PM_test();
            result.readParams(stream, exception);
            return result;
        }

        public void readParams(SerializableData stream, boolean exception) {
            val = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(val);
        }
    }

    public static class PM_auth extends Packet {
        public static int svuid = 333030643;

        public int id;
        public String login;
        public String password;

        public static PM_auth PMdeserialize(SerializableData stream, int constructor, boolean exception) {
            if (PM_auth.svuid != constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in PM_auth", constructor));
                } else {
                    return null;
                }
            }
            PM_auth result = new PM_auth();
            result.readParams(stream, exception);
            return result;
        }

        public void readParams(SerializableData stream, boolean exception) {
            id = stream.readInt32(exception);
            login = stream.readString(exception);
            password = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(id);
            stream.writeString(login);
            stream.writeString(password);
        }
    }

    public static class PM_authToken extends Packet {
        public static int svuid = 359382942;

        public byte[] token;

        public void readParams(SerializableData stream, boolean exception) {
            token = stream.readByteArray(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeByteArray(token);
        }
    }

    public static class UserObject extends Packet implements Parcelable {
        public int id;
        public byte[] token;
        public String login;
        public String first_name;
        public String last_name;
        public String patronymic;
        public String email;
        public String country;
        public String city;
        public String birthdate;
        public long phoneNumber;
        public int gender;
        public String walletKey;
        //        public long access_hash;
        public long photoID;
        public boolean confirmed;
        public String inviteCode;

        public UserObject() {
        }

        public static UserObject deserialize(SerializableData stream, int constructor, boolean exception) {
            UserObject result = null;
            switch (constructor) {
                case PM_user.svuid:
                    result = new PM_user();
                    break;
                case PM_userFull.svuid:
                    result = new PM_userFull();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in UserObject", constructor));
            }
            if (result != null) {
                try {
                    result.readParams(stream, exception);
                } catch (Exception e) {
                    return null;
                }
            }
            return result;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeByteArray(this.token);
            dest.writeString(this.login);
            dest.writeString(this.first_name);
            dest.writeString(this.last_name);
            dest.writeString(this.patronymic);
            dest.writeString(this.email);
            dest.writeString(this.country);
            dest.writeString(this.city);
            dest.writeString(this.birthdate);
            dest.writeLong(this.phoneNumber);
            dest.writeInt(this.gender);
            dest.writeString(this.walletKey);
            dest.writeLong(this.photoID);
            dest.writeByte((byte) (this.confirmed ? 1 : 0));
            dest.writeString(this.inviteCode);
        }

        protected UserObject(Parcel in) {
            this.id = in.readInt();
            this.token = in.createByteArray();
            this.login = in.readString();
            this.first_name = in.readString();
            this.last_name = in.readString();
            this.patronymic = in.readString();
            this.email = in.readString();
            this.country = in.readString();
            this.city = in.readString();
            this.birthdate = in.readString();
            this.phoneNumber = in.readLong();
            this.gender = in.readInt();
            this.walletKey = in.readString();
            this.photoID = in.readLong();
            this.confirmed = in.readByte() == 1;
            this.inviteCode = in.readString();
        }

        public static final Creator<UserObject> CREATOR = new Creator<UserObject>() {
            @Override
            public UserObject createFromParcel(Parcel source) {
                return new UserObject(source);
            }

            @Override
            public UserObject[] newArray(int size) {
                return new UserObject[size];
            }
        };

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UserObject) {
                UserObject user = (UserObject) obj;
                return id == user.id;
            }
            return false;
        }
    }

    public static class PM_user extends UserObject {
        public static final int svuid = 143710769;

        public static PM_user deserialize(SerializableData stream, int constructor, boolean exception) {
            if (PM_user.svuid != constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in PM_user", constructor));
                } else {
                    return null;
                }
            }
            PM_user result = new PM_user();
            result.readParams(stream, exception);
            return result;
        }

        public void readParams(SerializableData stream, boolean exception) {
            id = stream.readInt32(exception);
            login = stream.readString(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            token = stream.readByteArray(exception);
            photoID = stream.readInt64(exception);
            walletKey = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(id);
            stream.writeString(login);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeByteArray(token);
            stream.writeInt64(photoID);
            stream.writeString(walletKey);
        }
    }

    public static class PM_userFull extends UserObject {
        public static final int svuid = 1692387515;

        public static PM_userFull deserialize(SerializableData stream, int constructor, boolean exception) {
            if (PM_userFull.svuid != constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in PM_userFull", constructor));
                } else {
                    return null;
                }
            }
            PM_userFull result = new PM_userFull();
            result.readParams(stream, exception);
            return result;
        }

        public void readParams(SerializableData stream, boolean exception) {
            id = stream.readInt32(exception);
            login = stream.readString(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            patronymic = stream.readString(exception);
            email = stream.readString(exception);
            country = stream.readString(exception);
            city = stream.readString(exception);
            birthdate = stream.readString(exception);
            phoneNumber = stream.readInt64(exception);
            gender = stream.readInt32(exception);
            token = stream.readByteArray(exception);
            photoID = stream.readInt64(exception);
            walletKey = stream.readString(exception);
            confirmed = stream.readBool(exception);
            inviteCode = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(id);
            stream.writeString(login);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeString(patronymic);
            stream.writeString(email);
            stream.writeString(country);
            stream.writeString(city);
            stream.writeString(birthdate);
            stream.writeInt64(phoneNumber);
            stream.writeInt32(gender);
            stream.writeByteArray(token);
            stream.writeInt64(photoID);
            stream.writeString(walletKey);
            stream.writeBool(confirmed);
            stream.writeString(inviteCode);
        }
    }

    public static class PM_requestPhoto extends Packet {
        public static int svuid = 1129923073;

        public int userID;
        public long id;

        public void readParams(SerializableData stream, boolean exception) {
            userID = stream.readInt32(exception);
            id = stream.readInt64(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(userID);
            stream.writeInt64(id);
        }
    }

    public static class Peer extends Packet {
        public int channel_id;
        public int user_id;
        public int group_id;

        public static Peer PMdeserialize(SerializableData stream, int constructor, boolean exception) {
            Peer result = null;
            switch (constructor) {
                case 1202091136:
                    result = new PM_peerChannel();
                    break;
                case 1226888699:
                    result = new PM_peerUser();
                    break;
                case 1778284232:
                    result = new PM_peerGroup();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in Peer", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Peer)) return false;
            Peer p = (Peer) obj;
            return channel_id == p.channel_id && user_id == p.user_id && group_id == p.group_id;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    public static class PM_peerChannel extends Peer {
        public static int svuid = 1202091136;

        public void readParams(SerializableData stream, boolean exception) {
            channel_id = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(channel_id);
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    public static class PM_peerUser extends Peer {
        public static int svuid = 1226888699;

        public PM_peerUser() {

        }

        public PM_peerUser(int uid) {
            this.user_id = uid;
        }

        public void readParams(SerializableData stream, boolean exception) {
            user_id = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(user_id);
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    public static class PM_peerGroup extends Peer {
        public static int svuid = 1778284232;

        public PM_peerGroup() {

        }

        public PM_peerGroup(int gid) {
            this.group_id = gid;
        }

        public void readParams(SerializableData stream, boolean exception) {
            group_id = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(group_id);
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    public static class PM_BTC_getWalletKey extends Packet {
        public static int svuid = 1553345733;

        public int uid;

        public void readParams(SerializableData stream, boolean exception) {
            uid = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(uid);
        }
    }

    /**
     * Using for setting local wallet key or for wallet key container
     */
    public static class PM_BTC_setWalletKey extends Packet {
        public static int svuid = 353327501;

        public String walletKey;

        public void readParams(SerializableData stream, boolean exception) {
            walletKey = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(walletKey);
        }
    }

    public static class PM_ETH_getWalletKey extends Packet {
        public static int svuid = 617421965;

        public int uid;

        public void readParams(SerializableData stream, boolean exception) {
            uid = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(uid);
        }
    }

    public static class PM_ETH_setWalletKey extends Packet {
        public static int svuid = 120591201;

        public String walletKey;

        public void readParams(SerializableData stream, boolean exception) {
            walletKey = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(walletKey);
        }
    }

    public static class PM_resendEmail extends Packet {
        public static int svuid = 682727075;

        public void readParams(SerializableData stream, boolean exception) {
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
        }
    }

    public static class Message extends Packet {
        public Message() {
        }

        public static int MESSAGE_FLAG_UNREAD = 0b1;
        public static int MESSAGE_FLAG_FROM_ID = 0b10;
        public static int MESSAGE_FLAG_REPLY = 0b100;
        public static int MESSAGE_FLAG_VIEWS = 0b1000;
        public static int MESSAGE_FLAG_EDITED = 0b10000;

        public long id;
        public int from_id;
        public Peer to_id;
        public int date;
        public int reply_to_msg_id;
        public String text;
        public int flags;
        public boolean unread;
        //        public ArrayList<MessageEntity> entities = new ArrayList<>();
        public int views;
        public int edit_date;
        public FileManager.FileType itemType;
        public long itemID;

        public static Message deserialize(SerializableData stream, int constructor, boolean exception) {
            Message result = null;
            switch (constructor) {
                case PM_message.svuid:
                    result = new PM_message();
                    break;
                case PM_messageItem.svuid:
                    result = new PM_messageItem();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in Message", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class Update extends Packet {
        public static Update deserialize(SerializableData stream, int constructor, boolean exception) {
            Update result = null;
            switch (constructor) {
                case PM_updateMessageID.svuid:
                    result = new PM_updateMessageID();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in Message", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class PM_updateMessageID extends Update {
        public static final int svuid = 1311594707;

        public long oldID;
        public long newID;

        public void readParams(SerializableData stream, boolean exception) {
            oldID = stream.readInt64(exception);
            newID = stream.readInt64(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt64(oldID);
            stream.writeInt64(newID);
        }
    }

    public static class PM_updatePhotoID extends Update {
        public static int svuid = 1917996696;

        public long oldID;
        public long newID;

        public void readParams(SerializableData stream, boolean exception) {
            oldID = stream.readInt64(exception);
            newID = stream.readInt64(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt64(oldID);
            stream.writeInt64(newID);
        }
    }

    public static class Group extends Packet {
        public static int svuid = 1150008731;

        public int flags;
        public int id;
        public int creatorID;
        public String title;
        //        public int date;
        public ArrayList<UserObject> users = new ArrayList<>();
        public PM_photo photo;

        public static Group PMdeserialize(SerializableData stream, int constructor, boolean exception) {
            if (Group.svuid != constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in Group", constructor));
                } else {
                    return null;
                }
            }
            Group result = new Group();
            result.readParams(stream, exception);
            return result;
        }

        public void readParams(SerializableData stream, boolean exception) {
            flags = stream.readInt32(exception);
            id = stream.readInt32(exception);
            creatorID = stream.readInt32(exception);
            title = stream.readString(exception);

            int magic = stream.readInt32(exception);
            if (magic != SVUID_ARRAY) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                UserObject object = UserObject.deserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                users.add(object);
            }

            magic = stream.readInt32(exception);
            if (magic != PM_photo.svuid) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong PM_photo magic, got %x", magic));
                }
                return;
            }
            photo = (PM_photo) MessageMedia.deserialize(stream, magic, exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(flags);
            stream.writeInt32(id);
            stream.writeInt32(creatorID);
            stream.writeString(title);
            writeArray(stream, users);
            photo.serializeToStream(stream);
        }
    }

    public static class PM_group_removeParticipant extends Packet {
        public static int svuid = 370161898;

        public int id;
        public int userID;

        public void readParams(SerializableData stream, boolean exception) {
            id = stream.readInt32(exception);
            userID = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(id);
            stream.writeInt32(userID);
        }
    }

    public static class PM_group_addParticipants extends Packet {
        public static int svuid = 1066060061;

        public int id;
        public ArrayList<Integer> userIDs = new ArrayList<>();

        public void readParams(SerializableData stream, boolean exception) {
            id = stream.readInt32(exception);
            int magic = stream.readInt32(exception);
            if (magic != SVUID_ARRAY) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                int uid = stream.readInt32(exception);
                if (exception) {
                    return;
                }
                userIDs.add(uid);
            }
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(id);
            stream.writeInt32(SVUID_ARRAY);
            int count = userIDs.size();
            stream.writeInt32(count);
            for (int i = 0; i < count; i++) {
                stream.writeInt32(userIDs.get(i));
            }
        }
    }

    public static class PM_group_setSettings extends Packet {
        public static int svuid = 903771455;

        public int id;
        public String title;

        public void readParams(SerializableData stream, boolean exception) {
            id = stream.readInt32(exception);
            title = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(id);
            stream.writeString(title);
        }
    }

    public static class PM_group_setPhoto extends Packet {
        public static int svuid = 446580011;

        public int id;
        public PM_photo photo;

        public void readParams(SerializableData stream, boolean exception) {
            id = stream.readInt32(exception);
            int magic = stream.readInt32(exception);
            if (magic != PM_photo.svuid) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong PM_photo magic, got %x", magic));
                }
                return;
            }
            photo = (PM_photo) MessageMedia.deserialize(stream, magic, exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(id);
            photo.serializeToStream(stream);
        }
    }

    private static void writeArray(SerializableData stream, List<? extends Packet> array) {
        stream.writeInt32(SVUID_ARRAY);
        int count = array.size();
        stream.writeInt32(count);
        for (int i = 0; i < count; i++) {
            array.get(i).serializeToStream(stream);
        }
    }

    public static class PM_chatsAndMessages extends Packet {
        public static int svuid = 720223855;

        public ArrayList<Message> messages = new ArrayList<>();
        public ArrayList<Group> groups = new ArrayList<>();
        public ArrayList<UserObject> users = new ArrayList<>();
        public int count;

        public void readParams(SerializableData stream, boolean exception) {
            int magic = stream.readInt32(exception);
            if (magic != SVUID_ARRAY) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                Message object = Message.deserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                messages.add(object);
            }
            magic = stream.readInt32(exception);
            if (magic != SVUID_ARRAY) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                Group object = Group.PMdeserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                groups.add(object);
            }
            magic = stream.readInt32(exception);
            if (magic != SVUID_ARRAY) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                UserObject object = UserObject.deserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                users.add(object);
            }
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            writeArray(stream, messages);
            writeArray(stream, groups);
            writeArray(stream, users);
        }
    }

    public static class PM_chat_messages extends Packet {
        public static int svuid = 929127698;

        public Peer chatID;
        //        public ArrayList<Message> messages = new ArrayList<>();
        public LinkedList<Message> messages = new LinkedList<>();

        public void readParams(SerializableData stream, boolean exception) {
            chatID = Peer.PMdeserialize(stream, stream.readInt32(exception), exception);

            int magic = stream.readInt32(exception);
            if (magic != SVUID_ARRAY) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                Message object = Message.deserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                messages.add(object);
            }
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            chatID.serializeToStream(stream);
            writeArray(stream, messages);
        }
    }

    public static class PM_getChatMessages extends Packet {
        public static int svuid = 966582132;

        public Peer chatID;
        public int count;
        public int offset;

        public void readParams(SerializableData stream, boolean exception) {
            chatID = Peer.PMdeserialize(stream, stream.readInt32(exception), exception);
            count = stream.readInt32(exception);
            offset = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            chatID.serializeToStream(stream);
            stream.writeInt32(count);
            stream.writeInt32(offset);
        }
    }

    public static class PM_keepAlive extends Packet {
        public static int svuid = 1264656564;

        public void readParams(SerializableData stream, boolean exception) {
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
        }
    }

    public static class PM_setProfilePhoto extends Packet {
        public static int svuid = 477263581;

        public PM_photo photo;

        public void readParams(SerializableData stream, boolean exception) {
            int magic = stream.readInt32(exception);
            if (magic != PM_photo.svuid) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong PM_photo magic, got %x", magic));
                }
                return;
            }
            photo = (PM_photo) MessageMedia.deserialize(stream, magic, exception);

        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            photo.serializeToStream(stream);
        }
    }

    public static class PM_message extends Message {
        public static final int svuid = 1683670506;

        public PM_message() {
        }

        public void readParams(SerializableData stream, boolean exception) {
            flags = stream.readInt32(exception);
            unread = (flags & MESSAGE_FLAG_UNREAD) != 0;
            id = stream.readInt64(exception);
            if ((flags & MESSAGE_FLAG_FROM_ID) != 0) {
                from_id = stream.readInt32(exception);
            }
            to_id = Peer.PMdeserialize(stream, stream.readInt32(exception), exception);
            if (from_id == 0) {
                if (to_id.user_id != 0) {
                    from_id = to_id.user_id;
                } else {
                    from_id = -to_id.channel_id;
                }
            }
            if ((flags & MESSAGE_FLAG_REPLY) != 0) {
                reply_to_msg_id = stream.readInt32(exception);
            }
            date = stream.readInt32(exception);
            text = stream.readString(exception);
//            if ((flags & 128) != 0) {
//                int magic = stream.readInt32(exception);
//                if (magic != 0x1cb5c415) {
//                    if (exception) {
//                        throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
//                    }
//                    return;
//                }
//                int count = stream.readInt32(exception);
//                for (int a = 0; a < count; a++) {
//                    MessageEntity object = MessageEntity.deserialize(stream, stream.readInt32(exception), exception);
//                    if (object == null) {
//                        return;
//                    }
//                    entities.add(object);
//                }
//            }
            if ((flags & MESSAGE_FLAG_VIEWS) != 0) {
                views = stream.readInt32(exception);
            }
            if ((flags & MESSAGE_FLAG_EDITED) != 0) {
                edit_date = stream.readInt32(exception);
            }
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            flags = unread ? (flags | MESSAGE_FLAG_UNREAD) : (flags & ~MESSAGE_FLAG_UNREAD);
            stream.writeInt32(flags);
            stream.writeInt64(id);

            if ((flags & MESSAGE_FLAG_FROM_ID) != 0) {
                stream.writeInt32(from_id);
            }
            to_id.serializeToStream(stream);

            if ((flags & MESSAGE_FLAG_REPLY) != 0) {
                stream.writeInt32(reply_to_msg_id);
            }
            stream.writeInt32(date);
            stream.writeString(text);
//            if ((flags & 128) != 0) {
//                stream.writeInt32(0x1cb5c415);
//                int count = entities.size();
//                stream.writeInt32(count);
//                for (int a = 0; a < count; a++) {
//                    entities.get(a).serializeToStream(stream);
//                }
//            }
            if ((flags & MESSAGE_FLAG_VIEWS) != 0) {
                stream.writeInt32(views);
            }
            if ((flags & MESSAGE_FLAG_EDITED) != 0) {
                stream.writeInt32(edit_date);
            }
        }
    }

    public static class PM_messageItem extends Message {
        public static final int svuid = 1874618975;

        public void readParams(SerializableData stream, boolean exception) {
            flags = stream.readInt32(exception);
            unread = (flags & MESSAGE_FLAG_UNREAD) != 0;
            id = stream.readInt64(exception);
            if ((flags & MESSAGE_FLAG_FROM_ID) != 0) {
                from_id = stream.readInt32(exception);
            }
            to_id = Peer.PMdeserialize(stream, stream.readInt32(exception), exception);
            if (from_id == 0) {
                if (to_id.user_id != 0) {
                    from_id = to_id.user_id;
                } else {
                    from_id = -to_id.channel_id;
                }
            }
            if ((flags & MESSAGE_FLAG_REPLY) != 0) {
                reply_to_msg_id = stream.readInt32(exception);
            }
            date = stream.readInt32(exception);

            itemType = FileManager.FileType.values()[stream.readInt32(exception)];
            itemID = stream.readInt64(exception);
            text = stream.readString(exception);

            if ((flags & MESSAGE_FLAG_VIEWS) != 0) {
                views = stream.readInt32(exception);
            }
            if ((flags & MESSAGE_FLAG_EDITED) != 0) {
                edit_date = stream.readInt32(exception);
            }
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            flags = unread ? (flags | MESSAGE_FLAG_UNREAD) : (flags & ~MESSAGE_FLAG_UNREAD);
            stream.writeInt32(flags);
            stream.writeInt64(id);

            if ((flags & MESSAGE_FLAG_FROM_ID) != 0) {
                stream.writeInt32(from_id);
            }
            to_id.serializeToStream(stream);

            if ((flags & MESSAGE_FLAG_REPLY) != 0) {
                stream.writeInt32(reply_to_msg_id);
            }
            stream.writeInt32(date);

            stream.writeInt32(itemType.ordinal());
            stream.writeInt64(itemID);
            stream.writeString(text);

            if ((flags & MESSAGE_FLAG_VIEWS) != 0) {
                stream.writeInt32(views);
            }
            if ((flags & MESSAGE_FLAG_EDITED) != 0) {
                stream.writeInt32(edit_date);
            }
        }
    }

    public static class PM_register extends Packet {
        public static int svuid = 540920824;

        public String login;
        public String password;
        public String firstName;
        public String lastName;
        public String patronymic;
        public String email;
        public long phone;
        public String country;
        public String city;
        public String birthdayDate;
        public int gender;
        public String walletKey;
        public byte[] walletBytes;
        public String inviteCode;

        public void readParams(SerializableData stream, boolean exception) {
            login = stream.readString(exception);
            password = stream.readString(exception);
            firstName = stream.readString(exception);
            lastName = stream.readString(exception);
            patronymic = stream.readString(exception);
            email = stream.readString(exception);
            phone = stream.readInt64(exception);
            country = stream.readString(exception);
            city = stream.readString(exception);
            birthdayDate = stream.readString(exception);
            gender = stream.readInt32(exception);
            walletKey = stream.readString(exception);
            walletBytes = stream.readByteArray(exception);
            inviteCode = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(login);
            stream.writeString(password);
            stream.writeString(firstName);
            stream.writeString(lastName);
            stream.writeString(patronymic);
            stream.writeString(email);
            stream.writeInt64(phone);
            stream.writeString(country);
            stream.writeString(city);
            stream.writeString(birthdayDate);
            stream.writeInt32(gender);
            stream.writeString(walletKey);
            stream.writeByteArray(walletBytes);
            stream.writeString(inviteCode);
        }
    }

    public static class PM_searchContact extends Packet {
        public static int svuid = 1904015974;
        public String query;

        public void readParams(SerializableData stream, boolean exception) {
            query = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(query);
        }
    }

    public static class PM_requestDHParams extends Packet {
        public static int svuid = 1452151203;

        public void readParams(SerializableData stream, boolean exception) {
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
        }
    }

    public static class PM_DHParams extends Packet {
        public static int svuid = 790923294;
        public byte[] p;
        public byte[] g;

        public void readParams(SerializableData stream, boolean exception) {
            p = stream.readByteArray(exception);
            g = stream.readByteArray(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeByteArray(p);
            stream.writeByteArray(g);
        }
    }

    public static class PM_clientDHdata extends Packet {
        public static int svuid = 1636198261;

        public byte[] key;

        public void readParams(SerializableData stream, boolean exception) {
            key = stream.readByteArray(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeByteArray(key);
        }
    }

    public static class PM_serverDHdata extends Packet {
        public static int svuid = 1874402433;

        public byte[] key;
        public long keyID;

        public void readParams(SerializableData stream, boolean exception) {
            key = stream.readByteArray(exception);
            keyID = stream.readInt64(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeByteArray(key);
            stream.writeInt64(keyID);
        }
    }

    public static class PM_DHresult extends Packet {
        public static int svuid = 472276209;

        public boolean ok;

        public void readParams(SerializableData stream, boolean exception) {
            ok = stream.readBool(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeBool(ok);
        }
    }

    public static class PM_postConnectionData extends Packet {
        public static int svuid = 1577010971;

        //        public long keyID;
        public long salt;

        public void readParams(SerializableData stream, boolean exception) {
//            keyID = stream.readInt64(exception);
            salt = stream.readInt64(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
//            stream.writeInt64(keyID);
            stream.writeInt64(salt);
        }
    }

    public static class PM_users extends Packet {
        public static int svuid = 509261291;
        public ArrayList<UserObject> users = new ArrayList<>();

        public void readParams(SerializableData stream, boolean exception) {
            int magic = stream.readInt32(exception);
            if (magic != SVUID_ARRAY) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                UserObject object = UserObject.deserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                users.add(object);
            }
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            writeArray(stream, users);
        }
    }

    public static class PM_addFriend extends Packet {
        public static int svuid = 1407346220;
        public int uid;

        public void readParams(SerializableData stream, boolean exception) {
            uid = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(uid);
        }
    }

    public static class PM_file extends Packet {
        public static int svuid = 673680214;

        public long id;
        public int partsCount;
        public int totalSize;
        public FileManager.FileType type;
        public String name;
//        public String md5_checksum;

        public void readParams(SerializableData stream, boolean exception) {
            id = stream.readInt64(exception);
            partsCount = stream.readInt32(exception);
            totalSize = stream.readInt32(exception);
            type = FileManager.FileType.values()[stream.readInt32(exception)];
            name = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt64(id);
            stream.writeInt32(partsCount);
            stream.writeInt32(totalSize);
            stream.writeInt32(type.ordinal());
            stream.writeString(name);
        }
    }

    public static class PM_filePart extends Packet {
        public static int svuid = 22502919;

        public long fileID;
        public int part;
        //        public SerializedBuffer bytes;
        public byte[] bytes;

        public Packet deserializeResponse(SerializableData stream, int svuid, boolean exception) {
            return Bool.PMdeserialize(stream, svuid, exception);
        }

        public void readParams(SerializableData stream, boolean exception) {
            fileID = stream.readInt64(exception);
            part = stream.readInt32(exception);
            bytes = stream.readByteArray(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt64(fileID);
            stream.writeInt32(part);
            stream.writeByteArray(bytes);
        }

        @Override
        public void freeResources() {
            if (disableFree) {
                return;
            }
//            if (bytes != null) {
//                bytes.reuse();
//                bytes = null;
//            }
        }
    }

    public static class PM_messages_getChats extends Packet {
        public static int constructor = 0x6b47f94d;

        public int offset_date;
        public int offset_id;
        //        public InputPeer offset_peer;
        public int limit;

//        public Packet deserializeResponse(SerializableData stream, int constructor, boolean exception) {
//            return messages_Dialogs.deserialize(stream, constructor, exception);
//        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(offset_date);
            stream.writeInt32(offset_id);
//            offset_peer.serializeToStream(stream);
            stream.writeInt32(limit);
        }
    }

    public static class PM_photo extends MessageMedia {
        public static final int svuid = 1935780422;

        public long id;
        public long access_hash;
        public int user_id;
        public int date;
        public String caption;
        //        public GeoPoint geo;
//        public ArrayList<PhotoSize> sizes = new ArrayList<>();
        public int flags;

        public PM_photo() {

        }

        public PM_photo(int uid, long id) {
            this.id = id;
            this.user_id = uid;
        }

        public void readParams(SerializableData stream, boolean exception) {
//            flags = stream.readInt32(exception);
            user_id = stream.readInt32(exception);
            id = stream.readInt64(exception);
//            bytes = stream.readByteArray(exception);
//            if (stream.readInt32(exception) == PM_file.svuid) {
//                file = new PM_file();
//                file.readParams(stream, exception);
//            } else {
//                Log.e(Config.TAG, "error while reading PM_file");
//            }
//            access_hash = stream.readInt64(exception);
//            date = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
//            stream.writeInt32(flags);
            stream.writeInt32(user_id);
            stream.writeInt64(id);
//            file.serializeToStream(stream);
//            stream.writeByteArray(bytes);
//            stream.writeInt64(access_hash);
//            stream.writeInt32(date);
        }

        @Override
        public String toString() {
            return "{PHOTO " + user_id + "_" + id + "}";
        }
    }

    public static class PM_getStickerPack extends Packet {
        public static int svuid = 1913111419;

        public int id;

        public void readParams(SerializableData stream, boolean exception) {
            id = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(id);
        }
    }

    public static class PM_stickerPack extends Packet {
        public static int svuid = 66975105;

        public int id;
        public int count;
        public String title;
        public String author;
        public ArrayList<PM_sticker> stickers = new ArrayList<>();

        public void readParams(SerializableData stream, boolean exception) {
            id = stream.readInt32(exception);
            count = stream.readInt32(exception);
            title = stream.readString(exception);
            author = stream.readString(exception);

            int magic = stream.readInt32(exception);
            if (magic != SVUID_ARRAY) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                PM_sticker object = PM_sticker.deserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                stickers.add(object);
            }
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(id);
            stream.writeInt32(count);
            stream.writeString(title);
            stream.writeString(author);
            writeArray(stream, stickers);
        }
    }

    public static class PM_sticker extends MessageMedia {
        public static int svuid = 603143999;

        public long id;

        public void readParams(SerializableData stream, boolean exception) {
            id = stream.readInt64(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt64(id);
        }

        public static PM_sticker deserialize(SerializableData stream, int constructor, boolean exception) {
            if (PM_sticker.svuid != constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in PM_sticker", constructor));
                } else {
                    return null;
                }
            }
            PM_sticker result = new PM_sticker();
            result.readParams(stream, exception);
            return result;
        }
    }

    public static class MessageMedia extends Packet {
        //        public byte[] bytes;
        public PM_file file;
//        public Audio audio_unused;
//        public PM_photo photo;
//        public String title;
//        public String caption;

        public static MessageMedia deserialize(SerializableData stream, int constructor, boolean exception) {
            MessageMedia result = null;
            switch (constructor) {
                case PM_photo.svuid:
                    result = new PM_photo();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in MessageMedia", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);

            }
            return result;
        }
    }

    public static class Bool extends Packet {

        public static Bool PMdeserialize(SerializableData stream, int constructor, boolean exception) {
            Bool result = null;
            switch (constructor) {
                case SVUID_TRUE:
                    result = new PM_boolTrue();
                    break;
                case SVUID_FALSE:
                    result = new PM_boolFalse();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in Bool", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class PM_boolTrue extends Bool {
        public static int svuid = SVUID_TRUE;

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
        }
    }

    public static class PM_boolFalse extends Bool {
        public static int svuid = SVUID_FALSE;

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
        }
    }

    public static class PM_createGroup extends Packet {
        public static int svuid = 179109192;

        public String title;
        public ArrayList<Integer> userIDs = new ArrayList<>();

        public void readParams(SerializableData stream, boolean exception) {
            title = stream.readString(exception);

            int magic = stream.readInt32(exception);
            if (magic != SVUID_ARRAY) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                int uid = stream.readInt32(exception);
                if (exception) {
                    return;
                }
                userIDs.add(uid);
            }
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(title);
            stream.writeInt32(SVUID_ARRAY);
            int count = userIDs.size();
            stream.writeInt32(count);
            for (int i = 0; i < count; i++) {
                stream.writeInt32(userIDs.get(i));
            }
        }
    }

    public static class PM_ETH_getTxInfo extends Packet {
        public static int svuid = 2121624423;

        public String hash;

        public void readParams(SerializableData stream, boolean exception) {
            hash = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(hash);
        }
    }

    public static class PM_ETH_txInfo extends Packet {
        public static int svuid = 514255593;

        public String hash;
        public String from;
        public String to;
        long time;
        public String amount;
        public String fee;

        public void readParams(SerializableData stream, boolean exception) {
            hash = stream.readString(exception);
            from = stream.readString(exception);
            to = stream.readString(exception);
            time = stream.readInt64(exception);
            amount = stream.readString(exception);
            fee = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(hash);
            stream.writeString(from);
            stream.writeString(to);
            stream.writeInt64(time);
            stream.writeString(amount);
            stream.writeString(fee);
        }
    }

    public static class PM_ETH_toFiat extends Packet {
        public static int svuid = 285620428;

        public String prefix;
        public String privateKey;

        public void readParams(SerializableData stream, boolean exception) {
            prefix = stream.readString(exception);
            privateKey = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(prefix);
            stream.writeString(privateKey);
        }
    }

    public static class PM_ETH_fiatInfo extends Packet {
        public static int svuid = 1437700991;

        public String amount;

        public void readParams(SerializableData stream, boolean exception) {
            amount = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(amount);
        }
    }

    public static class PM_ETH_send extends Packet {
        public static int svuid = 1278997128;

        public String amount;
        public String senderPrivateKey;
        public String receiverPrivateKey;
        public String message;

        public void readParams(SerializableData stream, boolean exception) {
            amount = stream.readString(exception);
            senderPrivateKey = stream.readString(exception);
            receiverPrivateKey = stream.readString(exception);
            message = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(amount);
            stream.writeString(senderPrivateKey);
            stream.writeString(receiverPrivateKey);
            stream.writeString(message);
        }
    }

    public static class PM_ETH_sendInfo extends Packet {
        public static int svuid = 108044474;

        public String hash;

        public void readParams(SerializableData stream, boolean exception) {
            hash = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(hash);
        }
    }

    public static class PM_ETH_getBalance extends Packet {
        public static int svuid = 399562569;

        public String privateKey;

        public void readParams(SerializableData stream, boolean exception) {
            privateKey = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(privateKey);
        }
    }

    public static class PM_ETH_balanceInfo extends Packet {
        public static int svuid = 1261840240;

        public String amount;

        public void readParams(SerializableData stream, boolean exception) {
            amount = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(amount);
        }
    }

    public static class PM_ETH_createWallet extends Packet {
        public static int svuid = 2113639233;

        public void readParams(SerializableData stream, boolean exception) {
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
        }
    }

    public static class PM_ETH_walletInfo extends Packet {
        public static int svuid = 522641238;

        public String walletKey;
        public String privateKey;

        public void readParams(SerializableData stream, boolean exception) {
            walletKey = stream.readString(exception);
            privateKey = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(walletKey);
            stream.writeString(privateKey);
        }
    }

    public static class PM_ETH_getPublicFromPrivate extends Packet {
        public static int svuid = 1267745470;

        public String privateKey;

        public void readParams(SerializableData stream, boolean exception) {
            privateKey = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(privateKey);
        }
    }

    public static class PM_ETH_publicFromPrivateInfo extends Packet {
        public static int svuid = 276868702;

        public String publicKey;

        public void readParams(SerializableData stream, boolean exception) {
            publicKey = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(publicKey);
        }
    }

    public static class PM_postReferal extends Packet {
        public static int svuid = 581538205;

        public String code;

        public void readParams(SerializableData stream, boolean exception) {
            code = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(code);
        }
    }

    public static class PM_deleteChat extends Packet {
        public static int svuid = 523847389;

        public Peer peer;

        public void readParams(SerializableData stream, boolean exception) {
            peer = Peer.PMdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            peer.serializeToStream(stream);
        }
    }

    public static class PM_deleteDialogMessages extends Packet {
        public static int svuid = 357613531;

        public ArrayList<Long> messageIDs = new ArrayList<>();

        public void readParams(SerializableData stream, boolean exception) {
            int magic = stream.readInt32(exception);
            if (magic != SVUID_ARRAY) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                long mid = stream.readInt64(exception);
                if (exception) {
                    return;
                }
                messageIDs.add(mid);
            }
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(SVUID_ARRAY);
            int count = messageIDs.size();
            stream.writeInt32(count);
            for (int i = 0; i < count; i++) {
                stream.writeInt64(messageIDs.get(i));
            }
        }
    }

    public static class PM_deleteGroupMessages extends Packet {
        public static int svuid = 943281791;

        public ArrayList<Long> messageIDs = new ArrayList<>();

        public void readParams(SerializableData stream, boolean exception) {
            int magic = stream.readInt32(exception);
            if (magic != SVUID_ARRAY) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int i = 0; i < count; i++) {
                long mid = stream.readInt64(exception);
                if (exception) {
                    return;
                }
                messageIDs.add(mid);
            }
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(SVUID_ARRAY);
            int count = messageIDs.size();
            stream.writeInt32(count);
            for (int i = 0; i < count; i++) {
                stream.writeInt64(messageIDs.get(i));
            }
        }
    }

    public static class PM_getUserInfo extends Packet {
        public static int svuid = 543732124;

        public int user_id;

        public void readParams(SerializableData stream, boolean exception) {
            user_id = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(user_id);
        }
    }

    public static class PM_getInvitedUsersCount extends Packet {
        public static int svuid = 953284719;

        public void readParams(SerializableData stream, boolean exception) {
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
        }
    }

    public static class PM_invitedUsersCount extends Packet {
        public static int svuid = 421747118;

        public int count;

        public void readParams(SerializableData stream, boolean exception) {
            count = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(count);
        }
    }

    public static class PM_restorePassword extends Packet {
        public static int svuid = 23582811;

        public int code;
        public String password;
        public String login;

        public void readParams(SerializableData stream, boolean exception) {
            code = stream.readInt32(exception);
            password = stream.readString(exception);
            login = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeInt32(code);
            stream.writeString(password);
            stream.writeString(login);
        }
    }

    public static class PM_restorePasswordRequestCode extends Packet {
        public static int svuid = 564783058;

        public String login;

        public void readParams(SerializableData stream, boolean exception) {
            login = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(login);
        }
    }

    public static class PM_verifyPasswordRecoveryCode extends Packet {
        public static int svuid = 569433023;

        public String login;
        public int code;

        public void readParams(SerializableData stream, boolean exception) {
            login = stream.readString(exception);
            code = stream.readInt32(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(login);
            stream.writeInt32(code);
        }
    }

    public static class PM_checkEmailConfirmation extends Packet {
        public static int svuid = 512435272;

        public String login;
        public String newEmail;

        public void readParams(SerializableData stream, boolean exception) {
            login = stream.readString(exception);
            newEmail = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(login);
            stream.writeString(newEmail);
        }
    }

    public static class PM_getUsersByPhoneNumber extends Packet {//TODO: в ответ приходит PM_users
        public static int svuid = 517290247;

        public String phones;

        public void readParams(SerializableData stream, boolean exception) {
            phones = stream.readString(exception);
        }

        public void serializeToStream(SerializableData stream) {
            stream.writeInt32(svuid);
            stream.writeString(phones);
        }
    }
}
