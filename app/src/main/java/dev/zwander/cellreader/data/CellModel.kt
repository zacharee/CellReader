package dev.zwander.cellreader.data

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import dev.zwander.cellreader.utils.CellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

object CellModel {
    var primaryCell by mutableStateOf(0)

    private val subIds = mutableStateListOf<Int>()
    val cellInfos = mutableStateMapOf<Int, List<CellInfo>>()
    val strengthInfos = mutableStateMapOf<Int, List<CellSignalStrength>>()
    val signalStrengths = mutableStateMapOf<Int, SignalStrength?>()
    val subInfos = mutableStateMapOf<Int, SubscriptionInfo?>()
    val serviceStates = mutableStateMapOf<Int, ServiceState?>()
    val telephonies = mutableStateMapOf<Int, TelephonyManager>()

    val sortedSubIds by derivedStateOf {
        subIds.sortedWith(CellUtils.SubsComparator(primaryCell))
    }

    private val telephonyCallbacks = HashMap<Int, TelephonyCallback>()
    @Suppress("DEPRECATION")
    private val telephonyListeners = HashMap<Int, PhoneStateListener>()

    @SuppressLint("MissingPermission")
    fun create(
        telephony: TelephonyManager,
        subs: SubscriptionManager,
        subscriptions: List<Int>,
        listenerCallback: TelephonyListenerCallback
    ) {
        telephonies.putAll(
            subscriptions.map {
                cellInfos[it] = listOf()
                strengthInfos[it] = listOf()
                subInfos[it] = subs.getActiveSubscriptionInfo(it)
                subIds.add(it)

                it to telephony.createForSubscriptionId(it).also { telephony ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val callback = telephonyCallbacks[it] ?: TelephonyListener(it, listenerCallback).apply {
                            telephonyCallbacks[it] = this
                        }

                        telephony.registerTelephonyCallback(Dispatchers.IO.asExecutor(), callback)
                    } else {
                        val listener = telephonyListeners[it] ?: StateListener(it, listenerCallback).apply {
                            telephonyListeners[it] = this
                        }

                        @Suppress("DEPRECATION")
                        telephony.listen(
                            listener,
                            PhoneStateListener.LISTEN_SERVICE_STATE or
                                    PhoneStateListener.LISTEN_CELL_INFO or
                                    PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        )
                    }

                    listenerCallback.updateCellInfo(it, telephony.allCellInfo)
                    listenerCallback.updateSignal(it, telephony.signalStrength)
                    listenerCallback.updateServiceState(it, telephony.serviceState)
                }
            }
        )
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
private class TelephonyListener(private val subId: Int, private val listenerCallback: TelephonyListenerCallback) : TelephonyCallback(),
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
private class StateListener(private val subId: Int, private val listenerCallback: TelephonyListenerCallback) : PhoneStateListener() {
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