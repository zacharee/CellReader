package dev.zwander.cellreader.data

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*

val CellConfigLte.endcAvailable: Boolean
    get() = this::class.java
        .getDeclaredMethod("isEndcAvailable")
        .apply { isAccessible = true }
        .invoke(this) as Boolean

@Suppress("DEPRECATION")
val CellInfo.timeStampMillisCompat: Long
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) timestampMillis else (timeStamp / 1000000)

val CellSignalStrengthGsm.bitErrorRateCompat: Int
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        bitErrorRate
    } else {
        CellSignalStrengthGsm::class.java
            .getDeclaredField("mBitErrorRate")
            .apply { isAccessible = true }
            .getInt(this)
    }

val CellSignalStrengthWcdma.bitErrorRateCompat: Int
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        bitErrorRate
    } else {
        CellSignalStrengthWcdma::class.java
            .getDeclaredField("mBitErrorRate")
            .apply { isAccessible = true }
            .getInt(this)
    }

val SubscriptionInfo.cardIdCompat: String?
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        cardId.toString()
    } else {
        try {
            SubscriptionInfo::class.java
                .getMethod("getCardId")
                .invoke(this) as String
        } catch (e: Throwable) {
            null
        }
    }

val SubscriptionInfo.allAccessRulesCompat: List<UiccAccessRule>
    get() = try {
        @Suppress("UNCHECKED_CAST")
        SubscriptionInfo::class.java
            .getMethod("getAllAccessRules")
            .invoke(this) as? List<UiccAccessRule>? ?: listOf()
    } catch (e: Throwable) {
        try {
            accessRules
        } catch (e: Throwable) {
            listOf()
        }
    }

val CellSignalStrength.isValidCompat: Boolean?
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) isValid else null

val ServiceState.isIwlanPreferredCompat: Boolean?
    @SuppressLint("BlockedPrivateApi", "PrivateApi")
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