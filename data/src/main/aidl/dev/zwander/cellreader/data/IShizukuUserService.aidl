package dev.zwander.cellreader.data;

import dev.zwander.cellreader.data.IPrivilegedListener;

interface IShizukuUserService {
    void destroy() = 16777114;
    void registerPrivilegedListener(int subId, IPrivilegedListener listener) = 1;
    void unregisterPrivilegedListener(int subId, IPrivilegedListener listener) = 2;
}