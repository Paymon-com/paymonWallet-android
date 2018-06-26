# Install script for directory: /home/negi/Development/Android/paymon/app/libs/boringssl/decrepit

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "/home/negi/Development/Android/sdk/ndk-bundle/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/user")
endif()
string(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
if(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  if(BUILD_TYPE)
    string(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  else()
    set(CMAKE_INSTALL_CONFIG_NAME "")
  endif()
  message(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
endif()

# Set the component getting installed.
if(NOT CMAKE_INSTALL_COMPONENT)
  if(COMPONENT)
    message(STATUS "Install component: \"${COMPONENT}\"")
    set(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  else()
    set(CMAKE_INSTALL_COMPONENT)
  endif()
endif()

# Install shared libraries without execute permission?
if(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)
  set(CMAKE_INSTALL_SO_NO_EXE "1")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for each subdirectory.
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/bio/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/blowfish/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/cast/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/des/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/dh/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/dsa/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/evp/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/obj/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/rc4/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/ripemd/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/rsa/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/ssl/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/x509/cmake_install.cmake")
  include("/home/negi/Development/Android/paymon/app/libs/boringssl/build-android/decrepit/xts/cmake_install.cmake")

endif()

