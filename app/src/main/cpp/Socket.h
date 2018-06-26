//
// Created by negi on 14.06.17.
//

#ifndef PAYMON_SOCKET_H
#define PAYMON_SOCKET_H

#include <sys/epoll.h>
#include <netinet/in.h>
#include <ctime>
#include <string>
#include "ByteStream.h"
#include "EventObject.h"

class Socket {
public:
    Socket();
    virtual ~Socket();

    void writeBuffer(SerializedBuffer *buffer);
    void openConnection(std::string address, uint16_t port, bool ipv6);
    void setTimeout(time_t timeout);
    bool isDisconnected();
    void onEvent(uint32_t events);

    void dropConnection();
protected:
    void checkTimeout(int64_t now);
    virtual void onReceivedData(SerializedBuffer *buffer) = 0;
    virtual void onDisconnected(int reason) = 0;
    virtual void onConnected() = 0;
private:
    ByteStream *outgoingByteStream = nullptr;
    struct epoll_event eventMask;
    struct sockaddr_in socketAddress;
    struct sockaddr_in6 socketAddress6;
    int socketFd = -1;
    time_t timeout = 15;
    bool onConnectedSent = false;
    int64_t lastEventTime = 0;
    EventObject *eventObject;

    bool checkSocketError();
    void closeSocket(int reason);
    void adjustWriteOp();

    friend class EventObject;
    friend class NetworkManager;
};


#endif //PAYMON_SOCKET_H
