package dev.zwander.cellreader.utils

import android.content.Context
import android.content.pm.PackageManager

object PermissionUtils {
    val permissions = arrayOf(
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.ACCESS_WIFI_STATE,
//        android.Manifest.permission.READ_PHONE_NUMBERS
    )

    fun getMissingPermissions(context: Context): Array<String> {
        return permissions.filter { permission ->
            context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }
}