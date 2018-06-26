//
// Created by negi on 14.06.17.
//

#include "Timer.h"
#include "FileLog.h"
#include "EventObject.h"
#include "NetworkManager.h"

Timer::Timer(std::function<void()> function) {
    eventObject = new EventObject(this, EventObjectTypeTimer);
    callback = function;
}

Timer::~Timer() {
    stop();
    if (eventObject != nullptr) {
        delete eventObject;
        eventObject = nullptr;
    }
}

void Timer::start() {
    if (started || timeout == 0) {
        return;
    }
    started = true;
    NetworkManager::getInstance().scheduleEvent(eventObject, timeout);
}

void Timer::stop() {
    if (!started) {
        return;
    }
    started = false;
    NetworkManager::getInstance().removeEvent(eventObject);
}

void Timer::setTimeout(uint32_t ms, bool repeat) {
    if (ms == timeout) {
        return;
    }
    repeatable = repeat;
    timeout = ms;
    if (started) {
        NetworkManager::getInstance().removeEvent(eventObject);
        NetworkManager::getInstance().scheduleEvent(eventObject, timeout);
    }
}

void Timer::onEvent() {
    callback();
    DEBUG_D("timer(%p) call", this);
    if (started && repeatable && timeout != 0) {
        NetworkManager::getInstance().scheduleEvent(eventObject, timeout);
    }
}
