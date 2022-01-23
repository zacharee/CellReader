package dev.zwander.cellreader.data.wrappers

import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import dev.zwander.cellreader.data.timeStampMillisCompat
import java.util.*

sealed class CellInfoWrapper(
    var isRegistered: Boolean,
    var timeStamp: Long,
    var connectionStatus: Int
) {
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
        return other is CellInfoGsmWrapper
                && other.cellIdentity == cellIdentity
                && other.cellSignalStrength == cellSignalStrength
    }
}

class CellInfoGsmWrapper(
    isRegistered: Boolean,
    timeStamp: Long,
    connectionStatus: Int,
    override val cellIdentity: CellIdentityGsmWrapper,
    override val cellSignalStrength: CellSignalStrengthGsmWrapper
) : CellInfoWrapper(
    isRegistered,
    timeStamp,
    connectionStatus
) {
    constructor(info: CellInfoGsm) : this(
        info.isRegistered,
        info.timeStampMillisCompat,
        info.cellConnectionStatus,
        CellIdentityGsmWrapper(info.cellIdentity),
        CellSignalStrengthGsmWrapper(info.cellSignalStrength)
    )
}

class CellInfoCdmaWrapper(
    isRegistered: Boolean,
    timeStamp: Long,
    connectionStatus: Int,
    override val cellIdentity: CellIdentityCdmaWrapper,
    override val cellSignalStrength: CellSignalStrengthCdmaWrapper
) : CellInfoWrapper(
    isRegistered, timeStamp, connectionStatus
) {
    constructor(info: CellInfoCdma) : this(
        info.isRegistered,
        info.timeStampMillisCompat,
        info.cellConnectionStatus,
        CellIdentityCdmaWrapper(info.cellIdentity),
        CellSignalStrengthCdmaWrapper(info.cellSignalStrength)
    )
}

class CellInfoTdscdmaWrapper(
    isRegistered: Boolean,
    timeStamp: Long,
    connectionStatus: Int,
    override val cellIdentity: CellIdentityTdscdmaWrapper,
    override val cellSignalStrength: CellSignalStrengthTdscdmaWrapper
) : CellInfoWrapper(
    isRegistered, timeStamp, connectionStatus
) {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(info: CellInfoTdscdma) : this(
        info.isRegistered,
        info.timeStampMillisCompat,
        info.cellConnectionStatus,
        CellIdentityTdscdmaWrapper(info.cellIdentity),
        CellSignalStrengthTdscdmaWrapper(info.cellSignalStrength)
    )
}

class CellInfoWcdmaWrapper(
    isRegistered: Boolean,
    timeStamp: Long,
    connectionStatus: Int,
    override val cellIdentity: CellIdentityWcdmaWrapper,
    override val cellSignalStrength: CellSignalStrengthWcdmaWrapper
) : CellInfoWrapper(
    isRegistered, timeStamp, connectionStatus
) {
    constructor(info: CellInfoWcdma) : this(
        info.isRegistered,
        info.timeStampMillisCompat,
        info.cellConnectionStatus,
        CellIdentityWcdmaWrapper(info.cellIdentity),
        CellSignalStrengthWcdmaWrapper(info.cellSignalStrength)
    )
}

class CellInfoLteWrapper(
    isRegistered: Boolean,
    timeStamp: Long,
    connectionStatus: Int,
    override val cellIdentity: CellIdentityLteWrapper,
    override val cellSignalStrength: CellSignalStrengthLteWrapper,
    val cellConfig: CellConfigLteWrapper
) : CellInfoWrapper(
    isRegistered, timeStamp, connectionStatus
) {
    constructor(info: CellInfoLte) : this(
        info.isRegistered,
        info.timeStampMillisCompat,
        info.cellConnectionStatus,
        CellIdentityLteWrapper(info.cellIdentity),
        CellSignalStrengthLteWrapper(info.cellSignalStrength),
        CellConfigLteWrapper(info.cellConfig)
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

class CellInfoNrWrapper(
    isRegistered: Boolean,
    timeStamp: Long,
    connectionStatus: Int,
    override val cellIdentity: CellIdentityNrWrapper,
    override val cellSignalStrength: CellSignalStrengthNrWrapper
) : CellInfoWrapper(
    isRegistered, timeStamp, connectionStatus
) {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(info: CellInfoNr) : this(
        info.isRegistered,
        info.timeStampMillisCompat,
        info.cellConnectionStatus,
        CellIdentityNrWrapper(info.cellIdentity as CellIdentityNr),
        CellSignalStrengthNrWrapper(info.cellSignalStrength as CellSignalStrengthNr)
    )
}