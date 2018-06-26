#ifndef PAYMONSERVERDB_UTILS_H
#define PAYMONSERVERDB_UTILS_H

typedef uint8_t uint8;
typedef uint16_t uint16;
typedef uint32_t uint32;
typedef uint64_t uint64;
typedef int8_t int8;
typedef int16_t int16;
typedef int32_t int32;
typedef int64_t int64;
#include <iostream>

#include <memory>
#include <vector>
#include <sstream>
#include <iomanip>
#include "Packet.h"
#include "RPC.h"

const int endian_val = 1;
#define is_bigendian() ( (*(char*)&endian_val) == 0 )

class Utils {
public:
    template<typename ... Args> static std::string string_format(const std::string& format, Args ... args) {
        size_t size = (unsigned int)snprintf(nullptr, 0, format.c_str(), args ...) + 1;
        std::unique_ptr<char[]> buf(new char[size]);
        snprintf(buf.get(), size, format.c_str(), args ...);
        return std::string(buf.get(), buf.get() + size - 1);
    }

    static std::vector<std::string> splitString(const std::string& str, char separator);

    static void print(uint8_t* buffer, int size) {
        const int siz_ar = size; //sizeof(buffer);
//        for (int i = 0; i < siz_ar; ++i) {
//        cout << buffer[i] << " ";
//        }
        std::stringstream ss;
        for (int i = 0; i < siz_ar; ++i) {
            ss << std::hex << std::setfill('0') << std::setw(2) << std::uppercase << (int) buffer[i] << " ";
        }
        ss << std::endl;
        DEBUG_D("BYTES %s", ss.str().c_str());
    }
};


#endif //PAYMONSERVERDB_UTILS_H
