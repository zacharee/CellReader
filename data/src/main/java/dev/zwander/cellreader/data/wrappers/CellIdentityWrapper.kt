package dev.zwander.cellreader.data.wrappers

import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import dev.zwander.cellreader.data.ARFCNInfo
import dev.zwander.cellreader.data.ARFCNTools
import dev.zwander.cellreader.data.util.avail
import dev.zwander.cellreader.data.util.onAvail
import java.util.*
import kotlin.collections.ArrayList

sealed class CellIdentityWrapper(
    val type: Int
) {
    val plmn: String?
        get() = if (mcc.isNullOrBlank()) null else (mcc + mnc)

    open val inferringBands: Boolean
        get() = true

    open val bands: List<String>
        get() {
            val info = arfcnInfo

            return info.map { it.band }
        }

    internal abstract val arfcnInfo: List<ARFCNInfo>

    companion object {
        fun newInstance(identity: CellIdentity?): CellIdentityWrapper? {
            if (identity == null) return null

            return when {
                identity is CellIdentityGsm -> CellIdentityGsmWrapper(identity)
                identity is CellIdentityCdma -> CellIdentityCdmaWrapper(identity)
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) && identity is CellIdentityTdscdma -> CellIdentityTdscdmaWrapper(identity)
                identity is CellIdentityWcdma -> CellIdentityWcdmaWrapper(identity)
                identity is CellIdentityLte -> CellIdentityLteWrapper(identity)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && identity is CellIdentityNr -> CellIdentityNrWrapper(identity)
                else -> throw IllegalArgumentException("Unknown CellIdentity class ${identity.javaClass.canonicalName}")
            }
        }
    }

    abstract val mcc: String?
    abstract val mnc: String?
    abstract val alphaLong: String?
    abstract val alphaShort: String?
    abstract val globalCellId: String?
    abstract val channelNumber: Int

    abstract override fun hashCode(): Int
    abstract override fun equals(other: Any?): Boolean
}

@Suppress("DEPRECATION")
data class CellIdentityGsmWrapper(
    val lac: Int,
    val cid: Int,
    val arfcn: Int,
    val bsic: Int,
    val additionalPlmns: ArrayList<String>?,
    override val mcc: String?,
    override val mnc: String?,
    override val alphaLong: String?,
    override val alphaShort: String?,
    override val globalCellId: String?,
    override val channelNumber: Int
) : CellIdentityWrapper(
    CellInfo.TYPE_GSM
) {
    override val arfcnInfo = ARFCNTools.gsmArfcnToInfo(arfcn)

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.mccString else identity.mcc.onAvail { it.toString() },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.mncString else identity.mnc.onAvail { it.toString() },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.operatorAlphaLong.toString() else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.operatorAlphaShort.toString() else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) identity.globalCellId else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.channelNumber else identity.arfcn
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

data class CellIdentityCdmaWrapper(
    val networkId: Int,
    val systemId: Int,
    val basestationId: Int,
    val longitude: Int,
    val latitude: Int,
    override val alphaLong: String?,
    override val alphaShort: String?,
    override val globalCellId: String?,
    override val channelNumber: Int
) : CellIdentityWrapper(
    CellInfo.TYPE_CDMA
) {
    override val arfcnInfo = listOf<ARFCNInfo>()

    override val mcc: String? = null
    override val mnc: String? = null

    constructor(identity: CellIdentityCdma) : this(
        identity.networkId,
        identity.systemId,
        identity.basestationId,
        identity.longitude,
        identity.latitude,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.operatorAlphaLong.toString() else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.operatorAlphaShort.toString() else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) identity.globalCellId else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.channelNumber else CellInfo.UNAVAILABLE
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

data class CellIdentityTdscdmaWrapper(
    val lac: Int,
    val cid: Int,
    val cpid: Int,
    val uarfcn: Int,
    val additionalPlmns: ArrayList<String>?,
    val csgInfo: ClosedSubscriberGroupInfoWrapper?,
    override val mcc: String?,
    override val mnc: String?,
    override val alphaLong: String?,
    override val alphaShort: String?,
    override val globalCellId: String?,
    override val channelNumber: Int
) : CellIdentityWrapper(
    CellInfo.TYPE_TDSCDMA
) {
    override val arfcnInfo = ARFCNTools.tdscdmaArfcnToInfo(uarfcn)

    constructor(identity: CellIdentityTdscdma) : this(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.lac else CellInfo.UNAVAILABLE,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.cid else CellInfo.UNAVAILABLE,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.cpid else CellInfo.UNAVAILABLE,
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
            identity.closedSubscriberGroupInfo?.let { ClosedSubscriberGroupInfoWrapper(it) }
        } else {
            null
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.mccString else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.mncString else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.operatorAlphaLong.toString() else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.operatorAlphaShort.toString() else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) identity.globalCellId else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.channelNumber else CellInfo.UNAVAILABLE,
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

@Suppress("DEPRECATION")
data class CellIdentityWcdmaWrapper(
    val lac: Int,
    val cid: Int,
    val psc: Int,
    val uarfcn: Int,
    val additionalPlmns: ArrayList<String>?,
    val csgInfo: ClosedSubscriberGroupInfoWrapper?,
    override val mcc: String?,
    override val mnc: String?,
    override val alphaLong: String?,
    override val alphaShort: String?,
    override val globalCellId: String?,
    override val channelNumber: Int
) : CellIdentityWrapper(
    CellInfo.TYPE_WCDMA
) {
    override val arfcnInfo = ARFCNTools.uarfcnToInfo(uarfcn)

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
            identity.closedSubscriberGroupInfo?.let { ClosedSubscriberGroupInfoWrapper(it) }
        } else {
            null
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.mccString else identity.mcc.onAvail { it.toString() },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.mncString else identity.mnc.onAvail { it.toString() },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.operatorAlphaLong.toString() else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.operatorAlphaShort.toString() else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) identity.globalCellId else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.channelNumber else identity.uarfcn
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

@Suppress("DEPRECATION")
data class CellIdentityLteWrapper(
    val ci: Int,
    val pci: Int,
    val tac: Int,
    val earfcn: Int,
    val bandwidth: Int,
    private val _bands: ArrayList<Int>?,
    val additionalPlmns: ArrayList<String>?,
    val csgInfo: ClosedSubscriberGroupInfoWrapper?,
    override val mcc: String?,
    override val mnc: String?,
    override val alphaLong: String?,
    override val alphaShort: String?,
    override val globalCellId: String?,
    override val channelNumber: Int
) : CellIdentityWrapper(
    CellInfo.TYPE_LTE
) {
    override val arfcnInfo: List<ARFCNInfo>
        get() = ARFCNTools.earfcnToInfo(earfcn)

    override val inferringBands: Boolean
        get() = _bands.isNullOrEmpty()

    override val bands: List<String>
        get() = if (inferringBands) super.bands else _bands!!.map { it.toString() }

    constructor(identity: CellIdentityLte) : this(
        identity.ci,
        identity.pci,
        identity.tac,
        identity.earfcn,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.bandwidth else CellInfo.UNAVAILABLE,
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
            identity.closedSubscriberGroupInfo?.let { ClosedSubscriberGroupInfoWrapper(it) }
        } else {
            null
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.mccString else identity.mcc.onAvail { it.toString() },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.mncString else identity.mnc.onAvail { it.toString() },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.operatorAlphaLong.toString() else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.operatorAlphaShort.toString() else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) identity.globalCellId else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.channelNumber else identity.earfcn
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

data class CellIdentityNrWrapper(
    val nrArfcn: Int,
    val pci: Int,
    val tac: Int,
    val nci: Long,
    private val _bands: ArrayList<Int>?,
    val additionalPlmns: ArrayList<String>?,
    override val mcc: String?,
    override val mnc: String?,
    override val alphaLong: String?,
    override val alphaShort: String?,
    override val globalCellId: String?,
    override val channelNumber: Int
) : CellIdentityWrapper(
    CellInfo.TYPE_NR
) {
    override val arfcnInfo = ARFCNTools.nrArfcnToInfo(nrArfcn)

    override val inferringBands: Boolean
        get() = _bands.isNullOrEmpty()

    override val bands: List<String>
        get() = if (inferringBands) super.bands else _bands!!.map { it.toString() }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) identity.globalCellId else null,
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
