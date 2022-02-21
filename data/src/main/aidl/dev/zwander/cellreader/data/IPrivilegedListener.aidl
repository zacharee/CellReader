package dev.zwander.cellreader.data;

import java.util.List;

interface IPrivilegedListener {
    void onPhysicalChannelConfigsChanged(int subId, inout List configs, String string);
}