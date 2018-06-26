//
// Created by negi on 16.06.17.
//

#include <jni.h>
#include <cstdlib>
#include <time.h>
#include <iostream>
#include "PaymonWrapper.h"
#include "FileLog.h"
#include "NetworkManager.h"

JavaVM *jvm = nullptr;
JNIEnv *jniEnv = nullptr;

static const char *SerializedBufferClassPathName ="ru/paymon/android/utils/SerializedBuffer";
static JNINativeMethod SerializedBufferMethods[] = {
        {"native_getFreeBuffer", "(I)I", (void *) getFreeBuffer},
        {"native_limit", "(I)I", (void *) limit},
        {"native_position", "(I)I", (void *) position},
        {"native_reuse", "(I)V", (void *) reuse},
        {"native_getJavaByteBuffer", "(I)Ljava/nio/ByteBuffer;", (void *) getJavaByteBuffer}
};

static const char *ApplicationLoaderClassPathName = "ru/paymon/android/ApplicationLoader";
static JNINativeMethod ApplicationLoaderMethods[] = {
        {"native_init", "(Ljava/lang/String;SI)I", (void *) native_init}
};

static const char *KeyGeneratorClassPathName = "ru/paymon/android/utils/KeyGenerator";
static JNINativeMethod KeyGeneratorMethods[] = {
        {"wrapData", "(J[BJI)I", (void *) native_wrapData},
//        {"wrapDataToSend", "(I)I", (void *) native_wrapDataToSend},
        {"decryptMessage", "(J[BJIII)Z", (void *) native_decryptMessage},
};

static const char *NetworkManagerClassPathName = "ru/paymon/android/net/NetworkManager";
static JNINativeMethod NetworkManagerMethods[] = {
        {"native_sendData", "(III)V", (void *) native_sendData},
        {"native_connect", "()V", (void *) native_connect},
        {"native_reconnect", "()V", (void *) native_reconnect}
};

class Delegate : public ConnectiosManagerDelegate {
    void onUpdate() {
//        jniEnv->CallStaticVoidMethod(jclass_ConnectionsManager, jclass_ConnectionsManager_onUpdate);
    }
    void onSessionCreated() {
//        jniEnv->CallStaticVoidMethod(jclass_ConnectionsManager, jclass_ConnectionsManager_onSessionCreated);
    }
    void onConnectionStateChanged(ConnectionState state) {
        errno = 0;
//        jniEnv->CallVoidMethodd(jclass_NetworkManager, jclass_NetworkManager_onConnectionStateChanged, state);

        pthread_t self = pthread_self();
        DEBUG_D("TID %ld", self);

//        jclass localRefCls = jniEnv->FindClass(NetworkManagerClassPathName);
//        if (localRefCls == NULL) {
//            jthrowable exc = jniEnv->ExceptionOccurred();
//            if(exc) {
//                jniEnv->ExceptionDescribe();
//                jniEnv->ExceptionClear();
//            }
//            return;
//        }
//
//        //cache the EyeSightCore ref as global
//        /* Create a global reference */
//        jclass_NetworkManager = (_jclass*)jniEnv->NewGlobalRef(localRefCls);
//
//        /* The local reference is no longer useful */
//        jniEnv->DeleteLocalRef(localRefCls);

        if (jclass_NetworkManager == NULL) {
            DEBUG_E("jclass_NetworkManager == NULL");
//            jthrowable exc = jniEnv->ExceptionOccurred();
//            if(exc) {
//                jniEnv->ExceptionDescribe();
//                jniEnv->ExceptionClear();
//            }
            return;
        }

//        jclass_NetworkManager = (jclass) jniEnv->NewGlobalRef(jniEnv->FindClass(NetworkManagerClassPathName));
//        if (jclass_NetworkManager == 0) {
//            DEBUG_E("jclass_NetworkManager == 0");
//            DEBUG_E("ERRNO: %s (%d)", strerror(errno), errno);
//            return;
//        }
//        jclass_NetworkManager_onConnectionDataReceived = jniEnv->GetStaticMethodID(jclass_NetworkManager, "onConnectionDataReceived", "(II)V");
//        if (jclass_NetworkManager_onConnectionDataReceived == 0) {
//            DEBUG_E("jclass_NetworkManager_onConnectionDataReceived == 0");
//            DEBUG_E("ERRNO: %s (%d)", strerror(errno), errno);
//            return;
//        }
//        jclass_NetworkManager_onConnectionStateChanged = jniEnv->GetStaticMethodID(jclass_NetworkManager, "onConnectionStateChanged", "(I)V");
//        if (jclass_NetworkManager_onConnectionStateChanged == 0) {
//            DEBUG_E("jclass_NetworkManager_onConnectionStateChanged == 0");
//            DEBUG_E("ERRNO: %s (%d)", strerror(errno), errno);
//            return;
//        }
//        DEBUG_D("CallStaticVoidMethod %p %p", jclass_NetworkManager, jclass_NetworkManager_onConnectionStateChanged);
        JNIEnv *env;
        if (javaVm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
            DEBUG_E("!!!JNI_VERSION_1_6 not found");
        }

        jboolean isException = env->ExceptionCheck();
        DEBUG_D("EXCEPTION == %d", isException != 0);
        if (isException) {
            jthrowable exc = env->ExceptionOccurred();
            if(exc) {
                env->ExceptionDescribe();
                env->ExceptionClear();
            }
        } else {
            env->CallStaticVoidMethod(jclass_NetworkManager, jclass_NetworkManager_onConnectionStateChanged, (jint) state);
        }
        DEBUG_E("ERRNO: %s (%d)", strerror(errno), errno);
    }
    void onUnparsedMessageReceived(int64_t reqMessageId, SerializedBuffer *buffer, ConnectionType connectionType) {
//        if (connectionType == ConnectionTypeGeneric) {
//            jniEnv->CallStaticVoidMethod(jclass_ConnectionsManager, jclass_ConnectionsManager_onUnparsedMessageReceived, buffer);
//        }

    }
    void onLogout() {
//        jniEnv->CallStaticVoidMethod(jclass_ConnectionsManager, jclass_ConnectionsManager_onLogout);
    }

//    void onUpdateConfig(TL_config *config) {
//        NativeByteBuffer *buffer = BuffersStorage::getInstance().getFreeBuffer(config->getObjectSize());
//        config->serializeToStream(buffer);
//        buffer->position(0);
//        jniEnv->CallStaticVoidMethod(jclass_ConnectionsManager, jclass_ConnectionsManager_onUpdateConfig, buffer);
//        buffer->reuse();
//    }
    void onInternalPushReceived() {
//        jniEnv->CallStaticVoidMethod(jclass_ConnectionsManager, jclass_ConnectionsManager_onInternalPushReceived);
    }
    void onConnectionDataReceived(SerializedBuffer *buffer, uint length) {
        jniEnv->CallStaticVoidMethod(jclass_NetworkManager, jclass_NetworkManager_onConnectionDataReceived, buffer, (jint) length);
    }
};

jint native_init(JNIEnv *env, jclass c, jstring host, jshort port, jint version) {
    DEBUG_D("Native INIT");
    NetworkManager::getInstance().useJavaVM(jvm, false);
    NetworkManager::getInstance().setDelegate(new Delegate());
    std::string hostStr = env->GetStringUTFChars(host, (jboolean*) false);
    NetworkManager::getInstance().init(hostStr, (uint16) port, (uint) version, 0, 0, "Nexus 5", "Android 6.0", "0.1a", "RU", "", "", 1, true, false);

    return 0;
}

jint getFreeBuffer(JNIEnv *env, jclass c, jint length) {
    return (jint) BuffersStorage::getInstance().getFreeBuffer(length);
}

jint limit(JNIEnv *env, jclass c, jint address) {
    SerializedBuffer *buffer = (SerializedBuffer *) address;
    return buffer->limit();
}

jint position(JNIEnv *env, jclass c, jint address) {
    SerializedBuffer *buffer = (SerializedBuffer *) address;
    return buffer->position();
}

void reuse(JNIEnv *env, jclass c, jint address) {
    SerializedBuffer *buffer = (SerializedBuffer *) address;
    buffer->reuse();
}

jobject getJavaByteBuffer(JNIEnv *env, jclass c, jint address) {
    SerializedBuffer *buffer = (SerializedBuffer *) address;
    return buffer->getJavaByteBuffer();
}

void native_sendData(JNIEnv *env, jclass c, jint address, jint pos, jint limit) {
    SerializedBuffer *buff = (SerializedBuffer *) address;
//    DEBUG_D("native_sendData: %p %u %u", buff, (uint)pos, (uint)limit);
    buff->position((uint)pos);
    buff->limit((uint)limit);
//    DEBUG_D("native_sendData: %p %d %d", buff, buff->position(), buff->limit());
    NetworkManager::getInstance().sendData(buff);
}

JNIEXPORT void JNICALL native_connect(JNIEnv *env, jclass c) {
    NetworkManager::getInstance().connect();
}

void native_reconnect(JNIEnv *env, jclass c) {
    NetworkManager::getInstance().reconnect();
}

jint native_wrapData(JNIEnv *env, jclass c, jlong messageID, jbyteArray authKey, jlong authKeyID, jint bufferAddress) {
    return KeyGenerator::getInstance().wrapData(env, messageID, authKey, authKeyID, bufferAddress);
}

jboolean native_decryptMessage(JNIEnv *env, jclass c, jlong messageID, jbyteArray authKey, jlong authKeyID, jint bufferAddress, jint length, jint mark) {
    return KeyGenerator::getInstance().decryptMessage(env, messageID, authKey, authKeyID, bufferAddress, length, mark);
}

inline int registerNativeMethods(JNIEnv *env, const char *className, JNINativeMethod *methods, int methodsCount) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == 0) {
        DEBUG_E("CAN'T FIND CLASS");
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, methods, methodsCount) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

extern "C" int initNativeFunctions(JavaVM *vm, JNIEnv *env) {
    jvm = vm;
    jniEnv = env;

    jclass_ByteBuffer = (jclass) env->NewGlobalRef(env->FindClass("java/nio/ByteBuffer"));
    if (jclass_ByteBuffer == 0) {
        DEBUG_E("can't find java ByteBuffer class");
        exit(1);
    }
    jclass_ByteBuffer_allocateDirect = env->GetStaticMethodID(jclass_ByteBuffer, "allocateDirect", "(I)Ljava/nio/ByteBuffer;");
    if (jclass_ByteBuffer_allocateDirect == 0) {
        DEBUG_E("can't find java ByteBuffer allocateDirect");
        exit(1);
    }


    if (!registerNativeMethods(env, ApplicationLoaderClassPathName, ApplicationLoaderMethods, sizeof(ApplicationLoaderMethods) / sizeof(ApplicationLoaderMethods[0]))) {
        return JNI_FALSE;
    }

    if (!registerNativeMethods(env, SerializedBufferClassPathName, SerializedBufferMethods, sizeof(SerializedBufferMethods) / sizeof(SerializedBufferMethods[0]))) {
        return JNI_FALSE;
    }

    if (!registerNativeMethods(env, KeyGeneratorClassPathName, KeyGeneratorMethods, sizeof(KeyGeneratorMethods) / sizeof(KeyGeneratorMethods[0]))) {
        return JNI_FALSE;
    }

//    if (!registerNativeMethods(env, ConnectorClassPathName, ConnectorMethods, sizeof(ConnectorMethods) / sizeof(ConnectorMethods[0]))) {
//        return JNI_FALSE;
//    }

    if (!registerNativeMethods(env, NetworkManagerClassPathName, NetworkManagerMethods, sizeof(NetworkManagerMethods) / sizeof(NetworkManagerMethods[0]))) {
        return JNI_FALSE;
    }

    jclass_NetworkManager = (jclass) env->NewGlobalRef(env->FindClass(NetworkManagerClassPathName));
    if (jclass_NetworkManager == 0) {
        return JNI_FALSE;
    }
    jclass_NetworkManager_onConnectionDataReceived = env->GetStaticMethodID(jclass_NetworkManager, "onConnectionDataReceived", "(II)V");
    if (jclass_NetworkManager_onConnectionDataReceived == 0) {
        return JNI_FALSE;
    }
    jclass_NetworkManager_onConnectionStateChanged = env->GetStaticMethodID(jclass_NetworkManager, "onConnectionStateChanged", "(I)V");
    if (jclass_NetworkManager_onConnectionStateChanged == 0) {
        return JNI_FALSE;
    }
    LOG_D("JNI INITIALIZED");
    return JNI_TRUE;
}

