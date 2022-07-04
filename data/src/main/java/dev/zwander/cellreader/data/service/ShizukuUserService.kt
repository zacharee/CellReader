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
import com.android.internal.telephony.ITelephony
import dev.zwander.cellreader.data.INetworkScanCallback
import dev.zwander.cellreader.data.IPrivilegedListener
import dev.zwander.cellreader.data.IShizukuUserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlin.system.exitProcess

@SuppressLint("WrongConstant")
class ShizukuUserService : IShizukuUserService.Stub {
    constructor() {
        Log.e("CellReader", "init")
    }

    init {
        Log.e("CellReader", "init")

        if (Looper.getMainLooper() == null) {
            Looper.prepare()
        }

        ActivityThread.initializeMainlineModules()

        Log.e("CellReader", "init")
    }

    private val context = (ActivityThread.systemMain().systemContext as Context)
    private val listeners = mutableMapOf<Int, PhoneStateListenerSimple>()
    private val privilegedListeners = mutableMapOf<Int, MutableList<IPrivilegedListener>>()
    private val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val telephonyRegistryManager = context.getSystemService(Context.TELEPHONY_REGISTRY_SERVICE) as TelephonyRegistryManager

    override fun destroy() {
        Log.e("CellReader", "Destroyed")
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

    @SuppressLint("SoonBlockedPrivateApi", "MissingPermission")
    override fun requestNetworkScan(subId: Int, request: NetworkScanRequest?, callback: INetworkScanCallback?): MutableList<Any?> {
        ITelephony.Stub.asInterface(
            TelephonyFrameworkInitializer
                .getTelephonyServiceManager()
                .telephonyServiceRegisterer
                .get()
        ).apply {
            Log.e("CellReader", "stub $this")
        }

        return try {
            telephony.createForSubscriptionId(subId)
                .requestNetworkScan(
                    request,
                    Dispatchers.Main.asExecutor(),
                    object : TelephonyScanManager.NetworkScanCallback() {
                        override fun onComplete() {
                            callback?.onComplete()
                        }

                        override fun onError(error: Int) {
                            callback?.onError(error)
                        }

                        @SuppressLint("MissingPermission")
                        override fun onResults(results: MutableList<CellInfo>) {
                            callback?.onResults(results)
                        }
                    }
                ).run { mutableListOf(subId, NetworkScan::class.java.getDeclaredField("mSubId").apply { isAccessible = true }.getInt(this)) }
        } catch (e: Exception) {
            Log.e("CellReader", "Error", e)

            Log.e("CellReader", "${telephony.createForSubscriptionId(subId)
                .availableNetworks}")

            mutableListOf()
        }
    }

    override fun cancelNetworkScan(subId: Int, scanId: Int) {
        NetworkScan(subId, scanId).stopScan()
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

                if (Build.VERSION.SDK_INT < 33) {
                    telephonyRegistryManager::class.java
                        .getMethod(
                            "registerTelephonyCallback",
                            HandlerExecutor::class.java,
                            Int::class.java,
                            String::class.java,
                            String::class.java,
                            TelephonyCallback::class.java,
                            Boolean::class.java
                        )
                        .invoke(
                            telephonyRegistryManager,
                            HandlerExecutor(Handler(Looper.getMainLooper())),
                            subId,
                            "com.android.shell",
                            null,
                            listener,
                            true
                        )
                } else {
                    telephonyRegistryManager.registerTelephonyCallback(
                        false, false,
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
    }

    @RequiresApi(Build.VERSION_CODES.S)
    inner class PhoneStateListenerSimple(private val subId: Int) : TelephonyCallback(), TelephonyCallback.PhysicalChannelConfigListener {
        override fun onPhysicalChannelConfigChanged(configs: MutableList<PhysicalChannelConfig>) {
            Log.e("CellReader", "new configs $configs")
            privilegedListeners[subId]?.forEach {
                it.onPhysicalChannelConfigsChanged(subId, configs, configs.toString())
            }
        }
    }
}