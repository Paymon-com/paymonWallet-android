/*
 * This is the source code of tgnet library v. 1.0
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2015.
 */

#include "Packet.h"
#include "SerializedBuffer.h"

thread_local SerializedBuffer *sizeCalculatorBuffer = new SerializedBuffer(true);

Packet::~Packet() {

}

void Packet::readParams(SerializedBuffer *stream, bool &error) {

}

void Packet::serializeToStream(SerializedBuffer *stream) {

}

Packet *Packet::deserializeResponse(SerializedBuffer *stream, uint32_t constructor, bool &error) {
    return nullptr;
}

uint32_t Packet::getObjectSize() {
    sizeCalculatorBuffer->clearCapacity();
    serializeToStream(sizeCalculatorBuffer);
    return sizeCalculatorBuffer->capacity();
}

bool Packet::isNeedLayer() {
    return false;
}
