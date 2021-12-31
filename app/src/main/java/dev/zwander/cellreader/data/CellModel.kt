package dev.zwander.cellreader.data

import android.telephony.CellInfo
import android.telephony.CellSignalStrength
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CellModel {
    var cellInfos by mutableStateOf(listOf<CellInfo>())
    var strengths by mutableStateOf(listOf<CellSignalStrength>())

    operator fun component1() = cellInfos
    operator fun component2() = strengths
}