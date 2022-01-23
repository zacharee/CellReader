package dev.zwander.cellreader.data

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

fun CellIdentity.getBands(infos: List<ARFCNInfo>): List<String> {
    return when {
        this is CellIdentityGsm -> infos.map { it.band }
        this is CellIdentityWcdma -> infos.map { it.band }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellIdentityTdscdma -> infos.map { it.band }
        this is CellIdentityLte -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bands.map { it.toString() }
        } else {
            infos.map { it.band }
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && this is CellIdentityNr -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bands.map { it.toString() }
        } else {
            infos.map { it.band }
        }
        else -> listOf()
    }
}