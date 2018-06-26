#include <jni.h>
#include <time.h>
#include <cstdlib>
#include <openssl/sha.h>
#include <openssl/rand.h>
#include "FileLog.h"
#include "KeyGenerator.h"
#include "Packet.h"
#include "NetworkManager.h"

KeyGenerator& KeyGenerator::getInstance() {
    static KeyGenerator instance;
    return instance;
}

KeyGenerator::~KeyGenerator() {

}

int KeyGenerator::wrapData(JNIEnv *env, jlong &messageID, jbyteArray &authKey, jlong &authKeyID, jint &bufferAddress) {
    SerializedBuffer *request = (SerializedBuffer *) bufferAddress;

    int addr = 0;

    if (authKeyID != 0) {
//        DEBUG_D("keyID=%lld", (long long) authKeyID);
        byte *key = (byte*) env->GetByteArrayElements(authKey, (jboolean*) false);
        byte* buffer;
        addr = aesIGE(messageID, key, authKeyID, request, buffer);
        //((SerializedBuffer*) addr)->reuse();
        env->ReleaseByteArrayElements(authKey, (jbyte*) key, JNI_ABORT);
    } else {
        uint32_t messageLength = request->capacity();

        SerializedBuffer *buffer = BuffersStorage::getInstance().getFreeBuffer(8 + 8 + 4 + messageLength);
        buffer->position(0);
        buffer->writeInt64(0);
        buffer->writeInt64(messageID);
        buffer->writeInt32(messageLength);
        buffer->writeBytes(request);
        //buffer->reuse();
        addr = (int) (&*buffer);
    }

    if (addr != 0) {
        return addr;
    } else {
        DEBUG_E("Error wrapping packet data");
        return NULL;
    }
}

jboolean KeyGenerator::decryptMessage(JNIEnv *env, jlong &messageID, jbyteArray &authKey, jlong &authKeyID, jint &bufferAddress, jint &length, jint &mark) {
    if (authKey == nullptr) {
        return (jboolean) false;
    }
    SerializedBuffer *data = (SerializedBuffer *) bufferAddress;
    byte *ak = (byte*) env->GetByteArrayElements(authKey, (jboolean*) false);

    bool b = decryptAESIGE(ak, data->bytes() + mark + 8, data->bytes() + mark + 24, (uint32_t) length);
    env->ReleaseByteArrayElements(authKey, (jbyte*) ak, JNI_ABORT);
    return (jboolean) b;
}

inline void KeyGenerator::generateMessageKey(uint8_t *authKey, uint8_t *messageKey, uint8_t *result, bool incoming) {
    uint32_t x = incoming ? 8 : 0;

    static uint8_t sha[68];

    memcpy(sha + 20, messageKey, 16);
    memcpy(sha + 20 + 16, authKey + x, 32);
    SHA1(sha + 20, 48, sha);
    memcpy(result, sha, 8);
    memcpy(result + 32, sha + 8, 12);

    memcpy(sha + 20, authKey + 32 + x, 16);
    memcpy(sha + 20 + 16, messageKey, 16);
    memcpy(sha + 20 + 16 + 16, authKey + 48 + x, 16);
    SHA1(sha + 20, 48, sha);
    memcpy(result + 8, sha + 8, 12);
    memcpy(result + 32 + 12, sha, 8);

    memcpy(sha + 20, authKey + 64 + x, 32);
    memcpy(sha + 20 + 32, messageKey, 16);
    SHA1(sha + 20, 48, sha);
    memcpy(result + 8 + 12, sha + 4, 12);
    memcpy(result + 32 + 12 + 8, sha + 16, 4);

    memcpy(sha + 20, messageKey, 16);
    memcpy(sha + 20 + 16, authKey + 96 + x, 32);
    SHA1(sha + 20, 48, sha);
    memcpy(result + 32 + 12 + 8 + 4, sha, 8);
}

void KeyGenerator::aesIgeEncryption(uint8_t *buffer, uint8_t *key, uint8_t *iv, bool encrypt, bool changeIv, uint32_t length) {
    uint8_t *ivBytes = iv;
    if (!changeIv) {
        ivBytes = new uint8_t[32];
        memcpy(ivBytes, iv, 32);
    }
    AES_KEY akey;
    if (!encrypt) {
        AES_set_decrypt_key(key, 32 * 8, &akey);
        AES_ige_encrypt(buffer, buffer, length, &akey, ivBytes, AES_DECRYPT);
    } else {
        AES_set_encrypt_key(key, 32 * 8, &akey);
        AES_ige_encrypt(buffer, buffer, length, &akey, ivBytes, AES_ENCRYPT);
    }
    if (!changeIv) {
        delete [] ivBytes;
    }
}


int KeyGenerator::aesIGE(int64_t messageID, uint8_t* authKey, int64_t authKeyID, SerializedBuffer *sb, byte *&buff) {
    uint32_t messageSize = sb->capacity();//(int) msg.size();
    uint32_t additionalSize = (12 + messageSize) % 16;
    if (additionalSize != 0) {
        additionalSize = 16 - additionalSize;
    }

    SerializedBuffer *buffer = BuffersStorage::getInstance().getFreeBuffer(24 + 12 + messageSize + additionalSize);
    buffer->writeInt64(authKeyID);
    buffer->position(24);
    buffer->writeInt64(messageID);
    buffer->writeInt32(messageSize);
    buffer->writeBytes(sb);

    if (additionalSize != 0) {
        RAND_bytes(buffer->bytes() + 24 + 12 + messageSize, additionalSize);
    }

    static uint8_t messageKey[84];

    SHA1(buffer->bytes() + 24, 12 + messageSize, messageKey);
    memcpy(buffer->bytes() + 8, messageKey + 4, 16);
    generateMessageKey(authKey, messageKey + 4, messageKey + 20, false);
    aesIgeEncryption(buffer->bytes() + 24, messageKey + 20, messageKey + 52, true, false, buffer->limit() - 24);//4 + messageSize + additionalSize);

    buff = buffer->bytes();
    int addr = (int) (&*buffer);
    return addr;
}

bool KeyGenerator::decryptAESIGE(uint8_t *authKey, uint8_t *key, uint8_t *data, uint32_t length) {
    if (length % 16 != 0) {
        std::cout << "length % 16 != 0" << std::endl;
        return false;
    }
    static uint8_t messageKey[84];
    generateMessageKey(authKey, key, messageKey + 20, false);
    aesIgeEncryption(data, messageKey + 20, messageKey + 52, false, false, length);

    uint32_t messageLength;
    memcpy(&messageLength, data + 8, sizeof(uint32_t));
    if (messageLength > length - 12) {
        DEBUG_E("messageLength > length - 12 <> %u > %u - 12", messageLength, length);
        return false;
    }
    messageLength += 12;
    if (messageLength > length) {
        messageLength = length;
    }

    SHA1(data, messageLength, messageKey);
    return memcmp(messageKey + 4, key, 16) == 0;
}

