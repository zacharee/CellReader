package dev.zwander.cellreader.data;

interface INetworkScanCallback {
    void onResults(out List results) = 1;
    void onComplete() = 2;
    void onError(int error) = 3;
}