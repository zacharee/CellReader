package dev.zwander.cellreader

import android.app.Application
import android.os.Build
import android.telephony.*
import androidx.compose.runtime.*
import dev.zwander.cellreader.utils.CellUtils
import org.lsposed.hiddenapibypass.HiddenApiBypass

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

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        HiddenApiBypass.setHiddenApiExemptions("")
    }
}