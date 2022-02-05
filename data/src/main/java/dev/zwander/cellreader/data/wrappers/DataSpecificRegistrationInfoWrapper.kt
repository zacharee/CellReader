package dev.zwander.cellreader.data.wrappers

import android.telephony.DataSpecificRegistrationInfo

data class DataSpecificRegistrationInfoWrapper(
    val maxDataCalls: Int,
    val isDcNrRestricted: Boolean,
    val isNrAvailable: Boolean,
    val isEnDcAvailable: Boolean,
    val vopsSupportInfo: VopsSupportInfoWrapper?
) {
    constructor(info: DataSpecificRegistrationInfo) : this(
        info.maxDataCalls,
        info.isDcNrRestricted,
        info.isNrAvailable,
        info.isEnDcAvailable,
        info.vopsSupportInfo?.let { vops -> VopsSupportInfoWrapper.newInstance(vops) },
    )
}
