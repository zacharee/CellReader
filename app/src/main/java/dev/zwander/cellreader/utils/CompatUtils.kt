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

val CellInfo.cellIdentityCompat: CellIdentity
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        cellIdentity
    } else {
        this::class.java
            .getMethod("getCellIdentity")
            .invoke(this) as CellIdentity
    }

val CellInfo.cellSignalStrengthCompat: CellSignalStrength
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        cellSignalStrength
    } else {
        this::class.java
            .getMethod("getCellSignalStrength")
            .invoke(this) as CellSignalStrength
    }

val CellIdentity.mccStringCompat: String?
    @SuppressLint("SoonBlockedPrivateApi")
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mccString
    } else {
        CellIdentity::class.java
            .getDeclaredField("mMccStr")
            .apply { isAccessible = true }
            .get(this)?.toString()
    }

val CellIdentity.mncStringCompat: String?
    @SuppressLint("SoonBlockedPrivateApi")
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mccString
    } else {
        CellIdentity::class.java
            .getDeclaredField("mMncStr")
            .apply { isAccessible = true }
            .get(this)?.toString()
    }

val UiccAccessRule.accessTypeCompat: Long
    @SuppressLint("SoonBlockedPrivateApi")
    get() = UiccAccessRule::class.java
        .getDeclaredField("mAccessType")
        .apply { isAccessible = true }
        .getLong(this)