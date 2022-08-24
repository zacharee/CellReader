package dev.zwander.cellreader.data.data

import android.telephony.SignalStrength
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.MutableLiveData
import dev.zwander.cellreader.data.SubsComparator
import dev.zwander.cellreader.data.util.UpdatableTreeSet
import dev.zwander.cellreader.data.wrappers.*
import kotlin.collections.HashMap

interface ICellModel {
    val primaryCell: MutableLiveData<Int>
    val subIds: MutableLiveData<UpdatableTreeSet<Int>>
    val cellInfos: MutableLiveData<HashMap<Int, List<CellInfoWrapper>>>
    val strengthInfos: MutableLiveData<HashMap<Int, List<CellSignalStrengthWrapper>>>
    val subInfos: MutableLiveData<HashMap<Int, SubscriptionInfoWrapper?>>
    val serviceStates: MutableLiveData<HashMap<Int, ServiceStateWrapper?>>
    val displayInfos: MutableLiveData<HashMap<Int, TelephonyDisplayInfoWrapper?>>
    val dataConnectionStates: MutableLiveData<HashMap<Int, DataConnectionState>>

    fun clear()
}

abstract class CellModelBase : ICellModel {
    override val primaryCell = MutableLiveData(0)

    override val subIds by lazy { MutableLiveData(UpdatableTreeSet(SubsComparator(primaryCell.value!!))) }
    override val cellInfos = MutableLiveData<HashMap<Int, List<CellInfoWrapper>>>(hashMapOf())
    override val strengthInfos = MutableLiveData<HashMap<Int, List<CellSignalStrengthWrapper>>>(hashMapOf())
    override val subInfos = MutableLiveData<HashMap<Int, SubscriptionInfoWrapper?>>(hashMapOf())
    override val serviceStates = MutableLiveData<HashMap<Int, ServiceStateWrapper?>>(hashMapOf())
    override val displayInfos = MutableLiveData<HashMap<Int, TelephonyDisplayInfoWrapper?>>(hashMapOf())
    override val dataConnectionStates = MutableLiveData<HashMap<Int, DataConnectionState>>(hashMapOf())

    val LocalSubIds = compositionLocalOf { subIds }
    val LocalCellInfos = compositionLocalOf { cellInfos }
    val LocalStrengthInfos = compositionLocalOf { strengthInfos }
    val LocalSubInfos = compositionLocalOf { subInfos }
    val LocalServiceStates = compositionLocalOf { serviceStates }
    val LocalDisplayInfos = compositionLocalOf { displayInfos }
    val LocalDataConnectionStates = compositionLocalOf { dataConnectionStates }
    open val LocalSignalStrengths = compositionLocalOf<MutableLiveData<HashMap<Int, SignalStrength?>>?> { null }

    override fun clear() {
        primaryCell.value = 0

        subIds.value = UpdatableTreeSet(SubsComparator(primaryCell.value!!))
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
