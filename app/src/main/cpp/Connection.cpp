/*
 * This is the source code of tgnet library v. 1.0
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2015.
 */

#include <openssl/rand.h>
#include <stdlib.h>
#include "Connection.h"
#include "NetworkManager.h"
#include "Timer.h"

static uint32_t lastConnectionToken = 1;

Connection::Connection(ConnectionType type) {
//    currentDatacenter = datacenter;
    connectionType = type;
//    genereateNewSessionId();
    connectionState = TcpConnectionStageIdle;
    reconnectTimer = new Timer([&] {
        reconnectTimer->stop();
        connect(NetworkManager::getInstance().host, NetworkManager::getInstance().port);
    });
}

Connection::~Connection() {
    if (reconnectTimer != nullptr) {
        reconnectTimer->stop();
        delete reconnectTimer;
        reconnectTimer = nullptr;
    }
}

void Connection::suspendConnection() {
    reconnectTimer->stop();
    if (connectionState == TcpConnectionStageIdle || connectionState == TcpConnectionStageSuspended) {
        return;
    }
    DEBUG_D("connection(%p, type %d) suspend", this, connectionType);
    connectionState = TcpConnectionStageSuspended;
    dropConnection();
    NetworkManager::getInstance().onConnectionClosed(this);
    firstPacketSent = false;
    if (restOfTheData != nullptr) {
        restOfTheData->reuse();
        restOfTheData = nullptr;
    }
    lastPacketLength = 0;
    connectionToken = 0;
    wasConnected = false;
}

void Connection::onReceivedData(SerializedBuffer *buffer) {
    AES_ctr128_encrypt(buffer->bytes(), buffer->bytes(), buffer->limit(), &decryptKey, decryptIv, decryptCount, &decryptNum);

    failedConnectionCount = 0;

    SerializedBuffer *parseLaterBuffer = nullptr;
    if (restOfTheData != nullptr) {
        if (lastPacketLength == 0) {
            if (restOfTheData->capacity() - restOfTheData->position() >= buffer->limit()) {
                restOfTheData->limit(restOfTheData->position() + buffer->limit());
                restOfTheData->writeBytes(buffer);
                buffer = restOfTheData;
            } else {
                SerializedBuffer *newBuffer = BuffersStorage::getInstance().getFreeBuffer(restOfTheData->limit() + buffer->limit());
                restOfTheData->rewind();
                newBuffer->writeBytes(restOfTheData);
                newBuffer->writeBytes(buffer);
                buffer = newBuffer;
                restOfTheData->reuse();
                restOfTheData = newBuffer;
            }
        } else {
            uint32_t len;
            if (lastPacketLength - restOfTheData->position() <= buffer->limit()) {
                len = lastPacketLength - restOfTheData->position();
            } else {
                len = buffer->limit();
            }
            uint32_t oldLimit = buffer->limit();
            buffer->limit(len);
            restOfTheData->writeBytes(buffer);
            buffer->limit(oldLimit);
            if (restOfTheData->position() == lastPacketLength) {
                parseLaterBuffer = buffer->hasRemaining() ? buffer : nullptr;
                buffer = restOfTheData;
            } else {
                return;
            }
        }
    }

    buffer->rewind();

    while (buffer->hasRemaining()) {
        if (!hasSomeDataSinceLastConnect) {
//            currentDatacenter->storeCurrentAddressAndPortNum();
            isTryingNextPort = false;
            if (connectionType == ConnectionTypePush) {
                setTimeout(60 * 15);
            } else {
                setTimeout(25);
            }
        }
        hasSomeDataSinceLastConnect = true;

        uint32_t currentPacketLength = 0;
        uint32_t mark = buffer->position();
        uint8_t fByte = buffer->readByte(nullptr);

//        if ((fByte & (1 << 7)) != 0) {
//            buffer->position(mark);
//            if (buffer->remaining() < 4) {
//                SerializedBuffer *reuseLater = restOfTheData;
//                restOfTheData = BuffersStorage::getInstance().getFreeBuffer(16384);
//                restOfTheData->writeBytes(buffer);
//                restOfTheData->limit(restOfTheData->position());
//                lastPacketLength = 0;
//                if (reuseLater != nullptr) {
//                    reuseLater->reuse();
//                }
//                break;
//            }
//            int32_t ackId = buffer->readBigInt32(nullptr) & (~(1 << 31));
//            NetworkManager::getInstance().onConnectionQuickAckReceived(this, ackId);
//            continue;
//        }

        if (fByte != 0x7f) {
            currentPacketLength = ((uint32_t) fByte) * 4;
        } else {
            buffer->position(mark);
            if (buffer->remaining() < 4) {
                if (restOfTheData == nullptr || (restOfTheData != nullptr && restOfTheData->position() != 0)) {
                    SerializedBuffer *reuseLater = restOfTheData;
                    restOfTheData = BuffersStorage::getInstance().getFreeBuffer(16384);
                    restOfTheData->writeBytes(buffer);
                    restOfTheData->limit(restOfTheData->position());
                    lastPacketLength = 0;
                    if (reuseLater != nullptr) {
                        reuseLater->reuse();
                    }
                } else {
                    restOfTheData->position(restOfTheData->limit());
                }
                break;
            }
            currentPacketLength = ((uint32_t) buffer->readInt32(nullptr) >> 8) * 4;
        }

        if (currentPacketLength % 4 != 0 || currentPacketLength > 2 * 1024 * 1024) {
            DEBUG_E("connection(%p, type %d) received invalid packet length", this, connectionType);
            reconnect();
            return;
        }

        if (currentPacketLength < buffer->remaining()) {
            DEBUG_D("connection(%p, type %d) received message len %u but packet larger %u", this, /*currentDatacenter->getDatacenterId(),*/ connectionType, currentPacketLength, buffer->remaining());
        } else if (currentPacketLength == buffer->remaining()) {
            DEBUG_D("connection(%p, type %d) received message len %u equal to packet size", this, /*currentDatacenter->getDatacenterId(),*/ connectionType, currentPacketLength);
        } else {
            DEBUG_D("connection(%p, type %d) received packet size less(%u) then message size(%u)", this, /*currentDatacenter->getDatacenterId(),*/ connectionType, buffer->remaining(), currentPacketLength);

            SerializedBuffer *reuseLater = nullptr;
            uint32_t len = currentPacketLength + (fByte != 0x7f ? 1 : 4);
            if (restOfTheData != nullptr && restOfTheData->capacity() < len) {
                reuseLater = restOfTheData;
                restOfTheData = nullptr;
            }
            if (restOfTheData == nullptr) {
                buffer->position(mark);
                restOfTheData = BuffersStorage::getInstance().getFreeBuffer(len);
                restOfTheData->writeBytes(buffer);
            } else {
                restOfTheData->position(restOfTheData->limit());
                restOfTheData->limit(len);
            }
            lastPacketLength = len;
            if (reuseLater != nullptr) {
                reuseLater->reuse();
            }
            return;
        }

        uint32_t old = buffer->limit();
        buffer->limit(buffer->position() + currentPacketLength);
        NetworkManager::getInstance().onConnectionDataReceived(this, buffer, currentPacketLength);
        buffer->position(buffer->limit());
        buffer->limit(old);

        if (restOfTheData != nullptr) {
            if ((lastPacketLength != 0 && restOfTheData->position() == lastPacketLength) || (lastPacketLength == 0 && !restOfTheData->hasRemaining())) {
                restOfTheData->reuse();
                restOfTheData = nullptr;
            } else {
                DEBUG_E("compact occured");
                restOfTheData->compact();
                restOfTheData->limit(restOfTheData->position());
                restOfTheData->position(0);
            }
        }

        if (parseLaterBuffer != nullptr) {
            buffer = parseLaterBuffer;
            parseLaterBuffer = nullptr;
        }
    }
}

void Connection::connect(std::string host, const uint16 &port) {
    if (!NetworkManager::getInstance().isNetworkAvailable()) {
        DEBUG_E("Network no available");
        NetworkManager::getInstance().onConnectionClosed(this);
        return;
    }
    if ((connectionState == TcpConnectionStageConnected || connectionState == TcpConnectionStageConnecting)) {
        DEBUG_E("Connected already");
        return;
    }
    connectionState = TcpConnectionStageConnecting;
    bool ipv6 = NetworkManager::getInstance().isIpv6Enabled();
//    if (connectionType == ConnectionTypeDownload) {
//        currentAddressFlags = 2;
//        hostAddress = currentDatacenter->getCurrentAddress(currentAddressFlags | (ipv6 ? 1 : 0));
//        if (hostAddress.empty()) {
//            currentAddressFlags = 0;
//            hostAddress = currentDatacenter->getCurrentAddress(currentAddressFlags | (ipv6 ? 1 : 0));
//        }
//        if (hostAddress.empty() && ipv6) {
//            currentAddressFlags = 2;
//            hostAddress = currentDatacenter->getCurrentAddress(currentAddressFlags);
//            if (hostAddress.empty()) {
//                currentAddressFlags = 0;
//                hostAddress = currentDatacenter->getCurrentAddress(currentAddressFlags);
//            }
//        }
//    } else {
//        currentAddressFlags = 0;
//        hostAddress = currentDatacenter->getCurrentAddress(currentAddressFlags | (ipv6 ? 1 : 0));
//        if (ipv6 && hostAddress.empty()) {
//            hostAddress = currentDatacenter->getCurrentAddress(currentAddressFlags);
//        }
//    }
//    hostPort = (uint16_t) currentDatacenter->getCurrentPort(currentAddressFlags);

    hostAddress = host;
    hostPort = port;
    reconnectTimer->stop();

    DEBUG_D("connection(%p, type %d) connecting (%s:%hu)", this, /*currentDatacenter->getDatacenterId(), */connectionType, hostAddress.c_str(), hostPort);
    firstPacketSent = false;
    if (restOfTheData != nullptr) {
        restOfTheData->reuse();
        restOfTheData = nullptr;
    }
    lastPacketLength = 0;
    wasConnected = false;
    hasSomeDataSinceLastConnect = false;
    openConnection(hostAddress, hostPort, ipv6);
    if (connectionType == ConnectionTypePush) {
        if (isTryingNextPort) {
            setTimeout(20);
        } else {
            setTimeout(30);
        }
    } else {
        if (isTryingNextPort) {
            setTimeout(8);
        } else {
            if (connectionType == ConnectionTypeUpload) {
                setTimeout(25);
            } else {
                setTimeout(15);
            }
        }
    }
}

void Connection::reconnect() {
    if (connectionState == TcpConnectionStageReconnecting || connectionState == TcpConnectionStageConnecting) {
        return;
    }
    suspendConnection();
    connectionState = TcpConnectionStageReconnecting;
//    ConnectiosManagerDelegate *delegate = NetworkManager::getInstance().delegate;
//    if (delegate != nullptr) {
//        delegate->onConnectionStateChanged(ConnectionStateConnecting);
//    }

    connect(NetworkManager::getInstance().host, NetworkManager::getInstance().port);
}

void Connection::sendData(SerializedBuffer *buff, bool reportAck) {
    if (buff == nullptr) {
        return;
    }
    buff->rewind();
    if (connectionState == TcpConnectionStageIdle || connectionState == TcpConnectionStageReconnecting || connectionState == TcpConnectionStageSuspended) {
        connect(hostAddress, hostPort);
    }

    if (isDisconnected()) {
        buff->reuse();
        DEBUG_D("connection(%p, type %d) disconnected, don't send data", this, connectionType);
        return;
    }

    uint32_t bufferLen = 0;
    uint32_t packetLength = buff->limit() / 4;

    if (packetLength < 0x7f) {
        bufferLen++;
    } else {
        bufferLen += 4;
    }
    if (!firstPacketSent) {
        bufferLen += 64;
    }

    SerializedBuffer *buffer = BuffersStorage::getInstance().getFreeBuffer(bufferLen);
    uint8_t *bytes = buffer->bytes();

    if (!firstPacketSent) {
        DEBUG_D("Genrating AES key");
        buffer->position(64);
        static uint8_t temp[64];
        while (true) {
            RAND_bytes(bytes, 64);

            uint32_t val = (bytes[3] << 24) | (bytes[2] << 16) | (bytes[1] << 8) | (bytes[0]);
            uint32_t val2 = (bytes[7] << 24) | (bytes[6] << 16) | (bytes[5] << 8) | (bytes[4]);
            if (bytes[0] != 0xef && val != 0x44414548 && val != 0x54534f50 && val != 0x20544547 && val != 0x4954504f && val != 0xeeeeeeee && val2 != 0x00000000) {
                bytes[56] = bytes[57] = bytes[58] = bytes[59] = 0xef;
                break;
            }
        }

        for (int i = 0; i < 48; i++) {
            temp[i] = bytes[55 - i];
        }

        encryptNum = decryptNum = 0;
        memset(encryptCount, 0, 16);
        memset(decryptCount, 0, 16);

        if (AES_set_encrypt_key(bytes + 8, 256, &encryptKey) < 0) {
            DEBUG_E("unable to set encryptKey");
            exit(1);
        }
        if (!is_bigendian()) {
            for (int i = 0; i < 8; i++) {
//                encryptKey.rd_key[i] = htonl(encryptKey.rd_key[i]);
            }
        }

        memcpy(encryptIv, bytes + 40, 16);

        if (AES_set_encrypt_key(temp, 256, &decryptKey) < 0) {
            DEBUG_E("unable to set decryptKey");
            exit(1);
        }
        if (!is_bigendian()) {
            for (int i = 0; i < 8; i++) {
                decryptKey.rd_key[i] = htonl(decryptKey.rd_key[i]);
            }
        }

        memcpy(decryptIv, temp + 32, 16);
        AES_ctr128_encrypt(bytes, temp, 64, &encryptKey, encryptIv, encryptCount, &encryptNum);
        memcpy(bytes + 56, temp + 56, 8);
        firstPacketSent = true;
    }
    if (packetLength < 0x7f) {
        if (reportAck) {
            packetLength |= (1 << 7);
        }
        buffer->writeByte((uint8_t) packetLength);
//        Utils::print(buffer->bytes(), buffer->limit());
        bytes += (buffer->limit() - 1);
        AES_ctr128_encrypt(bytes, bytes, 1, &encryptKey, encryptIv, encryptCount, &encryptNum);
    } else {
        packetLength = (packetLength << 8) + 0x7f;
        if (reportAck) {
            packetLength |= (1 << 7);
        }
        buffer->writeInt32(packetLength);
//        Utils::print(buffer->bytes(), buffer->limit());
        bytes += (buffer->limit() - 4);
        AES_ctr128_encrypt(bytes, bytes, 4, &encryptKey, encryptIv, encryptCount, &encryptNum);
    }

    buffer->rewind();
    writeBuffer(buffer);
    buff->rewind();
    AES_ctr128_encrypt(buff->bytes(), buff->bytes(), buff->limit(), &encryptKey, encryptIv, encryptCount, &encryptNum);
    writeBuffer(buff);
}

void Connection::onDisconnected(int reason) {
    reconnectTimer->stop();
    DEBUG_D("connection(%p, type %d) disconnected with reason %d", this, connectionType, reason);
    bool switchToNextPort = wasConnected && !hasSomeDataSinceLastConnect && reason == 2;
    firstPacketSent = false;
    if (restOfTheData != nullptr) {
        restOfTheData->reuse();
        restOfTheData = nullptr;
    }
    connectionToken = 0;
    lastPacketLength = 0;
    wasConnected = false;
    if (connectionState != TcpConnectionStageSuspended && connectionState != TcpConnectionStageIdle) {
        connectionState = TcpConnectionStageIdle;
    }
    NetworkManager::getInstance().onConnectionClosed(this);

//    uint32_t datacenterId = currentDatacenter->getDatacenterId();
    if (connectionState == TcpConnectionStageIdle && connectionType == ConnectionTypeGeneric /*&& (currentDatacenter->isHandshaking() || datacenterId == NetworkManager::getInstance().currentDatacenterId || datacenterId == NetworkManager::getInstance().movingToDatacenterId)*/) {
        connectionState = TcpConnectionStageReconnecting;
        failedConnectionCount++;
        if (failedConnectionCount == 1) {
            if (hasSomeDataSinceLastConnect) {
                willRetryConnectCount = 5;
            } else {
                willRetryConnectCount = 1;
            }
        }
        if (NetworkManager::getInstance().isNetworkAvailable()) {
            isTryingNextPort = true;
            if (failedConnectionCount > willRetryConnectCount || switchToNextPort) {
//                currentDatacenter->nextAddressOrPort(currentAddressFlags);
                failedConnectionCount = 0;
            }
        }
        DEBUG_D("connection(%p, type %d) reconnect %s:%hu", this, connectionType, hostAddress.c_str(), hostPort);
        reconnectTimer->setTimeout(2000, false);
//        reconnectTimer->start();
    }
}

void Connection::onConnected() {
    connectionState = TcpConnectionStageConnected;
    connectionToken = lastConnectionToken++;
    wasConnected = true;
    DEBUG_D("connection(%p, type %d) connected to %s:%hu", this, connectionType, hostAddress.c_str(), hostPort);
    NetworkManager::getInstance().onConnectionConnected(this);
}

ConnectionType Connection::getConnectionType() {
    return connectionType;
}

uint32_t Connection::getConnectionToken() {
    return connectionToken;
}
