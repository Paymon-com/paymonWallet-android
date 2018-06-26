/*
 * This is the source code of tgnet library v. 1.0
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2015.
 */

#ifndef TLOBJECT_H
#define TLOBJECT_H

#include <stdint.h>

class SerializedBuffer;

class Packet {

public:
    virtual ~Packet();
    virtual void readParams(SerializedBuffer *stream, bool &error);
    virtual void serializeToStream(SerializedBuffer *stream);
    virtual Packet *deserializeResponse(SerializedBuffer *stream, uint32_t constructor, bool &error);
    uint32_t getObjectSize();
    virtual bool isNeedLayer();
};

#endif
