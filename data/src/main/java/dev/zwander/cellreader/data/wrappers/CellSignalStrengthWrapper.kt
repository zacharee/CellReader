package dev.zwander.cellreader.data.wrappers

import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import dev.zwander.cellreader.data.bitErrorRateCompat
import java.util.*
import kotlin.collections.ArrayList

sealed class CellSignalStrengthWrapper {
    companion object {
        fun newInstance(strength: CellSignalStrength): CellSignalStrengthWrapper {
            return when {
                strength is CellSignalStrengthGsm -> CellSignalStrengthGsmWrapper(strength)
                strength is CellSignalStrengthCdma -> CellSignalStrengthCdmaWrapper(strength)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && strength is CellSignalStrengthTdscdma -> CellSignalStrengthTdscdmaWrapper(strength)
                strength is CellSignalStrengthWcdma -> CellSignalStrengthWcdmaWrapper(strength)
                strength is CellSignalStrengthLte -> CellSignalStrengthLteWrapper(strength)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && strength is CellSignalStrengthNr -> CellSignalStrengthNrWrapper(strength)
                else -> throw IllegalArgumentException("Unknown CellSignalStrength class ${strength.javaClass.canonicalName}")
            }
        }
    }

    abstract val level: Int
    abstract val dbm: Int
    abstract val valid: Boolean

    abstract override fun hashCode(): Int
    abstract override fun equals(other: Any?): Boolean
}

class CellSignalStrengthGsmWrapper(
    val rssi: Int,
    val bitErrorRate: Int,
    val timingAdvance: Int,
    override val level: Int,
    override val dbm: Int,
    override val valid: Boolean
) : CellSignalStrengthWrapper() {
    constructor(strength: CellSignalStrengthGsm) : this(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) strength.rssi else CellInfo.UNAVAILABLE,
        strength.bitErrorRateCompat,
        strength.timingAdvance,
        strength.level,
        strength.dbm,
        strength.isValid
    )

    override fun hashCode(): Int {
        return Objects.hash(
            rssi,
            bitErrorRate,
            timingAdvance
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is CellSignalStrengthGsmWrapper
                && other.rssi == rssi
                && other.bitErrorRate == bitErrorRate
                && other.timingAdvance == timingAdvance
                && other.level == level
    }
}

class CellSignalStrengthCdmaWrapper(
    val cdmaDbm: Int,
    val cdmaEcio: Int,
    val evdoDbm: Int,
    val evdoEcio: Int,
    val evdoSnr: Int,
    override val level: Int,
    override val dbm: Int,
    override val valid: Boolean
) : CellSignalStrengthWrapper() {
    constructor(strength: CellSignalStrengthCdma) : this(
        strength.cdmaDbm,
        strength.cdmaEcio,
        strength.evdoDbm,
        strength.evdoEcio,
        strength.evdoSnr,
        strength.level,
        strength.dbm,
        strength.isValid
    )

    override fun hashCode(): Int {
        return Objects.hash(
            cdmaDbm,
            cdmaEcio,
            evdoDbm,
            evdoEcio,
            evdoSnr,
            level
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is CellSignalStrengthCdmaWrapper
                && other.cdmaDbm == cdmaDbm
                && other.cdmaEcio == cdmaEcio
                && other.evdoDbm == evdoDbm
                && other.evdoEcio == evdoEcio
                && other.evdoSnr == evdoSnr
                && other.level == level
    }
}

class CellSignalStrengthTdscdmaWrapper(
    val rssi: Int,
    val bitErrorRate: Int,
    val rscp: Int,
    override val level: Int,
    override val dbm: Int,
    override val valid: Boolean
) : CellSignalStrengthWrapper() {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(strength: CellSignalStrengthTdscdma) : this(
        strength.rssi,
        strength.bitErrorRate,
        strength.rscp,
        strength.level,
        strength.dbm,
        strength.isValid
    )

    override fun hashCode(): Int {
        return Objects.hash(
            rssi,
            bitErrorRate,
            rscp,
            level
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is CellSignalStrengthTdscdmaWrapper
                && other.rssi == rssi
                && other.bitErrorRate == bitErrorRate
                && other.rscp == rscp
                && other.level == level
    }
}

class CellSignalStrengthWcdmaWrapper(
    val rssi: Int,
    val bitErrorRate: Int,
    val rscp: Int,
    val ecNo: Int,
    override val level: Int,
    override val dbm: Int,
    override val valid: Boolean
) : CellSignalStrengthWrapper() {
    constructor(strength: CellSignalStrengthWcdma) : this(
        strength.rssi,
        strength.bitErrorRate,
        strength.rscp,
        strength.ecNo,
        strength.level,
        strength.dbm,
        strength.isValid
    )

    override fun hashCode(): Int {
        return Objects.hash(
            rssi,
            bitErrorRate,
            rscp,
            ecNo
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is CellSignalStrengthWcdmaWrapper
                && other.rssi == rssi
                && other.bitErrorRate == bitErrorRate
                && other.rscp == rscp
                && other.ecNo == ecNo
                && other.level == level
    }
}

class CellSignalStrengthLteWrapper(
    val rssi: Int,
    val rsrp: Int,
    val rsrq: Int,
    val rssnr: Int,
    val cqiTableIndex: Int,
    val cqi: Int,
    val timingAdvance: Int,
    override val level: Int,
    override val dbm: Int,
    override val valid: Boolean
) : CellSignalStrengthWrapper() {
    constructor(strength: CellSignalStrengthLte) : this(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) strength.rssi else CellInfo.UNAVAILABLE,
        strength.rsrp,
        strength.rsrq,
        strength.rssnr,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) strength.cqiTableIndex else CellInfo.UNAVAILABLE,
        strength.cqi,
        strength.timingAdvance,
        strength.level,
        strength.dbm,
        strength.isValid
    )

    override fun hashCode(): Int {
        return Objects.hash(
            rssi,
            rsrp,
            rsrq,
            rssnr,
            cqiTableIndex,
            cqi,
            timingAdvance,
            level
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is CellSignalStrengthLteWrapper
                && other.rssi == rssi
                && other.rsrp == rsrp
                && other.rsrq == rsrq
                && other.rssnr == rssnr
                && other.cqiTableIndex == cqiTableIndex
                && other.cqi == cqi
                && other.timingAdvance == timingAdvance
                && other.level == level
    }
}

class CellSignalStrengthNrWrapper(
    val csiRsrp: Int,
    val csiRsrq: Int,
    val csiSinr: Int,
    val csiCqiTableIndex: Int,
    val csiCqiReport: ArrayList<Int>?,
    val ssRsrp: Int,
    val ssRsrq: Int,
    val ssSinr: Int,
    override val level: Int,
    override val dbm: Int,
    override val valid: Boolean
) : CellSignalStrengthWrapper() {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(strength: CellSignalStrengthNr) : this(
        strength.csiRsrp,
        strength.csiRsrq,
        strength.csiSinr,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) strength.csiCqiTableIndex else CellInfo.UNAVAILABLE,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ArrayList(strength.csiCqiReport) else null,
        strength.ssRsrp,
        strength.ssRsrq,
        strength.ssSinr,
        strength.level,
        strength.dbm,
        strength.isValid
    )

    override fun hashCode(): Int {
        return Objects.hash(
            csiRsrp,
            csiRsrq,
            csiSinr,
            csiCqiTableIndex,
            csiCqiReport,
            ssRsrp,
            ssRsrq,
            ssSinr,
            level
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is CellSignalStrengthNrWrapper
                && other.csiRsrp == csiRsrp
                && other.csiRsrq == csiRsrq
                && other.csiSinr == csiSinr
                && other.csiCqiTableIndex == csiCqiTableIndex
                && other.csiCqiReport == csiCqiReport
                && other.ssRsrp == ssRsrp
                && other.ssRsrq == ssRsrq
                && other.level == level
    }
}
