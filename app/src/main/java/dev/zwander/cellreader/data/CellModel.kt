package dev.zwander.cellreader.data

import android.telephony.*
import androidx.compose.runtime.*
import dev.zwander.cellreader.utils.CellUtils

object CellModel {
    var primaryCell by mutableStateOf(0)
    val subIds = mutableStateListOf<Int>()
    val sortedSubIds by derivedStateOf {
        subIds.sortedWith(CellUtils.SubsComparator(primaryCell))
    }

    val cellInfos = mutableStateMapOf<Int, List<CellInfo>>()
    val strengthInfos = mutableStateMapOf<Int, List<CellSignalStrength>>()
    val signalStrengths = mutableStateMapOf<Int, SignalStrength?>()
    val subInfos = mutableStateMapOf<Int, SubscriptionInfo?>()
    val serviceStates = mutableStateMapOf<Int, ServiceState?>()
    val telephonies = mutableStateMapOf<Int, TelephonyManager>()
}

@Composable
fun ProvideCellModel(block: @Composable() CellModel.() -> Unit) {
    CellModel.block()
}