package dev.zwander.cellreader.data.data

import android.telephony.SignalStrength
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import dev.zwander.cellreader.data.SubsComparator
import dev.zwander.cellreader.data.util.UpdatableTreeSet
import dev.zwander.cellreader.data.wrappers.CellInfoWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthWrapper
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper
import dev.zwander.cellreader.data.wrappers.SubscriptionInfoWrapper
import dev.zwander.cellreader.data.wrappers.TelephonyDisplayInfoWrapper
import kotlinx.coroutines.flow.MutableStateFlow

interface ICellModel {
    val primaryCell: MutableStateFlow<Int>
    val subIds: MutableStateFlow<UpdatableTreeSet<Int>>
    val cellInfos: MutableStateFlow<HashMap<Int, List<CellInfoWrapper>>>
    val strengthInfos: MutableStateFlow<HashMap<Int, List<CellSignalStrengthWrapper>>>
    val subInfos: MutableStateFlow<HashMap<Int, SubscriptionInfoWrapper?>>
    val serviceStates: MutableStateFlow<HashMap<Int, ServiceStateWrapper?>>
    val displayInfos: MutableStateFlow<HashMap<Int, TelephonyDisplayInfoWrapper?>>
    val dataConnectionStates: MutableStateFlow<HashMap<Int, DataConnectionState>>

    fun clear()
}

abstract class CellModelBase : ICellModel {
    override val primaryCell = MutableStateFlow(0)

    override val subIds by lazy { MutableStateFlow(UpdatableTreeSet(SubsComparator(primaryCell.value))) }
    override val cellInfos = MutableStateFlow<HashMap<Int, List<CellInfoWrapper>>>(hashMapOf())
    override val strengthInfos = MutableStateFlow<HashMap<Int, List<CellSignalStrengthWrapper>>>(hashMapOf())
    override val subInfos = MutableStateFlow<HashMap<Int, SubscriptionInfoWrapper?>>(hashMapOf())
    override val serviceStates = MutableStateFlow<HashMap<Int, ServiceStateWrapper?>>(hashMapOf())
    override val displayInfos = MutableStateFlow<HashMap<Int, TelephonyDisplayInfoWrapper?>>(hashMapOf())
    override val dataConnectionStates = MutableStateFlow<HashMap<Int, DataConnectionState>>(hashMapOf())

    open val signalStrengths: MutableStateFlow<HashMap<Int, SignalStrength?>>? = null

    override fun clear() {
        primaryCell.value = 0

        subIds.value = UpdatableTreeSet(SubsComparator(primaryCell.value))
        cellInfos.value = hashMapOf()
        strengthInfos.value = hashMapOf()
        subInfos.value = hashMapOf()
        serviceStates.value = hashMapOf()
        dataConnectionStates.value = hashMapOf()
    }

    @Composable
    fun ProvideCellModel(content: @Composable () -> Unit) {
        CompositionLocalProvider(
            LocalCellModel provides this
        ) {
            content()
        }
    }
}

val LocalCellModel = compositionLocalOf<CellModelBase> { error("No CellModel provided") }
