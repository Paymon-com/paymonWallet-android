//
// Created by negi on 14.06.17.
//

#ifndef PAYMON_TIMER_H
#define PAYMON_TIMER_H

#include <stdint.h>
#include <functional>
#include <sys/epoll.h>

class EventObject;

class Timer {

public:
    Timer(std::function<void()> function);
    ~Timer();

    void start();
    void stop();
    void setTimeout(uint32_t ms, bool repeat);

private:
    void onEvent();

    bool started = false;
    bool repeatable = false;
    uint32_t timeout = 0;
    std::function<void()> callback;
    EventObject *eventObject;

    friend class EventObject;
};

#endif //PAYMON_TIMER_H
