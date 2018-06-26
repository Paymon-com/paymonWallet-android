//
// Created by negi on 16.04.17.
//

#ifndef PAYMONSERVEREPOLL_RPC_H
#define PAYMONSERVEREPOLL_RPC_H


#include "SerializedBuffer.h"
#include "Packet.h"
#include "Utils.h"
#include "ByteArray.h"

class RPC {
public:
    static const uint SVUID_ARRAY = 1550732454;
    static const uint SVUID_TRUE = 1606318489;
    static const uint SVUID_FALSE = 1541450798;

    static const int ERROR_INTERNAL = 0x1;
    static const int ERROR_KEY = 0x2;
    static const int ERROR_REGISTER = 0x3;
    static const int ERROR_AUTH = 0x4;
    static const int ERROR_AUTH_TOKEN = 0x5;
    static const int ERROR_ADD_FRIEND = 0x6;
    static const int ERROR_LOAD_USERS = 0x7;
    static const int ERROR_LOAD_CHATS_AND_MESSAGES = 0x8;
    static const int ERROR_LOAD_CHAT_MESSAGES = 0x9;
    static const int ERROR_UPLOAD_PHOTO = 0xA;
};

class Bool : public Packet {

public:
    static Bool *TLdeserialize(SerializedBuffer *stream, uint32_t constructor, bool &error);
};

class PM_boolTrue : public Bool {

public:
    static const uint svuid = RPC::SVUID_TRUE;

    void serializeToStream(SerializedBuffer *stream);
};

class PM_boolFalse : public Bool {

public:
    static const uint32_t svuid = RPC::SVUID_FALSE;

    void serializeToStream(SerializedBuffer *stream);
};

class PM_json : public Packet {

public:
    static const uint constructor = 1363708171;

    std::string text;

    static PM_json *TLdeserialize(SerializedBuffer *stream, uint constructor, bool &error);

    void readParams(SerializedBuffer *stream, bool &error);

    void serializeToStream(SerializedBuffer *stream);
};

class PM_error : public Packet {

public:
    static const uint constructor = 384728714;

    int code;
    std::string text;

    static PM_error *TLdeserialize(SerializedBuffer *stream, uint constructor, bool &error);

    void readParams(SerializedBuffer *stream, bool &error);

    void serializeToStream(SerializedBuffer *stream);
};

class PM_user : public Packet {

public:
    static const uint constructor = 143710769;

    int id;
    std::string login;
    std::string first_name;
    std::string last_name;
    std::string walletKey;
    std::unique_ptr<ByteArray> token;
    int64 photoID;

    static PM_user *TLdeserialize(SerializedBuffer *stream, uint constructor, bool &error);

    void readParams(SerializedBuffer *stream, bool &error);

    void serializeToStream(SerializedBuffer *stream);
};

class PM_test : public Packet {

public:
    static const uint constructor = 0x55555555;

    int32 val;

    static PM_test *TLdeserialize(SerializedBuffer *stream, uint constructor, bool &error);
    void readParams(SerializedBuffer *stream, bool &error);
    void serializeToStream(SerializedBuffer *stream);
};

class PM_keepAlive : public Packet {

public:
    static const uint constructor = 1264656564;

    void readParams(SerializedBuffer *stream, bool &error);
    void serializeToStream(SerializedBuffer *stream);
};

class PM_auth : public Packet {

public:
    static const uint constructor = 333030643;

    int id;
    std::string login;
    std::string password;

    static PM_auth *TLdeserialize(SerializedBuffer *stream, uint constructor, bool &error);
    void readParams(SerializedBuffer *stream, bool &error);
    void serializeToStream(SerializedBuffer *stream);
};

class PM_authToken : public Packet {

public:
    static const uint constructor = 359382942;

    std::unique_ptr<ByteArray> token;

    void readParams(SerializedBuffer *stream, bool &error) {
        token = std::unique_ptr<ByteArray>(stream->readByteArray(&error));
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
        stream->writeByteArray(token.get());
    }
};

class PM_requestDHParams : public Packet {

public:
    static const uint constructor = 1452151203;

    void readParams(SerializedBuffer *stream, bool &error) {
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
    }
};

class PM_DHParams : public Packet {

public:
    static const uint constructor = 790923294;

    std::unique_ptr<ByteArray> p;
    std::unique_ptr<ByteArray> g;

    void readParams(SerializedBuffer *stream, bool &error) {
        p = std::unique_ptr<ByteArray>(stream->readByteArray(&error));
        g = std::unique_ptr<ByteArray>(stream->readByteArray(&error));
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
        stream->writeByteArray(p.get());
        stream->writeByteArray(g.get());
    }
};

class PM_serverDHdata : public Packet {

public:
    static const uint constructor = 1874402433;

    std::unique_ptr<ByteArray> key;
    int64 keyID;

    void readParams(SerializedBuffer *stream, bool &error) {
        key = std::unique_ptr<ByteArray>(stream->readByteArray(&error));
        keyID = stream->readInt64(&error);
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
        stream->writeByteArray(key.get());
        stream->writeInt64(keyID);
    }
};

class PM_DHresult : public Packet {

public:
    static const uint constructor = 472276209;

    bool ok;

    void readParams(SerializedBuffer *stream, bool &error) {
        ok = stream->readBool(&error);
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
        stream->writeBool(ok);
    }
};

class PM_postConnectionData : public Packet {
public:
    static const uint constructor = 1577010971;

    // int64 keyID;
    int64 salt;

    void readParams(SerializedBuffer *stream, bool &error) {
        // keyID = stream->readInt64(&error);
        salt = stream->readInt64(&error);
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
        // stream->writeInt64(keyID);
        stream->writeInt64(salt);
    }
};

class PM_clientDHdata : public Packet {

public:
    static const uint constructor = 1636198261;

    std::unique_ptr<ByteArray> key;

    void readParams(SerializedBuffer *stream, bool &error) {
        key = std::unique_ptr<ByteArray>(stream->readByteArray(&error));
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
        stream->writeByteArray(key.get());
    }
};

class Peer : public Packet {
public:
    int channel_id;
    int user_id;
    int group_id;

    static Peer *deserialize(SerializedBuffer *stream, uint constructor, bool &error);
};

class PM_peerChannel : public Peer {

public:
    static const uint constructor = 1202091136;

    void readParams(SerializedBuffer *stream, bool &error);
    void serializeToStream(SerializedBuffer *stream);
};

class PM_peerUser : public Peer {

public:
    static const uint constructor = 1226888699;

    void readParams(SerializedBuffer *stream, bool &error);
    void serializeToStream(SerializedBuffer *stream);
};

class PM_peerGroup : public Peer {

public:
    static const uint constructor = 1778284232;

    void readParams(SerializedBuffer *stream, bool &error);
    void serializeToStream(SerializedBuffer *stream);
};

class Message : public Packet {
public:
    static const int MESSAGE_FLAG_UNREAD = 0b1;
    static const int MESSAGE_FLAG_FROM_ID = 0b10;
    static const int MESSAGE_FLAG_REPLY = 0b100;
    static const int MESSAGE_FLAG_VIEWS = 0b1000;
    static const int MESSAGE_FLAG_EDITED = 0b10000;

    int64 id;
    int from_id;
    Peer* to_id;
    int date;
    int reply_to_msg_id;
    std::string text;
    int flags;
    bool unread;
    int views;
    int edit_date;

    static Message *TLdeserialize(SerializedBuffer *stream, uint constructor, bool &error);

    virtual ~Message();
};

class Group : public Packet {

public:
    static const uint constructor = 1150008731;

    int flags;
    int id;
    std::string title;
    int date;

    static Group *TLdeserialize(SerializedBuffer *stream, uint constructor, bool &error) {
        if (Group::constructor != constructor) {
            error = true;
            DEBUG_E("can't parse magic %x in Group", constructor);
            return nullptr;
        }
        Group *result = new Group();
        result->readParams(stream, error);
        return result;
    }
    void readParams(SerializedBuffer *stream, bool &error) {
        flags = stream->readInt32(&error);
        id = stream->readInt32(&error);
        title = stream->readString(&error);
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
        stream->writeInt32(flags);
        stream->writeInt32(id);
        stream->writeString(title);
    }
};

class PM_chats_and_messages : public Packet {

public:
    static const uint constructor = 720223855;

    std::vector<std::unique_ptr<Message>> messages;
    std::vector<std::unique_ptr<Group>> groups;
    std::vector<std::unique_ptr<PM_user>> users;
    int count;

    void readParams(SerializedBuffer *stream, bool &error) {
        uint magic = stream->readUint32(&error);
        if (magic != RPC::SVUID_ARRAY) {
            error = true;
            DEBUG_E("wrong Vector magic, got %x", magic);
            return;
        }
        uint count = stream->readUint32(&error);
        for (uint i = 0; i < count; i++) {
            Message *object = new Message();
            object->readParams(stream, error);
            if (error) {
                return;
            }
            messages.push_back(std::unique_ptr<Message>(object));
        }

        magic = stream->readUint32(&error);
        if (magic != RPC::SVUID_ARRAY) {
            error = true;
            DEBUG_E("wrong Vector magic, got %x", magic);
            return;
        }
        count = stream->readUint32(&error);
        for (uint i = 0; i < count; i++) {
            Group *object = new Group();
            object->readParams(stream, error);
            if (error) {
                return;
            }
            groups.push_back(std::unique_ptr<Group>(object));
        }

        magic = stream->readUint32(&error);
        if (magic != RPC::SVUID_ARRAY) {
            error = true;
            DEBUG_E("wrong Vector magic, got %x", magic);
            return;
        }
        count = stream->readUint32(&error);
        for (uint i = 0; i < count; i++) {
            PM_user *object = new PM_user();
            object->readParams(stream, error);
            if (error) {
                return;
            }
            users.push_back(std::unique_ptr<PM_user>(object));
        }
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);

        stream->writeInt32(RPC::SVUID_ARRAY);
        uint count = (uint) messages.size();
        stream->writeInt32(count);
        for (uint i = 0; i < count; i++) {
            messages[i]->serializeToStream(stream);
        }

        stream->writeInt32(RPC::SVUID_ARRAY);
        count = (uint) groups.size();
        stream->writeInt32(count);
        for (uint i = 0; i < count; i++) {
            groups[i]->serializeToStream(stream);
        }

        stream->writeInt32(RPC::SVUID_ARRAY);
        count = (uint) users.size();
        stream->writeInt32(count);
        for (uint i = 0; i < count; i++) {
            users[i]->serializeToStream(stream);
        }
    }
};

class PM_chat_messages : public Packet {

public:
    static const uint constructor = 929127698;

    std::unique_ptr<Peer> chatID;
    std::vector<std::unique_ptr<Message>> messages;

    void readParams(SerializedBuffer *stream, bool &error) {
        chatID = std::unique_ptr<Peer>(Peer::deserialize(stream, stream->readUint32(&error), error));
        if (chatID == nullptr) {
            DEBUG_E("Error parse chatID in PM_chat_messages");
            return;
        }
        uint magic = stream->readUint32(&error);
        if (magic != RPC::SVUID_ARRAY) {
            error = true;
            DEBUG_E("wrong Vector magic, got %x", magic);
            return;
        }
        uint count = stream->readUint32(&error);
        for (uint i = 0; i < count; i++) {
            Message *object = new Message();
            object->readParams(stream, error);
            if (error) {
                return;
            }
            messages.push_back(std::unique_ptr<Message>(object));
        }
    }

    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
        chatID->serializeToStream(stream);
        stream->writeInt32(RPC::SVUID_ARRAY);
        uint count = (uint) messages.size();
        stream->writeInt32(count);
        for (uint i = 0; i < count; i++) {
            messages[i]->serializeToStream(stream);
        }
    }
};

class PM_message : public Message {

public:
    static const uint constructor = 1683670506;

    void readParams(SerializedBuffer *stream, bool &error);
    void serializeToStream(SerializedBuffer *stream);
};

class PM_register : public Packet {

public:
    static const uint constructor = 540920824;

    std::string login;
    std::string password;
    std::string firstName;
    std::string lastName;
    std::string patronymic;
    int64       phone;
    std::string country;
    std::string city;
    std::string birthdayDate;
    std::string walletKey;
    std::unique_ptr<ByteArray> walletBytes;

    int gender;

    void readParams(SerializedBuffer *stream, bool &error) {
        login = stream->readString(&error);
        password = stream->readString(&error);
        firstName = stream->readString(&error);
        lastName = stream->readString(&error);
        patronymic = stream->readString(&error);
        phone = stream->readInt64(&error);
        country = stream->readString(&error);
        city = stream->readString(&error);
        birthdayDate = stream->readString(&error);
        gender = stream->readInt32(&error);
        walletKey = stream->readString(&error);
        walletBytes = std::unique_ptr<ByteArray>(stream->readByteArray(&error));
    }

    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
        stream->writeString(login);
        stream->writeString(password);
        stream->writeString(firstName);
        stream->writeString(lastName);
        stream->writeString(patronymic);
        stream->writeInt64(phone);
        stream->writeString(country);
        stream->writeString(city);
        stream->writeString(birthdayDate);
        stream->writeInt32(gender);
        stream->writeString(walletKey);
        stream->writeByteArray(walletBytes.get());
    }
};

class PM_searchContact : public Packet {

public:
    static const uint constructor = 1904015974;

    std::string query;

    void readParams(SerializedBuffer *stream, bool &error) {
        query = stream->readString(&error);
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
        stream->writeString(query);
    }
};

class PM_addFriend : public Packet {

public:
    static const uint constructor = 1407346220;

    int uid;

    void readParams(SerializedBuffer *stream, bool &error) {
        uid = stream->readInt32(&error);
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
        stream->writeInt32(uid);
    }
};

class PM_users : public Packet  {

public:
    static const uint constructor = 509261291;

    std::vector<std::unique_ptr<PM_user>> users;

    void readParams(SerializedBuffer *stream, bool &error) {
        uint magic = stream->readUint32(&error);
        if (magic != RPC::SVUID_ARRAY) {
            error = true;
            DEBUG_E("wrong Vector magic, got %x", magic);
            return;
        }
        uint count = stream->readUint32(&error);
        for (uint i = 0; i < count; i++) {
            PM_user *object = new PM_user();
            object->readParams(stream, error);
            if (error) {
                return;
            }
            users.push_back(std::unique_ptr<PM_user>(object));
        }
    }

    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(constructor);
        stream->writeInt32(RPC::SVUID_ARRAY);
        uint count = (uint) users.size();
        stream->writeInt32(count);
        for (uint i = 0; i < count; i++) {
            users[i]->serializeToStream(stream);
        }
    }
};

class PM_file : public Packet {

public:
    static const uint svuid = 673680214;

    int64 id;
    int partsCount;
    int totalSize;
    std::string name;
//    std::unique_ptr<ByteArray> bytes;

    static PM_file *deserialize(SerializedBuffer *stream, uint constructor, bool &error);

    void readParams(SerializedBuffer *stream, bool &error) {
        id = stream->readInt64(&error);
        partsCount = stream->readInt32(&error);
        totalSize = stream->readInt32(&error);
        name = stream->readString(&error);
    }

    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(svuid);
        stream->writeInt64(id);
        stream->writeInt32(partsCount);
        stream->writeInt32(totalSize);
        stream->writeString(name);
    }
};

class PM_filePart : public Packet {

public:
    static const uint svuid = 22502919;

    int64 fileID;
    int part;
    std::unique_ptr<ByteArray> bytes;

    static Packet *PMdeserializeResponse(SerializedBuffer *stream, uint32_t constructor, bool &error);

    void readParams(SerializedBuffer *stream, bool &error) {
        bytes = std::unique_ptr<ByteArray>(stream->readByteArray(&error));
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(svuid);
        stream->writeInt64(fileID);
        stream->writeInt32(part);
        stream->writeByteArray(bytes.get());
    }
};

class MessageMedia : public Packet {

public:
    std::unique_ptr<PM_file> file;

    static MessageMedia *deserialize(SerializedBuffer *stream, uint32_t constructor, bool &error);

};

class PM_photo : public MessageMedia {

public:
    static const uint svuid = 1935780422;

    int64 id;
    int userID;

    void readParams(SerializedBuffer *stream, bool &error) {
        userID = stream->readInt32(&error);
        id = stream->readInt64(&error);
//        DEBUG_D("reading");
        file = std::unique_ptr<PM_file>(PM_file::deserialize(stream, stream->readUint32(&error), error));
//        bytes = std::unique_ptr<ByteArray>(stream->readByteArray(&error));
    }

    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(svuid);
        stream->writeInt32(userID);
        stream->writeInt64(id);
//        stream->writeByteArray(bytes.get());
        file->serializeToStream(stream);
    }
};

class Update : public Packet {

public:
    std::unique_ptr<ByteArray> bytes;

    Update *deserialize(SerializedBuffer *stream, uint32_t constructor, bool &error);

};

class PM_updateMessageID : public Update {

public:
    static const uint svuid = 1311594707;

    int64 oldID;
    int64 newID;

    void readParams(SerializedBuffer *stream, bool &error) {
        oldID = stream->readInt64(&error);
        newID = stream->readInt64(&error);
    }

    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(svuid);
        stream->writeInt64(oldID);
        stream->writeInt64(newID);
    }
};

class PM_updatePhotoID : public Update {

public:
    static const uint svuid = 1917996696;

    int64 oldID;
    int64 newID;

    void readParams(SerializedBuffer *stream, bool &error) {
        oldID = stream->readInt64(&error);
        newID = stream->readInt64(&error);
    }

    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(svuid);
        stream->writeInt64(oldID);
        stream->writeInt64(newID);
    }
};

class PM_requestPhoto : public Packet {

public:
    static const uint svuid = 1129923073;

    int userID;
    int64 id;

    void readParams(SerializedBuffer *stream, bool &error) {
        userID = stream->readInt32(&error);
        id = stream->readInt64(&error);
    }

    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(svuid);
        stream->writeInt32(userID);
        stream->writeInt64(id);
    }
};

class PM_setProfilePhoto : public Packet {

public:
    static const uint svuid = 477263581;

    std::unique_ptr<PM_photo> photo;

    void readParams(SerializedBuffer *stream, bool &error) {
        photo = std::unique_ptr<PM_photo>(dynamic_cast<PM_photo*> (MessageMedia::deserialize(stream, stream->readUint32(&error), error)));
    }
    void serializeToStream(SerializedBuffer *stream) {
        stream->writeInt32(svuid);
        photo->serializeToStream(stream);
    }
};

#endif //PAYMONSERVEREPOLL_RPC_H
