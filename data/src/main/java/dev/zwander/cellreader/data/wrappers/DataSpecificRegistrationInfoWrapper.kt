package dev.zwander.cellreader.data.wrappers

import android.os.Build
import android.telephony.DataSpecificRegistrationInfo

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
        lteAttachResultType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) info.lteAttachResultType else null,
        lteAttachExtraInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) info.lteAttachExtraInfo else null,
    )
}
