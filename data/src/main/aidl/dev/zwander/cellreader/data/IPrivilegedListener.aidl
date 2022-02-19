package dev.zwander.cellreader.data;

import java.util.List;

interface IPrivilegedListener {
    void onPhysicalChannelConfigsChanged(int subId, out List configs);
}