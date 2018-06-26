//
// Created by negi on 14.06.17.
//

#ifndef PAYMON_CONNECTION_H
#define PAYMON_CONNECTION_H


#include <pthread.h>
#include <vector>
#include <string>
#include <openssl/aes.h>
#include "Socket.h"
#include "Defines.h"
#include "Utils.h"

class Datacenter;
class Timer;
class ByteStream;

enum ConnectionType {
    ConnectionTypeGeneric = 1,
    ConnectionTypeDownload = 2,
    ConnectionTypeUpload = 4,
    ConnectionTypePush = 8,
};

class Connection : public Socket {

public:
    Connection(ConnectionType type);
    ~Connection();

    void connect(std::string host, const uint16 &port);
    void suspendConnection();
    void sendData(SerializedBuffer *buffer, bool reportAck);
    uint32_t getConnectionToken();
    ConnectionType getConnectionType();
//    Datacenter *getDatacenter();

protected:
    void onReceivedData(SerializedBuffer *buffer) override;
    void onDisconnected(int reason) override;
    void onConnected() override;
    void reconnect();

private:
    enum TcpConnectionState {
        TcpConnectionStageIdle,
        TcpConnectionStageConnecting,
        TcpConnectionStageReconnecting,
        TcpConnectionStageConnected,
        TcpConnectionStageSuspended
    };

    TcpConnectionState connectionState = TcpConnectionStageIdle;
    uint32_t connectionToken = 0;
    std::string hostAddress;
    uint16_t hostPort;
    uint16_t failedConnectionCount;
    Datacenter *currentDatacenter;
    uint32_t currentAddressFlags;
    ConnectionType connectionType;
    bool firstPacketSent = false;
    SerializedBuffer *restOfTheData = nullptr;
    uint32_t lastPacketLength = 0;
    bool hasSomeDataSinceLastConnect = false;
    bool isTryingNextPort = false;
    bool wasConnected = false;
    uint32_t willRetryConnectCount = 5;
    Timer *reconnectTimer;

    AES_KEY encryptKey;
    uint8_t encryptIv[16];
    uint32_t encryptNum;
    uint8_t encryptCount[16];

    AES_KEY decryptKey;
    uint8_t decryptIv[16];
    uint32_t decryptNum;
    uint8_t decryptCount[16];

    friend class NetworkManager;
};

#endif //PAYMON_CONNECTION_H
