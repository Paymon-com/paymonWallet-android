#include <stdio.h>
#include <jni.h>
#include <stdlib.h>
#include <openssl/aes.h>
#include <time.h>

int initNativeFunctions(JavaVM *vm, JNIEnv *env);

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = 0;
//    jvm = vm;
    srand(time(NULL));

    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
//        DEBUG_E("JNI_VERSION_1_6 not found");
        return -1;
    }

    if (initNativeFunctions(vm, env) != JNI_TRUE) {
//        DEBUG_E("initNativeFunctions failed");
        return -1;
    }
//    std::cout.rdbuf(new androidbuf);
//    DEBUG_D("JNI loaded");
    return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {

}