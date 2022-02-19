package dev.zwander.cellreader.data.service

import android.annotation.SuppressLint
import android.app.ActivityThread
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.HandlerExecutor
import android.os.Looper
import android.telephony.*
import android.util.Log
import androidx.annotation.RequiresApi
import dev.zwander.cellreader.data.IPrivilegedListener
import dev.zwander.cellreader.data.IShizukuUserService
import kotlin.system.exitProcess

@SuppressLint("WrongConstant")
class ShizukuUserService() : IShizukuUserService.Stub() {
    init {
        if (Looper.getMainLooper() == null) {
            Looper.prepare()
        }
    }

    private val context = (ActivityThread.systemMain().systemContext as Context)
    private val listeners = mutableMapOf<Int, PhoneStateListenerSimple>()
    private val privilegedListeners = mutableMapOf<Int, MutableList<IPrivilegedListener>>()
    private val telephonyRegistryManager = context.getSystemService(Context.TELEPHONY_REGISTRY_SERVICE) as TelephonyRegistryManager

    override fun destroy() {
        exitProcess(0)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun registerPrivilegedListener(subId: Int, listener: IPrivilegedListener) {
        if (!privilegedListeners.containsKey(subId)) {
            privilegedListeners[subId] = mutableListOf()
        }
        privilegedListeners[subId]!!.add(listener)
        updateListenerState(subId)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun unregisterPrivilegedListener(subId: Int, listener: IPrivilegedListener) {
        if (!privilegedListeners.containsKey(subId)) {
            return
        }
        privilegedListeners[subId]!!.remove(listener)
        updateListenerState(subId)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun updateListenerState(subId: Int) {
        if (!privilegedListeners.containsKey(subId) || privilegedListeners[subId]!!.isEmpty()) {
            if (listeners.containsKey(subId)) {
                telephonyRegistryManager.unregisterTelephonyCallback(
                    subId,
                    "com.android.shell",
                    null,
                    listeners[subId],
                    true
                )
            }
        } else {
            if (!listeners.containsKey(subId)) {
                listeners[subId] = PhoneStateListenerSimple(subId)

                val listener = listeners[subId]
                telephonyRegistryManager.registerTelephonyCallback(
                    HandlerExecutor(Handler(Looper.getMainLooper())),
                    subId,
                    "com.android.shell",
                    null,
                    listener,
                    true
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    inner class PhoneStateListenerSimple(private val subId: Int) : TelephonyCallback(), TelephonyCallback.PhysicalChannelConfigListener {
        override fun onPhysicalChannelConfigChanged(configs: MutableList<PhysicalChannelConfig>) {
            privilegedListeners[subId]?.forEach {
                it.onPhysicalChannelConfigsChanged(subId, configs)
            }
        }
    }
}