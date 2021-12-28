package dev.zwander.cellreader.data

import android.telephony.CellInfo

data class CellInfoWrapper(
    val subId: Int,
    val infos: List<CellInfo>
)