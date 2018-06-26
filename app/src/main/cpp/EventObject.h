//
// Created by negi on 14.06.17.
//

#ifndef PAYMON_EVENTOBJECT_H
#define PAYMON_EVENTOBJECT_H

#include <stdint.h>
#include "Defines.h"

enum EventObjectType {
    EventObjectTypeConnection,
    EventObjectTypeTimer,
    EventObjectTypePipe,
    EventObjectTypeEvent
};

class EventObject {

public:
    EventObject(void *object, EventObjectType type);
    void onEvent(uint32_t events);

    int64_t time;
    void *eventObject;
    EventObjectType eventType;
};

#endif //PAYMON_EVENTOBJECT_H
