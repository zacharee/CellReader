package dev.zwander.cellreader.data.data

import android.telephony.SubscriptionInfo
import androidx.compose.runtime.*
import dev.zwander.cellreader.data.SubsComparator
import dev.zwander.cellreader.data.wrappers.CellInfoWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthWrapper
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper
import dev.zwander.cellreader.data.wrappers.SubscriptionInfoWrapper

object CellModelWear {
    var primaryCell by mutableStateOf(0)

    val subIds = mutableStateListOf<Int>()
    val cellInfos = mutableStateMapOf<Int, List<CellInfoWrapper>>()
    val strengthInfos = mutableStateMapOf<Int, List<CellSignalStrengthWrapper>>()
//    val signalStrengths = mutableStateMapOf<Int, SignalStrength?>()
    val subInfos = mutableStateMapOf<Int, SubscriptionInfoWrapper?>()
    val serviceStates = mutableStateMapOf<Int, ServiceStateWrapper?>()

    val sortedSubIds by derivedStateOf {
        subIds.sortedWith(SubsComparator(primaryCell))
    }

    fun clear() {
        primaryCell = 0

        subIds.clear()
        cellInfos.clear()
        strengthInfos.clear()
//        signalStrengths.clear()
        subInfos.clear()
        serviceStates.clear()
    }
}

@Composable
fun ProvideCellModelWear(block: @Composable() CellModelWear.() -> Unit) {
    CellModelWear.block()
}