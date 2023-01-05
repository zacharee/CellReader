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

    abstract val vopsSupported: Boolean
    abstract val emergencyServiceSupported: Boolean
    abstract val emergencyFallbackServiceSupported: Boolean
    abstract val emcBearerSupport: Int
}

data class LteVopsSupportInfoWrapper(
    val vopsSupport: Int,
    override val emcBearerSupport: Int
) : VopsSupportInfoWrapper() {
    override val vopsSupported: Boolean
        get() = vopsSupport != LteVopsSupportInfo.LTE_STATUS_NOT_SUPPORTED

    override val emergencyServiceSupported: Boolean
        get() = emcBearerSupport == LteVopsSupportInfo.LTE_STATUS_SUPPORTED

    override val emergencyFallbackServiceSupported: Boolean
        get() = false

    constructor(info: LteVopsSupportInfo) : this(
        info.vopsSupport,
        info.emcBearerSupport
    )
}

data class NrVopsSupportInfoWrapper(
    val vopsSupport: Int,
    override val emcBearerSupport: Int,
    val emfSupport: Int
) : VopsSupportInfoWrapper() {
    override val vopsSupported: Boolean
        get() = vopsSupport != NrVopsSupportInfo.NR_STATUS_VOPS_NOT_SUPPORTED

    override val emergencyServiceSupported: Boolean
        get() = emcBearerSupport != NrVopsSupportInfo.NR_STATUS_EMC_NOT_SUPPORTED

    override val emergencyFallbackServiceSupported: Boolean
        get() = emfSupport != NrVopsSupportInfo.NR_STATUS_EMF_NOT_SUPPORTED

    constructor(info: NrVopsSupportInfo) : this(
        info.vopsSupport,
        info.emcSupport,
        info.emfSupport
    )
}
