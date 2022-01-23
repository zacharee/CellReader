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

    class CellInfoGsmWrapper(
        isRegistered: Boolean,
        timeStamp: Long,
        connectionStatus: Int,
        override val cellIdentity: CellIdentityWrapper.CellIdentityGsmWrapper,
        override val cellSignalStrength: CellSignalStrengthWrapper.CellSignalStrengthGsmWrapper
    ) : CellInfoWrapper(
        isRegistered,
        timeStamp,
        connectionStatus
    ) {
        constructor(info: CellInfoGsm) : this(
            info.isRegistered,
            info.timeStampMillisCompat,
            info.cellConnectionStatus,
            CellIdentityWrapper.CellIdentityGsmWrapper(info.cellIdentity),
            CellSignalStrengthWrapper.CellSignalStrengthGsmWrapper(info.cellSignalStrength)
        )
    }

    class CellInfoCdmaWrapper(
        isRegistered: Boolean,
        timeStamp: Long,
        connectionStatus: Int,
        override val cellIdentity: CellIdentityWrapper.CellIdentityCdmaWrapper,
        override val cellSignalStrength: CellSignalStrengthWrapper.CellSignalStrengthCdmaWrapper
    ) : CellInfoWrapper(
        isRegistered, timeStamp, connectionStatus
    ) {
        constructor(info: CellInfoCdma) : this(
            info.isRegistered,
            info.timeStampMillisCompat,
            info.cellConnectionStatus,
            CellIdentityWrapper.CellIdentityCdmaWrapper(info.cellIdentity),
            CellSignalStrengthWrapper.CellSignalStrengthCdmaWrapper(info.cellSignalStrength)
        )
    }

    class CellInfoTdscdmaWrapper(
        isRegistered: Boolean,
        timeStamp: Long,
        connectionStatus: Int,
        override val cellIdentity: CellIdentityWrapper.CellIdentityTdscdmaWrapper,
        override val cellSignalStrength: CellSignalStrengthWrapper.CellSignalStrengthTdscdmaWrapper
    ) : CellInfoWrapper(
        isRegistered, timeStamp, connectionStatus
    ) {
        @RequiresApi(Build.VERSION_CODES.Q)
        constructor(info: CellInfoTdscdma) : this(
            info.isRegistered,
            info.timeStampMillisCompat,
            info.cellConnectionStatus,
            CellIdentityWrapper.CellIdentityTdscdmaWrapper(info.cellIdentity),
            CellSignalStrengthWrapper.CellSignalStrengthTdscdmaWrapper(info.cellSignalStrength)
        )
    }

    class CellInfoWcdmaWrapper(
        isRegistered: Boolean,
        timeStamp: Long,
        connectionStatus: Int,
        override val cellIdentity: CellIdentityWrapper.CellIdentityWcdmaWrapper,
        override val cellSignalStrength: CellSignalStrengthWrapper.CellSignalStrengthWcdmaWrapper
    ) : CellInfoWrapper(
        isRegistered, timeStamp, connectionStatus
    ) {
        constructor(info: CellInfoWcdma) : this(
            info.isRegistered,
            info.timeStampMillisCompat,
            info.cellConnectionStatus,
            CellIdentityWrapper.CellIdentityWcdmaWrapper(info.cellIdentity),
            CellSignalStrengthWrapper.CellSignalStrengthWcdmaWrapper(info.cellSignalStrength)
        )
    }

    class CellInfoLteWrapper(
        isRegistered: Boolean,
        timeStamp: Long,
        connectionStatus: Int,
        override val cellIdentity: CellIdentityWrapper.CellIdentityLteWrapper,
        override val cellSignalStrength: CellSignalStrengthWrapper.CellSignalStrengthLteWrapper,
        val cellConfig: CellConfigLteWrapper
    ) : CellInfoWrapper(
        isRegistered, timeStamp, connectionStatus
    ) {
        constructor(info: CellInfoLte) : this(
            info.isRegistered,
            info.timeStampMillisCompat,
            info.cellConnectionStatus,
            CellIdentityWrapper.CellIdentityLteWrapper(info.cellIdentity),
            CellSignalStrengthWrapper.CellSignalStrengthLteWrapper(info.cellSignalStrength),
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
        override val cellIdentity: CellIdentityWrapper.CellIdentityNrWrapper,
        override val cellSignalStrength: CellSignalStrengthWrapper.CellSignalStrengthNrWrapper
    ) : CellInfoWrapper(
        isRegistered, timeStamp, connectionStatus
    ) {
        @RequiresApi(Build.VERSION_CODES.Q)
        constructor(info: CellInfoNr) : this(
            info.isRegistered,
            info.timeStampMillisCompat,
            info.cellConnectionStatus,
            CellIdentityWrapper.CellIdentityNrWrapper(info.cellIdentity as CellIdentityNr),
            CellSignalStrengthWrapper.CellSignalStrengthNrWrapper(info.cellSignalStrength as CellSignalStrengthNr)
        )
    }
}