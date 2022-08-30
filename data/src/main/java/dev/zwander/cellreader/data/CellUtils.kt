package dev.zwander.cellreader.data

import android.annotation.SuppressLint
import android.content.Context
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

fun CellSignalStrengthWrapper.typeString(context: Context): String {
    return context.resources.getString(when {
        this is CellSignalStrengthGsmWrapper -> R.string.gsm
        this is CellSignalStrengthWcdmaWrapper -> R.string.wcdma
        this is CellSignalStrengthCdmaWrapper -> R.string.cdma
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellSignalStrengthTdscdmaWrapper -> R.string.tdscdma
        this is CellSignalStrengthLteWrapper -> R.string.lte
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellSignalStrengthNrWrapper -> R.string.nr
        else -> R.string.unknown
    })
}

fun CellIdentityWrapper.typeString(context: Context): String {
    return context.resources.getString(when (type) {
        CellInfo.TYPE_GSM -> R.string.gsm
        CellInfo.TYPE_WCDMA -> R.string.wcdma
        CellInfo.TYPE_CDMA -> R.string.cdma
        CellInfo.TYPE_TDSCDMA -> R.string.tdscdma
        CellInfo.TYPE_LTE -> R.string.lte
        CellInfo.TYPE_NR -> R.string.nr
        else -> R.string.unknown
    })
}

val CellSignalStrength.isValidCompat: Boolean?
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) isValid else null

val ServiceState.isIwlanPreferredCompat: Boolean?
    @SuppressLint("BlockedPrivateApi")
    get() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> isIwlanPreferred
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            ServiceState::class.java.getDeclaredField("mIsIwlanPreferred")
                .apply { isAccessible = true }
                .getBoolean(this)
        }
        else -> null
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