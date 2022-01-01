package dev.zwander.cellreader

import android.app.Application
import android.telephony.CellInfo
import android.telephony.CellSignalStrength
import android.telephony.SubscriptionInfo
import androidx.collection.ArraySet
import androidx.compose.runtime.*
import dev.zwander.cellreader.data.CellInfoWrapper
import dev.zwander.cellreader.data.CellModel
import dev.zwander.cellreader.utils.CellUtils
import org.lsposed.hiddenapibypass.HiddenApiBypass

var primaryCell by mutableStateOf(0)
val subIds = mutableStateListOf<Int>()
val sortedSubIds by derivedStateOf {
    subIds.sortedWith(CellUtils.SubsComparator(primaryCell))
}

val cellInfos = mutableStateMapOf<Int, List<CellInfo>>()
val strengthInfos = mutableStateMapOf<Int, List<CellSignalStrength>>()

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        HiddenApiBypass.setHiddenApiExemptions("")
    }
}