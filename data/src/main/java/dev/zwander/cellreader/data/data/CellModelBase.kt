package dev.zwander.cellreader.data.data

import androidx.compose.runtime.*
import dev.zwander.cellreader.data.SubsComparator
import dev.zwander.cellreader.data.wrappers.CellInfoWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthWrapper
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper
import dev.zwander.cellreader.data.wrappers.SubscriptionInfoWrapper

abstract class CellModelBase {
    var primaryCell by mutableStateOf(0)

    val subIds = mutableStateListOf<Int>()
    val cellInfos = mutableStateMapOf<Int, List<CellInfoWrapper>>()
    val strengthInfos = mutableStateMapOf<Int, List<CellSignalStrengthWrapper>>()
    val subInfos = mutableStateMapOf<Int, SubscriptionInfoWrapper?>()
    val serviceStates = mutableStateMapOf<Int, ServiceStateWrapper?>()

    val sortedSubIds by derivedStateOf {
        primaryCell.let {
            subIds.sortedWith(SubsComparator(it))
        }
    }

    open fun clear() {
        primaryCell = 0

        subIds.clear()
        cellInfos.clear()
        strengthInfos.clear()
        subInfos.clear()
        serviceStates.clear()
    }
}
