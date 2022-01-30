package dev.zwander.cellreader.data.util

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*

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

val CellInfo.cellIdentityCompat: CellIdentity
    @SuppressLint("NewApi")
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        cellIdentity
    } else {
        this::class.java
            .getMethod("getCellIdentity")
            .invoke(this) as CellIdentity
    }

val CellInfo.cellSignalStrengthCompat: CellSignalStrength
    @SuppressLint("NewApi")
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
        mncString
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