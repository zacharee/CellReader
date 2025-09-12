package dev.zwander.cellreader.data.wrappers

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import dev.zwander.cellreader.data.bitErrorRateCompat
import dev.zwander.cellreader.data.isValidCompat
import java.util.*

sealed class CellSignalStrengthWrapper(val type: CellType) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : CellSignalStrengthWrapper> newInstance(strength: CellSignalStrength): T {
            return when {
                strength is CellSignalStrengthGsm -> CellSignalStrengthGsmWrapper(strength)
                strength is CellSignalStrengthCdma -> CellSignalStrengthCdmaWrapper(strength)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && strength is CellSignalStrengthTdscdma -> CellSignalStrengthTdscdmaWrapper(strength)
                strength is CellSignalStrengthWcdma -> CellSignalStrengthWcdmaWrapper(strength)
                strength is CellSignalStrengthLte -> CellSignalStrengthLteWrapper(strength)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && strength is CellSignalStrengthNr -> CellSignalStrengthNrWrapper(strength)
                else -> throw IllegalArgumentException("Unknown CellSignalStrength class ${strength.javaClass.canonicalName}")
            } as T
        }
    }

    abstract val level: Int
    abstract val dbm: Int
    abstract val valid: Boolean?
    abstract val asuLevel: Int

    abstract override fun hashCode(): Int
    abstract override fun equals(other: Any?): Boolean
}

data class CellSignalStrengthGsmWrapper(
    val rssi: Int,
    val bitErrorRate: Int,
    val timingAdvance: Int,
    override val level: Int,
    override val dbm: Int,
    override val valid: Boolean?,
    override val asuLevel: Int,
) : CellSignalStrengthWrapper(CellType.GSM) {
    @SuppressLint("NewApi")
    constructor(strength: CellSignalStrengthGsm) : this(
        rssi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) strength.rssi else CellInfo.UNAVAILABLE,
        bitErrorRate = strength.bitErrorRateCompat,
        timingAdvance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) strength.timingAdvance else CellInfo.UNAVAILABLE,
        level = strength.level,
        dbm = strength.dbm,
        valid = strength.isValidCompat,
        asuLevel = strength.asuLevel,
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

data class CellSignalStrengthCdmaWrapper(
    val cdmaDbm: Int,
    val cdmaEcio: Int,
    val evdoDbm: Int,
    val evdoEcio: Int,
    val evdoSnr: Int,
    override val level: Int,
    override val dbm: Int,
    override val valid: Boolean?,
    override val asuLevel: Int,
) : CellSignalStrengthWrapper(CellType.CDMA) {
    val evdoAsuLevel: Int
        get() {
            val evdoDbm: Int = evdoDbm
            val evdoSnr: Int = evdoSnr

            val levelEvdoDbm =
                if (evdoDbm >= -65) 16 else if (evdoDbm >= -75) 8 else if (evdoDbm >= -85) 4 else if (evdoDbm >= -95) 2 else if (evdoDbm >= -105) 1 else 99

            val levelEvdoSnr =
                if (evdoSnr >= 7) 16 else if (evdoSnr >= 6) 8 else if (evdoSnr >= 5) 4 else if (evdoSnr >= 3) 2 else if (evdoSnr >= 1) 1 else 99

            return if (levelEvdoDbm < levelEvdoSnr) levelEvdoDbm else levelEvdoSnr
        }

    constructor(strength: CellSignalStrengthCdma) : this(
        cdmaDbm = strength.cdmaDbm,
        cdmaEcio = strength.cdmaEcio,
        evdoDbm = strength.evdoDbm,
        evdoEcio = strength.evdoEcio,
        evdoSnr = strength.evdoSnr,
        level = strength.level,
        dbm = strength.dbm,
        valid = strength.isValidCompat,
        asuLevel = strength.asuLevel
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

data class CellSignalStrengthTdscdmaWrapper(
    val rssi: Int,
    val bitErrorRate: Int,
    val rscp: Int,
    override val level: Int,
    override val dbm: Int,
    override val valid: Boolean?,
    override val asuLevel: Int,
) : CellSignalStrengthWrapper(CellType.TDSCDMA) {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(strength: CellSignalStrengthTdscdma) : this(
        rssi = strength.rssi,
        bitErrorRate = strength.bitErrorRate,
        rscp = strength.rscp,
        level = strength.level,
        dbm = strength.dbm,
        valid = strength.isValidCompat,
        asuLevel = strength.asuLevel,
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

data class CellSignalStrengthWcdmaWrapper(
    val rssi: Int,
    val bitErrorRate: Int,
    val rscp: Int,
    val ecNo: Int,
    override val level: Int,
    override val dbm: Int,
    override val valid: Boolean?,
    override val asuLevel: Int,
) : CellSignalStrengthWrapper(CellType.WCDMA) {
    @SuppressLint("NewApi")
    constructor(strength: CellSignalStrengthWcdma) : this(
        rssi = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) strength.rssi else CellInfo.UNAVAILABLE,
        bitErrorRate = strength.bitErrorRateCompat,
        rscp = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) strength.rscp else CellInfo.UNAVAILABLE,
        ecNo = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) strength.ecNo else CellInfo.UNAVAILABLE,
        level = strength.level,
        dbm = strength.dbm,
        valid = strength.isValidCompat,
        asuLevel = strength.asuLevel,
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

data class CellSignalStrengthLteWrapper(
    val rssi: Int,
    val rsrp: Int,
    val rsrq: Int,
    val rssnr: Int,
    val cqiTableIndex: Int,
    val cqi: Int,
    val timingAdvance: Int,
    override val level: Int,
    override val dbm: Int,
    override val valid: Boolean?,
    override val asuLevel: Int,
) : CellSignalStrengthWrapper(CellType.LTE) {
    @SuppressLint("InlinedApi", "PrivateApi")
    constructor(strength: CellSignalStrengthLte) : this(
        rssi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) strength.rssi else CellInfo.UNAVAILABLE,
        rsrp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) strength.rsrp else strength.dbm,
        rsrq = strength.rsrq,
        rssnr = strength.rssnr,
        cqiTableIndex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) strength.cqiTableIndex else CellInfo.UNAVAILABLE,
        cqi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) strength.cqi else CellSignalStrengthLte::class.java
            .getDeclaredField("mCqi")
            .apply { isAccessible = true }
            .get(strength) as Int,
        timingAdvance = strength.timingAdvance,
        level = strength.level,
        dbm = strength.dbm,
        valid = strength.isValidCompat,
        asuLevel = strength.asuLevel,
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

data class CellSignalStrengthNrWrapper(
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
    override val valid: Boolean?,
    override val asuLevel: Int,
    val timingAdvance: Int?,
) : CellSignalStrengthWrapper(CellType.NR) {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(strength: CellSignalStrengthNr) : this(
        csiRsrp = strength.csiRsrp,
        csiRsrq = strength.csiRsrq,
        csiSinr = strength.csiSinr,
        csiCqiTableIndex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) strength.csiCqiTableIndex else CellInfo.UNAVAILABLE,
        csiCqiReport = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ArrayList(strength.csiCqiReport) else null,
        ssRsrp = strength.ssRsrp,
        ssRsrq = strength.ssRsrq,
        ssSinr = strength.ssSinr,
        level = strength.level,
        dbm = strength.dbm,
        valid = strength.isValidCompat,
        asuLevel = strength.asuLevel,
        timingAdvance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) strength.timingAdvanceMicros else null,
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
            level,
            timingAdvance,
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
                && other.timingAdvance == timingAdvance
    }
}
