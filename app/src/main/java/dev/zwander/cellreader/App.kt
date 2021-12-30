package dev.zwander.cellreader

import android.app.Application
import android.telephony.CellInfo
import android.telephony.CellSignalStrength
import androidx.collection.ArraySet
import androidx.compose.runtime.*
import dev.zwander.cellreader.data.CellInfoWrapper
import dev.zwander.cellreader.utils.CellUtils
import org.lsposed.hiddenapibypass.HiddenApiBypass

val sortedInfos by derivedStateOf {
    cellInfos.toSortedMap(CellUtils.SubsComparator(primaryCell))
}
val cellInfos = mutableStateMapOf<Int, Pair<List<CellSignalStrength>, List<CellInfo>>>()
var primaryCell by mutableStateOf(0)

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        HiddenApiBypass.setHiddenApiExemptions("")


    }
}