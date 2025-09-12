package dev.zwander.cellreader.data.wrappers

import android.telephony.CellConfigLte
import dev.zwander.cellreader.data.endcAvailable

data class CellConfigLteWrapper(
    val endcAvailable: Boolean
) {
    constructor(config: CellConfigLte) : this(endcAvailable = config.endcAvailable)
}