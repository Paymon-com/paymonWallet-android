//
// Created by negi on 23.04.17.
//

#include "ClassStore.h"
#include "RPC.h"

Packet *ClassStore::PMdeserialize(SerializedBuffer *stream, uint32_t bytes, uint32_t constructor, bool &error) {
    Packet *object = nullptr;
//    DEBUG_D("Deserializing svuid %d size %d", svuid, bytes);
    switch (constructor) {
        case PM_auth::constructor:
            object = new PM_auth();
            break;
        case PM_authToken::constructor:
            object = new PM_authToken();
            break;
        case PM_json::constructor:
            object = new PM_json();
            break;
        case PM_message::constructor:
            object = new PM_message();
            break;
        case PM_test::constructor:
            object = new PM_test();
            break;
        case PM_keepAlive::constructor:
            object = new PM_keepAlive();
            break;
        case PM_chat_messages::constructor:
            object = new PM_chat_messages();
            break;
        case PM_chats_and_messages::constructor:
            object = new PM_chats_and_messages();
            break;
        case Group::constructor:
            object = new Group();
            break;
        case PM_register::constructor:
            object = new PM_register();
            break;
        case PM_searchContact::constructor:
            object = new PM_searchContact();
            break;
        case PM_users::constructor:
            object = new PM_users();
            break;
        case PM_addFriend::constructor:
            object = new PM_addFriend();
            break;
        case PM_DHParams::constructor:
            object = new PM_DHParams();
            break;
        case PM_requestDHParams::constructor:
            object = new PM_requestDHParams();
            break;
        case PM_serverDHdata::constructor:
            object = new PM_serverDHdata();
            break;
        case PM_clientDHdata::constructor:
            object = new PM_clientDHdata();
            break;
        case PM_DHresult::constructor:
            object = new PM_DHresult();
            break;
        case PM_postConnectionData::constructor:
            object = new PM_postConnectionData();
            break;
        case PM_updateMessageID::svuid:
            object = new PM_updateMessageID();
            break;
        case PM_photo::svuid:
            object = new PM_photo();
            break;
        case PM_requestPhoto::svuid:
            object = new PM_requestPhoto();
            break;
        case PM_updatePhotoID::svuid:
            object = new PM_updatePhotoID();
            break;
        case PM_file::svuid:
            object = new PM_file();
            break;
        case PM_filePart::svuid:
            object = new PM_filePart();
            break;
        case PM_boolTrue::svuid:
            object = new PM_boolTrue();
            break;
        case PM_boolFalse::svuid:
            object = new PM_boolFalse();
            break;
        default:
            return nullptr;
    }
    object->readParams(stream, error);
    return object;
}
