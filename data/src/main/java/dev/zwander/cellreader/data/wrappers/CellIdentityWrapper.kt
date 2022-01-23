package dev.zwander.cellreader.data.wrappers

import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.collections.ArrayList

sealed class CellIdentityWrapper(
    val type: Int,
    val mcc: String?,
    val mnc: String?,
    val alphaLong: String?,
    val alphaShort: String?,
    val globalCellId: String?,
    val channelNumber: Int,
) {
    abstract override fun hashCode(): Int
    abstract override fun equals(other: Any?): Boolean

    class CellIdentityGsmWrapper(
        val lac: Int,
        val cid: Int,
        val arfcn: Int,
        val bsic: Int,
        val additionalPlmns: ArrayList<String>?,
        mcc: String?,
        mnc: String?,
        alphaLong: String?,
        alphaShort: String?,
        globalCellId: String?,
        channelNumber: Int
    ) : CellIdentityWrapper(
        CellInfo.TYPE_GSM, mcc, mnc, alphaLong,
        alphaShort, globalCellId, channelNumber
    ) {
        constructor(identity: CellIdentityGsm) : this(
            identity.lac,
            identity.cid,
            identity.arfcn,
            identity.bsic,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ArrayList(identity.additionalPlmns)
            } else {
                null
            },
            identity.mccString,
            identity.mncString,
            identity.operatorAlphaLong.toString(),
            identity.operatorAlphaShort.toString(),
            identity.globalCellId,
            identity.channelNumber
        )

        override fun hashCode(): Int {
            return Objects.hash(
                lac, cid,
                additionalPlmns,
                mcc, mnc,
                alphaLong, alphaShort
            )
        }

        override fun equals(other: Any?): Boolean {
            return other is CellIdentityGsmWrapper
                    && other.lac == lac
                    && other.cid == cid
                    && other.arfcn == arfcn
                    && other.bsic == bsic
                    && other.mcc == mcc
                    && other.mnc == mnc
                    && other.additionalPlmns == additionalPlmns
                    && other.type == type
                    && other.alphaLong == alphaLong
                    && other.alphaShort == alphaShort
        }
    }

    class CellIdentityCdmaWrapper(
        val networkId: Int,
        val systemId: Int,
        val basestationId: Int,
        val longitude: Int,
        val latitude: Int,
        alphaLong: String?,
        alphaShort: String?,
        globalCellId: String?,
        channelNumber: Int
    ) : CellIdentityWrapper(
        CellInfo.TYPE_CDMA, null, null,
        alphaLong, alphaShort, globalCellId, channelNumber
    ) {
        constructor(identity: CellIdentityCdma) : this(
            identity.networkId,
            identity.systemId,
            identity.basestationId,
            identity.longitude,
            identity.latitude,
            identity.operatorAlphaLong.toString(),
            identity.operatorAlphaShort.toString(),
            identity.globalCellId,
            identity.channelNumber
        )

        override fun hashCode(): Int {
            return Objects.hash(
                networkId,
                systemId,
                basestationId,
                latitude,
                longitude,
                alphaLong, alphaShort
            )
        }

        override fun equals(other: Any?): Boolean {
            return other is CellIdentityCdmaWrapper
                    && other.networkId == networkId
                    && other.systemId == systemId
                    && other.basestationId == basestationId
                    && other.latitude == latitude
                    && other.longitude == longitude
                    && other.alphaLong == alphaLong
                    && other.alphaShort == alphaShort
        }
    }

    class CellIdentityTdscdmaWrapper(
        val lac: Int,
        val cid: Int,
        val cpid: Int,
        val uarfcn: Int,
        val additionalPlmns: ArrayList<String>?,
        val csgInfo: ClosedSubscriberGroupInfoWrapper?,
        mcc: String?,
        mnc: String?,
        alphaLong: String?,
        alphaShort: String?,
        globalCellId: String?,
        channelNumber: Int
    ) : CellIdentityWrapper(
        CellInfo.TYPE_TDSCDMA, mcc, mnc, alphaLong,
        alphaShort, globalCellId, channelNumber
    ) {
        constructor(identity: CellIdentityTdscdma) : this(
            identity.lac,
            identity.cid,
            identity.cpid,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                identity.uarfcn
            } else {
                -1
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ArrayList(identity.additionalPlmns)
            } else {
                null
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ClosedSubscriberGroupInfoWrapper(identity.closedSubscriberGroupInfo)
            } else {
                null
            },
            identity.mccString,
            identity.mncString,
            identity.operatorAlphaLong.toString(),
            identity.operatorAlphaShort.toString(),
            identity.globalCellId,
            identity.channelNumber
        )

        override fun hashCode(): Int {
            return Objects.hash(
                lac,
                cid,
                cpid,
                uarfcn,
                additionalPlmns,
                csgInfo,
                mcc, mnc, alphaLong, alphaShort,
                globalCellId, channelNumber
            )
        }

        override fun equals(other: Any?): Boolean {
            return other is CellIdentityTdscdmaWrapper
                    && other.lac == lac
                    && other.cid == cid
                    && other.cpid == cpid
                    && other.uarfcn == uarfcn
                    && other.additionalPlmns == additionalPlmns
                    && other.csgInfo == csgInfo
                    && other.mcc == mcc
                    && other.mnc == mnc
                    && other.alphaLong == alphaLong
                    && other.alphaShort == alphaShort
                    && other.globalCellId == globalCellId
                    && other.channelNumber == channelNumber
        }
    }

    class CellIdentityWcdmaWrapper(
        val lac: Int,
        val cid: Int,
        val psc: Int,
        val uarfcn: Int,
        val additionalPlmns: ArrayList<String>?,
        val csgInfo: ClosedSubscriberGroupInfoWrapper?,
        mcc: String?,
        mnc: String?,
        alphaLong: String?,
        alphaShort: String?,
        globalCellId: String?,
        channelNumber: Int
    ) : CellIdentityWrapper(
        CellInfo.TYPE_WCDMA, mcc, mnc, alphaLong,
        alphaShort, globalCellId, channelNumber
    ) {
        constructor(identity: CellIdentityWcdma) : this(
            identity.lac,
            identity.cid,
            identity.psc,
            identity.uarfcn,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ArrayList(identity.additionalPlmns)
            } else {
                null
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ClosedSubscriberGroupInfoWrapper(identity.closedSubscriberGroupInfo)
            } else {
                null
            },
            identity.mccString,
            identity.mncString,
            identity.operatorAlphaLong.toString(),
            identity.operatorAlphaShort.toString(),
            identity.globalCellId,
            identity.channelNumber
        )

        override fun hashCode(): Int {
            return Objects.hash(
                lac,
                cid,
                psc,
                uarfcn,
                additionalPlmns,
                csgInfo,
                mcc, mnc, alphaLong, alphaShort,
                globalCellId, channelNumber
            )
        }

        override fun equals(other: Any?): Boolean {
            return other is CellIdentityWcdmaWrapper
                    && other.lac == lac
                    && other.cid == cid
                    && other.psc == psc
                    && other.uarfcn == uarfcn
                    && other.additionalPlmns == additionalPlmns
                    && other.csgInfo == csgInfo
                    && other.mcc == mcc
                    && other.mnc == mnc
                    && other.alphaLong == alphaLong
                    && other.alphaShort == alphaShort
                    && other.globalCellId == globalCellId
                    && other.channelNumber == channelNumber
        }
    }

    class CellIdentityLteWrapper(
        val ci: Int,
        val pci: Int,
        val tac: Int,
        val earfcn: Int,
        val bandwidth: Int,
        val bands: ArrayList<Int>?,
        val additionalPlmns: ArrayList<String>?,
        val csgInfo: ClosedSubscriberGroupInfoWrapper?,
        mcc: String?,
        mnc: String?,
        alphaLong: String?,
        alphaShort: String?,
        globalCellId: String?,
        channelNumber: Int
    ) : CellIdentityWrapper(
        CellInfo.TYPE_LTE, mcc, mnc, alphaLong,
        alphaShort, globalCellId, channelNumber
    ) {
        constructor(identity: CellIdentityLte) : this(
            identity.ci,
            identity.pci,
            identity.tac,
            identity.earfcn,
            identity.bandwidth,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ArrayList(identity.bands.toList())
            } else {
                null
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ArrayList(identity.additionalPlmns)
            } else {
                null
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ClosedSubscriberGroupInfoWrapper(identity.closedSubscriberGroupInfo)
            } else {
                null
            },
            identity.mccString,
            identity.mncString,
            identity.operatorAlphaLong.toString(),
            identity.operatorAlphaShort.toString(),
            identity.globalCellId,
            identity.channelNumber
        )

        override fun hashCode(): Int {
            return Objects.hash(
                ci,
                pci,
                tac,
                earfcn,
                additionalPlmns,
                csgInfo,
                bandwidth,
                bands,
                mcc, mnc, alphaLong, alphaShort,
                globalCellId, channelNumber
            )
        }

        override fun equals(other: Any?): Boolean {
            return other is CellIdentityLteWrapper
                    && other.ci == ci
                    && other.pci == pci
                    && other.tac == tac
                    && other.earfcn == earfcn
                    && other.additionalPlmns == additionalPlmns
                    && other.csgInfo == csgInfo
                    && other.mcc == mcc
                    && other.mnc == mnc
                    && other.alphaLong == alphaLong
                    && other.alphaShort == alphaShort
                    && other.globalCellId == globalCellId
                    && other.channelNumber == channelNumber
                    && other.bandwidth == bandwidth
                    && other.bands == bands
        }
    }

    class CellIdentityNrWrapper(
        val nrArfcn: Int,
        val pci: Int,
        val tac: Int,
        val nci: Long,
        val bands: ArrayList<Int>?,
        val additionalPlmns: ArrayList<String>?,
        mcc: String?,
        mnc: String?,
        alphaLong: String?,
        alphaShort: String?,
        globalCellId: String?,
        channelNumber: Int
    ) : CellIdentityWrapper(
        CellInfo.TYPE_NR, mcc, mnc, alphaLong,
        alphaShort, globalCellId, channelNumber
    ) {
        @RequiresApi(Build.VERSION_CODES.Q)
        constructor(identity: CellIdentityNr) : this(
            identity.nrarfcn,
            identity.pci,
            identity.tac,
            identity.nci,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ArrayList(identity.bands.toList())
            } else {
                null
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ArrayList(identity.additionalPlmns)
            } else {
                null
            },
            identity.mccString,
            identity.mncString,
            identity.operatorAlphaLong.toString(),
            identity.operatorAlphaShort.toString(),
            identity.globalCellId,
            identity.channelNumber
        )

        override fun hashCode(): Int {
            return Objects.hash(
                nci,
                pci,
                tac,
                nrArfcn,
                additionalPlmns,
                bands,
                mcc, mnc, alphaLong, alphaShort,
                globalCellId, channelNumber
            )
        }

        override fun equals(other: Any?): Boolean {
            return other is CellIdentityNrWrapper
                    && other.nci == nci
                    && other.pci == pci
                    && other.tac == tac
                    && other.nrArfcn == nrArfcn
                    && other.additionalPlmns == additionalPlmns
                    && other.mcc == mcc
                    && other.mnc == mnc
                    && other.alphaLong == alphaLong
                    && other.alphaShort == alphaShort
                    && other.globalCellId == globalCellId
                    && other.channelNumber == channelNumber
                    && other.bands == bands
        }
    }
}
