package dev.zwander.cellreader

import android.app.Application
import android.telephony.CellInfo
import androidx.compose.runtime.*
import org.lsposed.hiddenapibypass.HiddenApiBypass

val cellInfos = mutableStateMapOf<Int, List<CellInfo>>()
var primaryCell by mutableStateOf(0)

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        HiddenApiBypass.setHiddenApiExemptions("")


    }
}