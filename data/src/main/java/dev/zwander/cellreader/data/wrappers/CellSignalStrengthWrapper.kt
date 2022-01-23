package dev.zwander.cellreader.data.wrappers

import android.telephony.*
import java.util.*
import kotlin.collections.ArrayList

sealed class CellSignalStrengthWrapper {
    abstract val level: Int
    abstract val dbm: Int
    abstract val valid: Boolean

    abstract override fun hashCode(): Int
    abstract override fun equals(other: Any?): Boolean

    class CellSignalStrengthGsmWrapper(
        val rssi: Int,
        val bitErrorRate: Int,
        val timingAdvance: Int,
        override val level: Int,
        override val dbm: Int,
        override val valid: Boolean
    ) : CellSignalStrengthWrapper() {
        constructor(strength: CellSignalStrengthGsm) : this(
            strength.rssi,
            strength.bitErrorRate,
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
            strength.rssi,
            strength.rsrp,
            strength.rsrq,
            strength.rssnr,
            strength.cqiTableIndex,
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
        val csiCqiReport: ArrayList<Int>,
        val ssRsrp: Int,
        val ssRsrq: Int,
        val ssSinr: Int,
        override val level: Int,
        override val dbm: Int,
        override val valid: Boolean
    ) : CellSignalStrengthWrapper() {
        constructor(strength: CellSignalStrengthNr) : this(
            strength.csiRsrp,
            strength.csiRsrq,
            strength.csiSinr,
            strength.csiCqiTableIndex,
            ArrayList(strength.csiCqiReport),
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
}
