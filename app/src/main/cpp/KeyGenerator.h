//
// Created by negi on 18.05.17.
//

#ifndef PAYMON_KEYGENERATOR_H
#define PAYMON_KEYGENERATOR_H

#include <jni.h>
#include "SerializedBuffer.h"
#include "Defines.h"

extern jclass jclass_ByteBuffer;
extern jmethodID jclass_ByteBuffer_allocateDirect;
extern JavaVM *jvm;
extern SerializedBuffer* restOfTheData;
extern uint lastPacketLength;

class KeyGenerator {
public:
    static KeyGenerator &getInstance();

    ~KeyGenerator();

    int wrapData(JNIEnv *env, jlong &messageID, jbyteArray &authKey, jlong &authKeyID, jint &bufferAddress);
    jboolean decryptMessage(JNIEnv *env, jlong &messageID, jbyteArray &authKey, jlong &authKeyID,
                   jint &bufferAddress, jint &length, jint &mark);

private:

    inline void
    generateMessageKey(uint8_t *authKey, uint8_t *messageKey, uint8_t *result, bool incoming);

    int aesIGE(int64_t messageID, uint8_t *authKey, int64_t authKeyID, SerializedBuffer *sb,
               byte *&buff);

    void aesIgeEncryption(uint8_t *buffer, uint8_t *key, uint8_t *iv, bool encrypt, bool changeIv,
                          uint32_t length);

    bool decryptAESIGE(uint8_t *authKey, uint8_t *key, uint8_t *data, uint32_t length);
};
#endif //PAYMON_KEYGENERATOR_H
