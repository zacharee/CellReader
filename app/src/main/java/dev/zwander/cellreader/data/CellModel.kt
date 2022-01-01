package dev.zwander.cellreader.data

import android.telephony.CellInfo
import android.telephony.CellSignalStrength
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CellModel {
    val cellInfos = mutableStateListOf<CellInfo>()
    val strengths = mutableStateListOf<CellSignalStrength>()

    operator fun component1() = cellInfos
    operator fun component2() = strengths
}