package dev.zwander.cellreader.data.data

import androidx.compose.runtime.*
import dev.zwander.cellreader.data.SubsComparator
import dev.zwander.cellreader.data.wrappers.CellInfoWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthWrapper
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper
import dev.zwander.cellreader.data.wrappers.SubscriptionInfoWrapper

interface ICellModel {
    var primaryCell: Int
    val subIds: MutableList<Int>
    val cellInfos: MutableMap<Int, List<CellInfoWrapper>>
    val strengthInfos: MutableMap<Int, List<CellSignalStrengthWrapper>>
    val subInfos: MutableMap<Int, SubscriptionInfoWrapper?>
    val serviceStates: MutableMap<Int, ServiceStateWrapper?>

    val sortedSubIds: List<Int>

    fun clear()
}

open class CellModelBase : ICellModel {
    override var primaryCell by mutableStateOf(0)

    override val subIds = mutableStateListOf<Int>()
    override val cellInfos = mutableStateMapOf<Int, List<CellInfoWrapper>>()
    override val strengthInfos = mutableStateMapOf<Int, List<CellSignalStrengthWrapper>>()
    override val subInfos = mutableStateMapOf<Int, SubscriptionInfoWrapper?>()
    override val serviceStates = mutableStateMapOf<Int, ServiceStateWrapper?>()

    override val sortedSubIds by derivedStateOf {
        primaryCell.let {
            subIds.sortedWith(SubsComparator(it))
        }
    }

    override fun clear() {
        primaryCell = 0

        subIds.clear()
        cellInfos.clear()
        strengthInfos.clear()
        subInfos.clear()
        serviceStates.clear()
    }
}
