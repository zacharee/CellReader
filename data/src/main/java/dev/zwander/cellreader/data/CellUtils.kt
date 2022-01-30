package dev.zwander.cellreader.data

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import dev.zwander.cellreader.data.wrappers.*
import java.lang.Exception

val CellConfigLte.endcAvailable: Boolean
    get() = this::class.java
        .getDeclaredMethod("isEndcAvailable")
        .apply { isAccessible = true }
        .invoke(this) as Boolean

@Suppress("DEPRECATION")
val CellInfo.timeStampMillisCompat: Long
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) timestampMillis else (timeStamp / 1000000)

fun CellIdentityWrapper.getBands(infos: List<ARFCNInfo>): List<String> {
    return when {
        this is CellIdentityGsmWrapper -> infos.map { it.band }
        this is CellIdentityWcdmaWrapper -> infos.map { it.band }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellIdentityTdscdmaWrapper -> infos.map { it.band }
        this is CellIdentityLteWrapper -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bands?.map { it.toString() } ?: listOf()
        } else {
            infos.map { it.band }
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellIdentityNrWrapper -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bands?.map { it.toString() } ?: listOf()
        } else {
            infos.map { it.band }
        }
        else -> listOf()
    }
}

val CellSignalStrengthGsm.bitErrorRateCompat: Int
    @SuppressLint("DiscouragedPrivateApi")
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        bitErrorRate
    } else {
        CellSignalStrengthGsm::class.java
            .getDeclaredField("mBitErrorRate")
            .apply { isAccessible = true }
            .getInt(this)
    }

val CellSignalStrengthWcdma.bitErrorRateCompat: Int
    @SuppressLint("DiscouragedPrivateApi")
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        bitErrorRate
    } else {
        CellSignalStrengthWcdma::class.java
            .getDeclaredField("mBitErrorRate")
            .apply { isAccessible = true }
            .getInt(this)
    }

val SubscriptionInfo.cardIdCompat: String
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        cardId.toString()
    } else {
        SubscriptionInfo::class.java
            .getMethod("getCardId")
            .invoke(this) as String
    }

val SubscriptionInfo.allAccessRulesCompat: List<UiccAccessRule>
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        allAccessRules ?: listOf()
    } else {
        try {
            accessRules
        } catch (e: Exception) {
            listOf()
        }
    }

class SubsComparator(private val primarySub: Int) : Comparator<Int> {
    override fun compare(o1: Int, o2: Int): Int {
        if (o1 == primarySub) {
            return -1
        }

        if (o2 == primarySub) {
            return 1
        }

        return o1.compareTo(o2)
    }
}