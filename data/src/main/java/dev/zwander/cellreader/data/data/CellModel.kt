package dev.zwander.cellreader.data.data

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.MutableLiveData
import dev.zwander.cellreader.data.IPrivilegedListener
import dev.zwander.cellreader.data.IShizukuUserService

object CellModel : CellModelBase() {
    val signalStrengths = MutableLiveData<HashMap<Int, SignalStrength?>>(hashMapOf())
    val telephonies = HashMap<Int, TelephonyManager>()

    var service: IShizukuUserService? = null

    val telephonyCallbacks = HashMap<Int, TelephonyCallback>()
    val privilegedCallbacks = HashMap<Int, IPrivilegedListener>()

    @Suppress("DEPRECATION")
    val telephonyListeners = HashMap<Int, PhoneStateListener>()

    override val LocalSignalStrengths = compositionLocalOf<MutableLiveData<HashMap<Int, SignalStrength?>>?> { signalStrengths }

    fun destroy() {
        telephonies.forEach { (subId, telephony) ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephony.unregisterTelephonyCallback(telephonyCallbacks[subId])
            } else {
                @Suppress("DEPRECATION")
                telephony.listen(telephonyListeners[subId], PhoneStateListener.LISTEN_NONE)
            }
        }

        service?.apply {
            privilegedCallbacks.forEach { (subId, callback) ->
                unregisterPrivilegedListener(subId, callback)
            }
        }

        clear()
    }

    override fun clear() {
        super.clear()
        signalStrengths.value = hashMapOf()
        telephonies.clear()
        privilegedCallbacks.clear()
    }
}

@RequiresApi(Build.VERSION_CODES.S)
class TelephonyListener(
    private val subId: Int,
    private val listenerCallback: TelephonyListenerCallback
) : TelephonyCallback(),
    TelephonyCallback.CellInfoListener, TelephonyCallback.SignalStrengthsListener,
    TelephonyCallback.ServiceStateListener, TelephonyCallback.DisplayInfoListener,
    TelephonyCallback.DataConnectionStateListener {
    @SuppressLint("MissingPermission")
    override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
        listenerCallback.updateCellInfo(subId, cellInfo ?: mutableListOf())
    }

    override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
        listenerCallback.updateSignal(subId, signalStrength)
    }

    override fun onServiceStateChanged(serviceState: ServiceState?) {
        listenerCallback.updateServiceState(subId, serviceState)
    }

    override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo?) {
        listenerCallback.updateDisplayInfo(subId, telephonyDisplayInfo)
    }

    override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
        listenerCallback.updateDataConnectionState(subId, state, networkType)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
class PhysicalChannelConfigListener(
    private val listenerCallback: TelephonyListenerCallback
) : IPrivilegedListener.Stub() {
    @Suppress("UNCHECKED_CAST")
    override fun onPhysicalChannelConfigsChanged(subId: Int, configs: MutableList<Any?>, string: String?) {
        listenerCallback.updatePhysicalChannelConfigs(subId, configs as MutableList<PhysicalChannelConfig>)
    }
}

@Suppress("DEPRECATION")
class StateListener(
    private val subId: Int,
    private val listenerCallback: TelephonyListenerCallback
) : PhoneStateListener() {
    @Deprecated("Deprecated in Java")
    override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
        listenerCallback.updateCellInfo(subId, cellInfo ?: mutableListOf())
    }

    @Deprecated("Deprecated in Java")
    override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
        listenerCallback.updateSignal(subId, signalStrength)
    }

    @Deprecated("Deprecated in Java")
    override fun onServiceStateChanged(serviceState: ServiceState?) {
        listenerCallback.updateServiceState(subId, serviceState)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingPermission")
    override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo?) {
        listenerCallback.updateDisplayInfo(subId, telephonyDisplayInfo)
    }

    @Deprecated("Deprecated in Java")
    override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
        listenerCallback.updateDataConnectionState(subId, state, networkType)
    }
}

interface TelephonyListenerCallback {
    fun updateCellInfo(subId: Int, infos: MutableList<CellInfo>)
    fun updateSignal(subId: Int, strength: SignalStrength?)
    fun updateServiceState(subId: Int, serviceState: ServiceState?)
    fun updatePhysicalChannelConfigs(subId: Int, configs: List<PhysicalChannelConfig>)
    fun updateDisplayInfo(subId: Int, telephonyDisplayInfo: TelephonyDisplayInfo?)
    fun updateDataConnectionState(subId: Int, state: Int, networkType: Int)
}