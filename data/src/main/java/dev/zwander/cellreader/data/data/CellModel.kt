package dev.zwander.cellreader.data.data

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import dev.zwander.cellreader.data.SubsComparator

object CellModel {
    var primaryCell by mutableStateOf(0)

    val subIds = mutableStateListOf<Int>()
    val cellInfos = mutableStateMapOf<Int, List<CellInfo>>()
    val strengthInfos = mutableStateMapOf<Int, List<CellSignalStrength>>()
    val signalStrengths = mutableStateMapOf<Int, SignalStrength?>()
    val subInfos = mutableStateMapOf<Int, SubscriptionInfo?>()
    val serviceStates = mutableStateMapOf<Int, ServiceState?>()
    val telephonies = mutableStateMapOf<Int, TelephonyManager>()

    val sortedSubIds by derivedStateOf {
        subIds.sortedWith(SubsComparator(primaryCell))
    }

    val telephonyCallbacks = HashMap<Int, TelephonyCallback>()
    @Suppress("DEPRECATION")
    val telephonyListeners = HashMap<Int, PhoneStateListener>()

    @SuppressLint("MissingPermission")
    fun create(
        telephony: TelephonyManager,
        subs: SubscriptionManager,
        subscriptions: List<Int>,
        listenerCallback: TelephonyListenerCallback
    ) {

    }

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

    private fun clear() {
        primaryCell = 0

        subIds.clear()
        cellInfos.clear()
        strengthInfos.clear()
        signalStrengths.clear()
        subInfos.clear()
        serviceStates.clear()
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

@Composable
fun ProvideCellModel(block: @Composable() CellModel.() -> Unit) {
    CellModel.block()
}