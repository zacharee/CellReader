package dev.zwander.cellreader.data.data

import androidx.lifecycle.MutableLiveData
import dev.zwander.cellreader.data.SubsComparator
import dev.zwander.cellreader.data.wrappers.CellInfoWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthWrapper
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper
import dev.zwander.cellreader.data.wrappers.SubscriptionInfoWrapper
import java.util.*

interface ICellModel {
    val primaryCell: MutableLiveData<Int>
    val subIds: MutableLiveData<TreeSet<Int>>
    val cellInfos: MutableLiveData<HashMap<Int, List<CellInfoWrapper>>>
    val strengthInfos: MutableLiveData<HashMap<Int, List<CellSignalStrengthWrapper>>>
    val subInfos: MutableLiveData<HashMap<Int, SubscriptionInfoWrapper?>>
    val serviceStates: MutableLiveData<HashMap<Int, ServiceStateWrapper?>>

    fun clear()
}

open class CellModelBase : ICellModel {
    override val primaryCell = MutableLiveData(0)

    override val subIds by lazy { MutableLiveData<TreeSet<Int>>(TreeSet(SubsComparator(primaryCell.value!!))) }
    override val cellInfos = MutableLiveData<HashMap<Int, List<CellInfoWrapper>>>(hashMapOf())
    override val strengthInfos = MutableLiveData<HashMap<Int, List<CellSignalStrengthWrapper>>>(hashMapOf())
    override val subInfos = MutableLiveData<HashMap<Int, SubscriptionInfoWrapper?>>(hashMapOf())
    override val serviceStates = MutableLiveData<HashMap<Int, ServiceStateWrapper?>>(hashMapOf())

    override fun clear() {
        primaryCell.value = 0

        subIds.value = TreeSet(SubsComparator(primaryCell.value!!))
        cellInfos.value = hashMapOf()
        strengthInfos.value = hashMapOf()
        subInfos.value = hashMapOf()
        serviceStates.value = hashMapOf()
    }
}
