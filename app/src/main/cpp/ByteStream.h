//
// Created by negi on 14.06.17.
//

#ifndef PAYMON_BYTESTREAM_H
#define PAYMON_BYTESTREAM_H


#include <sys/types.h>
#include <vector>

class SerializedBuffer;

class ByteStream {

public:
    ByteStream();
    ~ByteStream();
    void append(SerializedBuffer *buffer);
    bool hasData();
    void get(SerializedBuffer *dst);
    void discard(uint count);
    void clean();

private:
    std::vector<SerializedBuffer *> buffersQueue;
};

#endif //PAYMON_BYTESTREAM_H
