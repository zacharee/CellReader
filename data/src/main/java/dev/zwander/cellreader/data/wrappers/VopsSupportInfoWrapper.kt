package dev.zwander.cellreader.data.wrappers

import android.os.Build
import android.telephony.LteVopsSupportInfo
import android.telephony.NrVopsSupportInfo
import android.telephony.VopsSupportInfo

sealed class VopsSupportInfoWrapper {
    companion object {
        fun newInstance(info: VopsSupportInfo): VopsSupportInfoWrapper {
            return when {
                info is LteVopsSupportInfo -> LteVopsSupportInfoWrapper(info)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info is NrVopsSupportInfo -> NrVopsSupportInfoWrapper(info)
                else -> throw IllegalArgumentException("Unknown VopsSupportInfo class ${info.javaClass.canonicalName}")
            }
        }
    }
}

data class LteVopsSupportInfoWrapper(
    val vopsSupport: Int,
    val emcBearerSupport: Int
) : VopsSupportInfoWrapper() {
    constructor(info: LteVopsSupportInfo) : this(
        info.vopsSupport,
        info.emcBearerSupport
    )
}

data class NrVopsSupportInfoWrapper(
    val vopsSUpport: Int,
    val emcSupport: Int,
    val emfSupport: Int
) : VopsSupportInfoWrapper() {
    constructor(info: NrVopsSupportInfo) : this(
        info.vopsSupport,
        info.emcSupport,
        info.emfSupport
    )
}
