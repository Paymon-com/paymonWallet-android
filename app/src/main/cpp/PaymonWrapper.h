//
// Created by negi on 16.06.17.
//

#ifndef PAYMON_PAYMONWRAPPER_H
#define PAYMON_PAYMONWRAPPER_H

#include <android/log.h>

extern inline int registerNativeMethods(JNIEnv *env, const char *className, JNINativeMethod *methods, int methodsCount);
//int initNativeFunctions(JavaVM *vm, JNIEnv *env);
//extern jint JNI_OnLoad(JavaVM *vm, void *reserved);
extern jint native_wrapData(JNIEnv *env, jclass c, jlong messageID, jbyteArray authKey, jlong authKeyID, jint bufferAddress);
jboolean native_decryptMessage(JNIEnv *env, jclass c, jlong messageID, jbyteArray authKey, jlong authKeyID, jint bufferAddress, jint length, jint mark);
extern jint native_init(JNIEnv *env, jclass c, jstring host, jshort port, jint version);
extern jint getFreeBuffer(JNIEnv *env, jclass c, jint length);
extern jint limit(JNIEnv *env, jclass c, jint address);
extern jint position(JNIEnv *env, jclass c, jint address);
extern void reuse(JNIEnv *env, jclass c, jint address);
extern jobject getJavaByteBuffer(JNIEnv *env, jclass c, jint address);
extern void native_sendData(JNIEnv *env, jclass c, jint address, jint pos, jint limit);
extern JNIEXPORT void JNICALL native_connect(JNIEnv *env, jclass c);
extern void native_reconnect(JNIEnv *env, jclass c);

class androidbuf : public std::streambuf {
public:
    enum { bufsize = 256 };
    androidbuf() { this->setp(buffer, buffer + bufsize - 1); }

private:
    int overflow(int c)
    {
        if (c == traits_type::eof()) {
            *this->pptr() = traits_type::to_char_type(c);
            this->sbumpc();
        }
        return this->sync()? traits_type::eof(): traits_type::not_eof(c);
    }

    int sync()
    {
        int rc = 0;
        if (this->pbase() != this->pptr()) {
            char writebuf[bufsize+1];
            memcpy(writebuf, this->pbase(), this->pptr() - this->pbase());
            writebuf[this->pptr() - this->pbase()] = '\0';

            rc = __android_log_write(ANDROID_LOG_INFO, "std", writebuf) > 0;
            this->setp(buffer, buffer + bufsize - 1);
        }
        return rc;
    }

    char buffer[bufsize];
};

extern JavaVM *jvm;
extern JNIEnv *jniEnv;
jclass jclass_ByteBuffer = nullptr;
jmethodID jclass_ByteBuffer_allocateDirect = 0;
uint lastPacketLength = 0;
//SerializedBuffer *restOfTheData = nullptr;

#define  LOG_TAG    "paymon-jni-dbg"
#define  LOG_D(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

jclass jclass_NetworkManager;
jmethodID jclass_NetworkManager_onConnectionDataReceived;
jmethodID jclass_NetworkManager_onConnectionStateChanged;

#endif //PAYMON_PAYMONWRAPPER_H
