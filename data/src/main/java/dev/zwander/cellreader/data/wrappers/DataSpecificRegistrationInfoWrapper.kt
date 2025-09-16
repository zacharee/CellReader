package dev.zwander.cellreader.data.wrappers

import android.os.Build
import android.telephony.DataSpecificRegistrationInfo
import dev.zwander.cellreader.data.util.withMinApi

data class DataSpecificRegistrationInfoWrapper(
    val maxDataCalls: Int,
    val isDcNrRestricted: Boolean,
    val isNrAvailable: Boolean,
    val isEnDcAvailable: Boolean,
    val vopsSupportInfo: VopsSupportInfoWrapper?,
    val lteAttachResultType: Int?,
    val lteAttachExtraInfo: Int?,
) {
    constructor(info: DataSpecificRegistrationInfo) : this(
        maxDataCalls = info.maxDataCalls,
        isDcNrRestricted = info.isDcNrRestricted,
        isNrAvailable = info.isNrAvailable,
        isEnDcAvailable = info.isEnDcAvailable,
        vopsSupportInfo = try {
            info.vopsSupportInfo?.let { vops -> VopsSupportInfoWrapper.newInstance(vops) }
        } catch (_: Throwable) {
            null
        },
        lteAttachResultType = withMinApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            info.lteAttachResultType
        },
        lteAttachExtraInfo = withMinApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            info.lteAttachExtraInfo
        },
    )
}
