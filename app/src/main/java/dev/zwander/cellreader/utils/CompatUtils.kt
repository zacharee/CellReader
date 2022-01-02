package dev.zwander.cellreader.utils

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import java.lang.Exception

val TelephonyManager.subscriptionIdCompat: Int
    @SuppressLint("SoonBlockedPrivateApi")
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        subscriptionId
    } else {
        TelephonyManager::class.java
            .getDeclaredMethod("getSubId")
            .apply { isAccessible = true }
            .invoke(this) as Int
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