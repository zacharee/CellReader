package dev.zwander.cellreader.data.wrappers

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName
import dev.zwander.cellreader.data.ARFCNInfo
import dev.zwander.cellreader.data.ARFCNTools
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.util.withMinApi
import java.util.*

sealed class CellIdentityWrapper(
    val type: CellType,
) {
    @SerializedName("realBandsBase")
    open val realBands: List<String> = listOf()

    val plmn: String? by lazy { if (mcc.isNullOrBlank()) null else (mcc + mnc) }
    val inferredBands: List<String> by lazy { arfcnInfo.map { it.band } }
    val hasBands: Boolean by lazy { realBands.isNotEmpty() || inferredBands.isNotEmpty() }

    fun formattedBandString(full: Boolean): String {
        val realString = realBands.joinToString(", ")
        val inferredString = inferredBands.joinToString(", ", "(", ")")

        return when {
            realString.isNotBlank() -> "$realString${if (full) "\n$inferredString" else ""}"
            else -> inferredString
        }
    }

    val arfcnInfo: List<ARFCNInfo> by lazy { ARFCNTools.getInfo(channelNumber, type) }

    @Suppress("DEPRECATION")
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : CellIdentityWrapper> newInstance(identity: Any): T {
            return when {
                identity is CellIdentityGsm -> CellIdentityGsmWrapper(identity)
                identity is CellIdentityCdma -> CellIdentityCdmaWrapper(identity)
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) && identity is CellIdentityTdscdma -> CellIdentityTdscdmaWrapper(identity)
                identity is CellIdentityWcdma -> CellIdentityWcdmaWrapper(identity)
                identity is CellIdentityLte -> CellIdentityLteWrapper(identity)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && identity is CellIdentityNr -> CellIdentityNrWrapper(identity)
                else -> throw IllegalArgumentException("Unknown CellIdentity class ${identity.javaClass.canonicalName}")
            } as T
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
    val additionalPlmns: Set<String>?,
    override val mcc: String?,
    override val mnc: String?,
    override val alphaLong: String?,
    override val alphaShort: String?,
    override val globalCellId: String?,
    override val channelNumber: Int,
) : CellIdentityWrapper(
    CellType.GSM,
) {
    constructor(identity: CellIdentityGsm) : this(
        lac = identity.lac,
        cid = identity.cid,
        arfcn = identity.arfcn,
        bsic = identity.bsic,
        additionalPlmns = withMinApi(Build.VERSION_CODES.R) {
            identity.additionalPlmns
        },
        mcc = withMinApi(Build.VERSION_CODES.P, identity.mcc.onAvail { it.toString() }) {
            identity.mccString
        },
        mnc = withMinApi(Build.VERSION_CODES.P, identity.mnc.onAvail { it.toString() }) {
            identity.mncString
        },
        alphaLong = withMinApi(Build.VERSION_CODES.P) {
            identity.operatorAlphaLong.toString()
        },
        alphaShort = withMinApi(Build.VERSION_CODES.P) {
            identity.operatorAlphaShort.toString()
        },
        globalCellId = withMinApi(Build.VERSION_CODES.R) {
            identity.globalCellId
        },
        channelNumber = withMinApi(Build.VERSION_CODES.P, identity.arfcn) {
            identity.channelNumber
        },
    )

    override fun hashCode(): Int {
        return Objects.hash(
            lac, cid,
            additionalPlmns,
            mcc, mnc,
            alphaLong, alphaShort,
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

@Suppress("DEPRECATION")
data class CellIdentityCdmaWrapper(
    val networkId: Int,
    val systemId: Int,
    val basestationId: Int,
    val longitude: Int,
    val latitude: Int,
    override val alphaLong: String?,
    override val alphaShort: String?,
    override val globalCellId: String?,
    override val channelNumber: Int,
) : CellIdentityWrapper(
    CellType.CDMA,
) {
    override val mcc: String? = null
    override val mnc: String? = null

    @SuppressLint("InlinedApi")
    constructor(identity: CellIdentityCdma) : this(
        networkId = identity.networkId,
        systemId = identity.systemId,
        basestationId = identity.basestationId,
        longitude = identity.longitude,
        latitude = identity.latitude,
        alphaLong = withMinApi(Build.VERSION_CODES.P) {
            identity.operatorAlphaLong.toString()
        },
        alphaShort = withMinApi(Build.VERSION_CODES.P) {
            identity.operatorAlphaShort.toString()
        },
        globalCellId = withMinApi(Build.VERSION_CODES.R) {
            identity.globalCellId
        },
        channelNumber = withMinApi(Build.VERSION_CODES.P, CellInfo.UNAVAILABLE) {
            identity.channelNumber
        },
    )

    override fun hashCode(): Int {
        return Objects.hash(
            networkId,
            systemId,
            basestationId,
            latitude,
            longitude,
            alphaLong, alphaShort,
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
    val additionalPlmns: Set<String>?,
    val csgInfo: ClosedSubscriberGroupInfoWrapper?,
    override val mcc: String?,
    override val mnc: String?,
    override val alphaLong: String?,
    override val alphaShort: String?,
    override val globalCellId: String?,
    override val channelNumber: Int,
) : CellIdentityWrapper(
    CellType.TDSCDMA,
) {
    @SuppressLint("InlinedApi")
    constructor(identity: CellIdentityTdscdma) : this(
        lac = withMinApi(Build.VERSION_CODES.P, CellInfo.UNAVAILABLE) {
            identity.lac
        },
        cid = withMinApi(Build.VERSION_CODES.P, CellInfo.UNAVAILABLE) {
            identity.cid
        },
        cpid = withMinApi(Build.VERSION_CODES.P, CellInfo.UNAVAILABLE) {
            identity.cpid
        },
        uarfcn = withMinApi(Build.VERSION_CODES.Q, CellInfo.UNAVAILABLE) {
            identity.uarfcn
        },
        additionalPlmns = withMinApi(Build.VERSION_CODES.R) {
            identity.additionalPlmns
        },
        csgInfo = withMinApi(Build.VERSION_CODES.R) {
            identity.closedSubscriberGroupInfo?.let { ClosedSubscriberGroupInfoWrapper(it) }
        },
        mcc = withMinApi(Build.VERSION_CODES.P) {
            identity.mccString
        },
        mnc = withMinApi(Build.VERSION_CODES.P) {
            identity.mncString
        },
        alphaLong = withMinApi(Build.VERSION_CODES.P) {
            identity.operatorAlphaLong.toString()
        },
        alphaShort = withMinApi(Build.VERSION_CODES.P) {
            identity.operatorAlphaShort.toString()
        },
        globalCellId = withMinApi(Build.VERSION_CODES.R) {
            identity.globalCellId
        },
        channelNumber = withMinApi(Build.VERSION_CODES.P, identity.uarfcn) {
            identity.channelNumber
        },
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
            globalCellId, channelNumber,
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
    val additionalPlmns: Set<String>?,
    val csgInfo: ClosedSubscriberGroupInfoWrapper?,
    override val mcc: String?,
    override val mnc: String?,
    override val alphaLong: String?,
    override val alphaShort: String?,
    override val globalCellId: String?,
    override val channelNumber: Int,
) : CellIdentityWrapper(
    CellType.WCDMA,
) {
    constructor(identity: CellIdentityWcdma) : this(
        lac = identity.lac,
        cid = identity.cid,
        psc = identity.psc,
        uarfcn = identity.uarfcn,
        additionalPlmns = withMinApi(Build.VERSION_CODES.R) {
            identity.additionalPlmns
        },
        csgInfo = withMinApi(Build.VERSION_CODES.R) {
            identity.closedSubscriberGroupInfo?.let { ClosedSubscriberGroupInfoWrapper(it) }
        },
        mcc = withMinApi(Build.VERSION_CODES.P, identity.mcc.onAvail { it.toString() }) {
            identity.mccString
        },
        mnc = withMinApi(Build.VERSION_CODES.P, identity.mnc.onAvail { it.toString() }) {
            identity.mncString
        },
        alphaLong = withMinApi(Build.VERSION_CODES.P) {
            identity.operatorAlphaLong.toString()
        },
        alphaShort = withMinApi(Build.VERSION_CODES.P) {
            identity.operatorAlphaShort.toString()
        },
        globalCellId = withMinApi(Build.VERSION_CODES.R) {
            identity.globalCellId
        },
        channelNumber = withMinApi(Build.VERSION_CODES.P, identity.uarfcn) {
            identity.channelNumber
        },
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
            globalCellId, channelNumber,
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
    override val realBands: List<String>,
    val additionalPlmns: Set<String>?,
    val csgInfo: ClosedSubscriberGroupInfoWrapper?,
    override val mcc: String?,
    override val mnc: String?,
    override val alphaLong: String?,
    override val alphaShort: String?,
    override val globalCellId: String?,
    override val channelNumber: Int,
) : CellIdentityWrapper(
    CellType.LTE,
) {
    @SuppressLint("InlinedApi")
    constructor(identity: CellIdentityLte) : this(
        ci = identity.ci,
        pci = identity.pci,
        tac = identity.tac,
        earfcn = identity.earfcn,
        bandwidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) identity.bandwidth else CellInfo.UNAVAILABLE,
        realBands = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            identity.bands.toList().map { it.toString() }
        } else {
            listOf()
        },
        additionalPlmns = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            identity.additionalPlmns
        } else {
            null
        },
        csgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            identity.closedSubscriberGroupInfo?.let { ClosedSubscriberGroupInfoWrapper(it) }
        } else {
            null
        },
        mcc = withMinApi(Build.VERSION_CODES.P, identity.mcc.onAvail { it.toString() }) {
            identity.mccString
        },
        mnc = withMinApi(Build.VERSION_CODES.P, identity.mnc.onAvail { it.toString() }) {
            identity.mncString
        },
        alphaLong = withMinApi(Build.VERSION_CODES.P) {
            identity.operatorAlphaLong.toString()
        },
        alphaShort = withMinApi(Build.VERSION_CODES.P) {
            identity.operatorAlphaShort.toString()
        },
        globalCellId = withMinApi(Build.VERSION_CODES.R) {
            identity.globalCellId
        },
        channelNumber = withMinApi(Build.VERSION_CODES.P, identity.earfcn) {
            identity.channelNumber
        },
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
            realBands,
            inferredBands,
            mcc, mnc, alphaLong, alphaShort,
            globalCellId, channelNumber,
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
                && other.inferredBands == inferredBands
                && other.realBands == realBands
    }
}

data class CellIdentityNrWrapper(
    val nrArfcn: Int,
    val pci: Int,
    val tac: Int,
    val nci: Long,
    override val realBands: List<String>,
    val additionalPlmns: Set<String>?,
    override val mcc: String?,
    override val mnc: String?,
    override val alphaLong: String?,
    override val alphaShort: String?,
    override val globalCellId: String?,
    override val channelNumber: Int,
) : CellIdentityWrapper(
    CellType.NR,
) {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(identity: CellIdentityNr) : this(
        nrArfcn = identity.nrarfcn,
        pci = identity.pci,
        tac = identity.tac,
        nci = identity.nci,
        realBands = withMinApi(Build.VERSION_CODES.R, listOf()) {
            identity.bands.toList().map { it.toString() }
        },
        additionalPlmns = withMinApi(Build.VERSION_CODES.R) {
            identity.additionalPlmns
        },
        mcc = identity.mccString,
        mnc = identity.mncString,
        alphaLong = identity.operatorAlphaLong.toString(),
        alphaShort = identity.operatorAlphaShort.toString(),
        globalCellId = withMinApi(Build.VERSION_CODES.R) {
            identity.globalCellId
        },
        channelNumber = identity.channelNumber,
    )

    override fun hashCode(): Int {
        return Objects.hash(
            nci,
            pci,
            tac,
            nrArfcn,
            additionalPlmns,
            realBands,
            inferredBands,
            mcc, mnc, alphaLong, alphaShort,
            globalCellId, channelNumber,
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
                && other.inferredBands == inferredBands
                && other.realBands == realBands
    }
}
