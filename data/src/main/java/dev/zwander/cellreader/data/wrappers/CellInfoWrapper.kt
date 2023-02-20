package dev.zwander.cellreader.data.wrappers

import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import dev.zwander.cellreader.data.timeStampMillisCompat
import java.util.*

sealed class CellInfoWrapper {
    companion object {
        fun newInstance(info: CellInfo): CellInfoWrapper {
            return when {
                info is CellInfoGsm -> CellInfoGsmWrapper(info)
                info is CellInfoCdma -> CellInfoCdmaWrapper(info)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info is CellInfoTdscdma -> CellInfoTdscdmaWrapper(info)
                info is CellInfoWcdma -> CellInfoWcdmaWrapper(info)
                info is CellInfoLte -> CellInfoLteWrapper(info)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info is CellInfoNr -> CellInfoNrWrapper(info)
                else -> throw IllegalArgumentException("Unknown CellInfo class ${info.javaClass.canonicalName}")
            }
        }
    }

    abstract val cellIdentity: CellIdentityWrapper
    abstract val cellSignalStrength: CellSignalStrengthWrapper
    abstract val isRegistered: Boolean
    abstract val timeStamp: Long
    abstract val connectionStatus: Int

    override fun hashCode(): Int {
        return Objects.hash(
            isRegistered,
            timeStamp,
            connectionStatus,
            cellIdentity,
            cellSignalStrength
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is CellInfoWrapper
                && other.cellIdentity == cellIdentity
                && other.cellSignalStrength == cellSignalStrength
    }
}

data class CellInfoGsmWrapper(
    override var isRegistered: Boolean,
    override var timeStamp: Long,
    override var connectionStatus: Int,
    override val cellIdentity: CellIdentityGsmWrapper,
    override val cellSignalStrength: CellSignalStrengthGsmWrapper
) : CellInfoWrapper() {
    constructor(info: CellInfoGsm) : this(
        info.isRegistered,
        info.timeStampMillisCompat,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.cellConnectionStatus else CellInfo.UNAVAILABLE,
        CellIdentityGsmWrapper(info.cellIdentity),
        CellSignalStrengthGsmWrapper(info.cellSignalStrength)
    )
}

data class CellInfoCdmaWrapper(
    override var isRegistered: Boolean,
    override var timeStamp: Long,
    override var connectionStatus: Int,
    override val cellIdentity: CellIdentityCdmaWrapper,
    override val cellSignalStrength: CellSignalStrengthCdmaWrapper
) : CellInfoWrapper() {
    constructor(info: CellInfoCdma) : this(
        info.isRegistered,
        info.timeStampMillisCompat,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.cellConnectionStatus else CellInfo.UNAVAILABLE,
        CellIdentityCdmaWrapper(info.cellIdentity),
        CellSignalStrengthCdmaWrapper(info.cellSignalStrength)
    )
}

data class CellInfoTdscdmaWrapper(
    override var isRegistered: Boolean,
    override var timeStamp: Long,
    override var connectionStatus: Int,
    override val cellIdentity: CellIdentityTdscdmaWrapper,
    override val cellSignalStrength: CellSignalStrengthTdscdmaWrapper
) : CellInfoWrapper() {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(info: CellInfoTdscdma) : this(
        info.isRegistered,
        info.timeStampMillisCompat,
        info.cellConnectionStatus,
        CellIdentityTdscdmaWrapper(info.cellIdentity),
        CellSignalStrengthTdscdmaWrapper(info.cellSignalStrength)
    )
}

data class CellInfoWcdmaWrapper(
    override var isRegistered: Boolean,
    override var timeStamp: Long,
    override var connectionStatus: Int,
    override val cellIdentity: CellIdentityWcdmaWrapper,
    override val cellSignalStrength: CellSignalStrengthWcdmaWrapper
) : CellInfoWrapper() {
    constructor(info: CellInfoWcdma) : this(
        info.isRegistered,
        info.timeStampMillisCompat,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.cellConnectionStatus else CellInfo.UNAVAILABLE,
        CellIdentityWcdmaWrapper(info.cellIdentity),
        CellSignalStrengthWcdmaWrapper(info.cellSignalStrength)
    )
}

data class CellInfoLteWrapper(
    override var isRegistered: Boolean,
    override var timeStamp: Long,
    override var connectionStatus: Int,
    override val cellIdentity: CellIdentityLteWrapper,
    override val cellSignalStrength: CellSignalStrengthLteWrapper,
    val cellConfig: CellConfigLteWrapper?
) : CellInfoWrapper() {
    constructor(info: CellInfoLte) : this(
        info.isRegistered,
        info.timeStampMillisCompat,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.cellConnectionStatus else CellInfo.UNAVAILABLE,
        CellIdentityLteWrapper(info.cellIdentity),
        CellSignalStrengthLteWrapper(info.cellSignalStrength),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) CellConfigLteWrapper(info.cellConfig) else null
    )

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), cellConfig)
    }

    override fun equals(other: Any?): Boolean {
        return other is CellInfoLteWrapper
                && super.equals(other)
                && other.cellConfig == cellConfig
    }
}

data class CellInfoNrWrapper(
    override var isRegistered: Boolean,
    override var timeStamp: Long,
    override var connectionStatus: Int,
    override val cellIdentity: CellIdentityNrWrapper,
    override val cellSignalStrength: CellSignalStrengthNrWrapper
) : CellInfoWrapper() {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(info: CellInfoNr) : this(
        info.isRegistered,
        info.timeStampMillisCompat,
        info.cellConnectionStatus,
        CellIdentityNrWrapper(info.cellIdentity as CellIdentityNr),
        CellSignalStrengthNrWrapper(info.cellSignalStrength as CellSignalStrengthNr)
    )
}