//
// Created by negi on 23.04.17.
//

#ifndef PAYMONSERVEREPOLL_CLASSSTORE_H
#define PAYMONSERVEREPOLL_CLASSSTORE_H


#include "Packet.h"

class ClassStore {
public:
    static Packet *PMdeserialize(SerializedBuffer *stream, uint32_t bytes, uint32_t constructor, bool &error);
};


#endif //PAYMONSERVEREPOLL_CLASSSTORE_H
