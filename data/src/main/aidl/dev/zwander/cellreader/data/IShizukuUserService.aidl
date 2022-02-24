package dev.zwander.cellreader.data;

import dev.zwander.cellreader.data.IPrivilegedListener;
import dev.zwander.cellreader.data.INetworkScanCallback;

interface IShizukuUserService {
    void destroy() = 16777114;
    void registerPrivilegedListener(int subId, IPrivilegedListener listener) = 1;
    void unregisterPrivilegedListener(int subId, IPrivilegedListener listener) = 2;

    List requestNetworkScan(int subId, in NetworkScanRequest request, INetworkScanCallback callback) = 3;
    void cancelNetworkScan(int subId, int scanId) = 4;
}