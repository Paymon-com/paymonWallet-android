cmake_minimum_required(VERSION 3.4.1)

include_directories(${CMAKE_SOURCE_DIR}/libs/boringssl/include)
SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -std=c++11 -DANDROID -finline-functions -ffast-math")

add_library(paymon-lib
     SHARED
     src/main/cpp/KeyGenerator.cpp
     src/main/cpp/KeyGenerator.h
     src/main/cpp/BuffersStorage.cpp
     src/main/cpp/BuffersStorage.h
     src/main/cpp/ByteArray.cpp
     src/main/cpp/ByteArray.h
     src/main/cpp/FileLog.cpp
     src/main/cpp/FileLog.h
     src/main/cpp/SerializedBuffer.cpp
     src/main/cpp/SerializedBuffer.h
     src/main/cpp/Packet.cpp
     src/main/cpp/Packet.h
     src/main/cpp/ByteStream.cpp
     src/main/cpp/ByteStream.h
     src/main/cpp/Socket.cpp
     src/main/cpp/Socket.h
     src/main/cpp/Defines.h
     src/main/cpp/NetworkManager.cpp
     src/main/cpp/NetworkManager.h
     src/main/cpp/EventObject.cpp
     src/main/cpp/EventObject.h
     src/main/cpp/Timer.cpp
     src/main/cpp/Timer.h
     src/main/cpp/Connection.cpp
     src/main/cpp/Connection.h
     src/main/cpp/RPC.cpp
     src/main/cpp/RPC.h
     src/main/cpp/Utils.cpp
     src/main/cpp/Utils.h
     src/main/cpp/ClassStore.cpp
     src/main/cpp/ClassStore.h
     src/main/cpp/PaymonWrapper.cpp
     src/main/cpp/PaymonWrapper.h
     src/main/cpp/jni.c
     #libs/boringssl/crypto/fipsmodule/modes/ctr.c
)

find_library(log-lib log)
#find_library(crypto_lib crypto)
add_library(crypto STATIC IMPORTED)
set_target_properties(crypto
  PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/boringssl/build-android/crypto/libcrypto.a)

add_library(ssl STATIC IMPORTED)
set_target_properties(ssl
  PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/boringssl/build-android/ssl/libssl.a)

#add_library( app-glue
#             STATIC
#             ${ANDROID_NDK}/sources/android/native_app_glue/android_native_app_glue.c )

# You need to link static libraries against your shared native library.
#target_link_libraries( native-lib app-glue ${log-lib} )
target_link_libraries(paymon-lib ssl crypto ${log-lib})

