package dev.zwander.cellreader.data.data

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData

object CellModel : CellModelBase() {
    val signalStrengths = MutableLiveData<MutableMap<Int, SignalStrength?>>(mutableMapOf())
    val telephonies = HashMap<Int, TelephonyManager>()

    val telephonyCallbacks = HashMap<Int, TelephonyCallback>()
    @Suppress("DEPRECATION")
    val telephonyListeners = HashMap<Int, PhoneStateListener>()

    fun destroy() {
        telephonies.forEach { (subId, telephony) ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephony.unregisterTelephonyCallback(telephonyCallbacks[subId])
            } else {
                @Suppress("DEPRECATION")
                telephony.listen(telephonyListeners[subId], PhoneStateListener.LISTEN_NONE)
            }
        }

        clear()
    }

    override fun clear() {
        super.clear()
        signalStrengths.value = hashMapOf()
        telephonies.clear()
    }
}

@RequiresApi(Build.VERSION_CODES.S)
class TelephonyListener(private val subId: Int, private val listenerCallback: TelephonyListenerCallback) : TelephonyCallback(),
    TelephonyCallback.CellInfoListener, TelephonyCallback.SignalStrengthsListener,
    TelephonyCallback.ServiceStateListener {
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
}

@Suppress("DEPRECATION")
class StateListener(private val subId: Int, private val listenerCallback: TelephonyListenerCallback) : PhoneStateListener() {
    override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
        listenerCallback.updateCellInfo(subId, cellInfo ?: mutableListOf())
    }

    override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
        listenerCallback.updateSignal(subId, signalStrength)
    }

    override fun onServiceStateChanged(serviceState: ServiceState?) {
        listenerCallback.updateServiceState(subId, serviceState)
    }
}

interface TelephonyListenerCallback {
    fun updateCellInfo(subId: Int, infos: MutableList<CellInfo>)
    fun updateSignal(subId: Int, strength: SignalStrength?)
    fun updateServiceState(subId: Int, serviceState: ServiceState?)
}