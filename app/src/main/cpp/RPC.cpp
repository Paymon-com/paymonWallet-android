//
// Created by negi on 16.04.17.
//

#include "RPC.h"

Peer *Peer::deserialize(SerializedBuffer *stream, uint32_t constructor, bool &error) {
    Peer *result = nullptr;
    switch(constructor) {
        case PM_peerChannel::constructor:
            result = new PM_peerChannel();
            break;
        case PM_peerUser::constructor:
            result = new PM_peerUser();
            break;
        case PM_peerGroup::constructor:
            result = new PM_peerGroup();
            break;
        default:
            error = true;
            DEBUG_E("can't parse magic %x in Peer", constructor);
            return nullptr;
    }
    result->readParams(stream, error);
    return result;
}

void PM_peerChannel::readParams(SerializedBuffer *stream, bool &error) {
    channel_id = stream->readInt32(&error);
}

void PM_peerChannel::serializeToStream(SerializedBuffer *stream) {
    stream->writeInt32(constructor);
    stream->writeInt32(channel_id);
}

void PM_peerUser::readParams(SerializedBuffer *stream, bool &error) {
    user_id = stream->readInt32(&error);
}

void PM_peerUser::serializeToStream(SerializedBuffer *stream) {
    stream->writeInt32(constructor);
    stream->writeInt32(user_id);
}

void PM_peerGroup::readParams(SerializedBuffer *stream, bool &error) {
    group_id = stream->readInt32(&error);
}

void PM_peerGroup::serializeToStream(SerializedBuffer *stream) {
    stream->writeInt32(constructor);
    stream->writeInt32(group_id);
}

Message *Message::TLdeserialize(SerializedBuffer *stream, uint32_t constructor, bool &error) {
    Message *result = nullptr;
    switch(constructor) {
        case 1683670506:
            result = new PM_message();
            break;
        default:
            error = true;
            DEBUG_E("can't parse magic %x in Message", constructor);
            return nullptr;
    }
    result->readParams(stream, error);
    return result;
}

Message::~Message() {
}

void PM_message::readParams(SerializedBuffer *stream, bool &error) {
    flags = stream->readInt32(&error);
    unread = (flags & MESSAGE_FLAG_UNREAD) != 0;

    id = stream->readInt64(&error);
    if ((flags & MESSAGE_FLAG_FROM_ID) != 0) {
        from_id = stream->readInt32(&error);
    }

    to_id = Peer::deserialize(stream, stream->readUint32(&error), error);
    if (from_id == 0) {
        if (to_id->user_id != 0) {
            from_id = to_id->user_id;
        } else {
            from_id = -to_id->channel_id;
        }
    }
    if ((flags & MESSAGE_FLAG_REPLY) != 0) {
        reply_to_msg_id = stream->readInt32(&error);
    }
    date = stream->readInt32(&error);
    text = stream->readString(&error);
    if ((flags & MESSAGE_FLAG_VIEWS) != 0) {
        views = stream->readInt32(&error);
    }
    if ((flags & MESSAGE_FLAG_EDITED) != 0) {
        edit_date = stream->readInt32(&error);
    }
}

void PM_message::serializeToStream(SerializedBuffer *stream) {
    stream->writeInt32(constructor);
    flags = unread ? (flags | MESSAGE_FLAG_UNREAD) : (flags &~ MESSAGE_FLAG_UNREAD);

    stream->writeInt32(flags);
    stream->writeInt64(id);

    if ((flags & MESSAGE_FLAG_FROM_ID) != 0) {
        stream->writeInt32(from_id);
    }

    if (to_id != nullptr) {
        to_id->serializeToStream(stream);
    } else {
        stream->writeInt32(PM_peerUser::constructor);
        stream->writeInt32(0);
    }

    if ((flags & MESSAGE_FLAG_REPLY) != 0) {
        stream->writeInt32(reply_to_msg_id);
    }
    stream->writeInt32(date);
    stream->writeString(text);

    if ((flags & MESSAGE_FLAG_VIEWS) != 0) {
        stream->writeInt32(views);
    }

    if ((flags & MESSAGE_FLAG_EDITED) != 0) {
        stream->writeInt32(edit_date);
    }
}

MessageMedia *MessageMedia::deserialize(SerializedBuffer *stream, uint32_t constructor, bool &error) {
    MessageMedia *result = nullptr;
    switch(constructor) {
        case PM_photo::svuid:
            result = new PM_photo();
            break;
        default:
            error = true;
            DEBUG_E("can't parse magic %x in MessageMedia", constructor);
            return nullptr;
    }
    result->readParams(stream, error);
    return result;
}

Update *Update::deserialize(SerializedBuffer *stream, uint32_t constructor, bool &error) {
    Update *result = nullptr;
    switch(constructor) {
        case PM_updateMessageID::svuid:
            result = new PM_updateMessageID();
            break;
        default:
            error = true;
            DEBUG_E("can't parse magic %x in Update", constructor);
            return nullptr;
    }
    result->readParams(stream, error);
    return result;
}

Bool *Bool::TLdeserialize(SerializedBuffer *stream, uint32_t constructor, bool &error) {
    Bool *result = nullptr;
    switch (constructor) {
        case RPC::SVUID_TRUE:
            result = new PM_boolTrue();
            break;
        case RPC::SVUID_FALSE:
            result = new PM_boolFalse();
            break;
        default:
            error = true;
            DEBUG_E("can't parse magic %x in Bool", constructor);
            return nullptr;
    }
    result->readParams(stream, error);
    return result;
}

PM_file *PM_file::deserialize(SerializedBuffer *stream, uint constructor, bool &error) {
    if (PM_file::svuid != constructor) {
        error = true;
        DEBUG_E("can't parse magic %x in PM_file", constructor);
        return nullptr;
    }
    PM_file *result = new PM_file();
    result->readParams(stream, error);
    return result;
}

void PM_boolTrue::serializeToStream(SerializedBuffer *stream) {
    stream->writeInt32(svuid);
}

void PM_boolFalse::serializeToStream(SerializedBuffer *stream) {
    stream->writeInt32(svuid);
}

PM_json *PM_json::TLdeserialize(SerializedBuffer *stream, uint constructor, bool &error) {
    if (PM_json::constructor != constructor) {
        error = true;
        DEBUG_E("can't parse magic %x in PM_json", constructor);
        return nullptr;
    }
    PM_json *result = new PM_json();
    result->readParams(stream, error);
    return result;
}

void PM_json::readParams(SerializedBuffer *stream, bool &error) {
    text = stream->readString(&error);
}

void PM_json::serializeToStream(SerializedBuffer *stream) {
    stream->writeInt32(constructor);
    stream->writeString(text);
}

PM_error *PM_error::TLdeserialize(SerializedBuffer *stream, uint constructor, bool &error) {
    if (PM_error::constructor != constructor) {
        error = true;
        DEBUG_E("can't parse magic %x in PM_error", constructor);
        return nullptr;
    }
    PM_error *result = new PM_error();
    result->readParams(stream, error);
    return result;
}

void PM_error::readParams(SerializedBuffer *stream, bool &error) {
    code = stream->readInt32(&error);
    text = stream->readString(&error);
}

void PM_error::serializeToStream(SerializedBuffer *stream) {
    stream->writeInt32(constructor);
    stream->writeInt32(code);
    stream->writeString(text);
}

PM_user *PM_user::TLdeserialize(SerializedBuffer *stream, uint constructor, bool &error) {
    if (PM_user::constructor != constructor) {
        error = true;
        DEBUG_E("can't parse magic %x in PM_user", constructor);
        return nullptr;
    }
    PM_user *result = new PM_user();
    result->readParams(stream, error);
    return result;
}

void PM_user::readParams(SerializedBuffer *stream, bool &error) {
    id = stream->readInt32(&error);
    login = stream->readString(&error);
    first_name = stream->readString(&error);
    last_name = stream->readString(&error);
    token = std::unique_ptr<ByteArray>(stream->readByteArray(&error));
    photoID = stream->readInt64(&error);
    walletKey = stream->readString(&error);
}

void PM_user::serializeToStream(SerializedBuffer *stream) {
    stream->writeInt32(constructor);
    stream->writeInt32(id);
    stream->writeString(login);
    stream->writeString(first_name);
    stream->writeString(last_name);
    stream->writeByteArray(token.get());
    stream->writeInt64(photoID);
    stream->writeString(walletKey);
}

PM_test *PM_test::TLdeserialize(SerializedBuffer *stream, uint constructor, bool &error) {
    if (PM_test::constructor != constructor) {
        error = true;
        DEBUG_E("can't parse magic %x in PM_test", constructor);
        return nullptr;
    }
    PM_test *result = new PM_test();
    result->readParams(stream, error);
    return result;
}

void PM_test::readParams(SerializedBuffer *stream, bool &error) {
    val = stream->readInt32(&error);
}

void PM_test::serializeToStream(SerializedBuffer *stream) {
    stream->writeInt32(constructor);
    stream->writeInt32(val);
}

void PM_keepAlive::readParams(SerializedBuffer *stream, bool &error) {
}

void PM_keepAlive::serializeToStream(SerializedBuffer *stream) {
    stream->writeInt32(constructor);
}

PM_auth *PM_auth::TLdeserialize(SerializedBuffer *stream, uint constructor, bool &error) {
    if (PM_auth::constructor != constructor) {
        error = true;
        DEBUG_E("can't parse magic %x in PM_test", constructor);
        return nullptr;
    }
    PM_auth *result = new PM_auth();
    result->readParams(stream, error);
    return result;
}

void PM_auth::readParams(SerializedBuffer *stream, bool &error) {
    id = stream->readInt32(&error);
    login = stream->readString(&error);
    password = stream->readString(&error);
}

void PM_auth::serializeToStream(SerializedBuffer *stream) {
    stream->writeInt32(constructor);
    stream->writeInt32(id);
    stream->writeString(login);
    stream->writeString(password);
}

Packet *PM_filePart::PMdeserializeResponse(SerializedBuffer *stream, uint32_t constructor, bool &error) {
    return Bool::TLdeserialize(stream, constructor, error);
}
