package dev.zwander.cellreader.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import rikka.shizuku.Shizuku

object PermissionUtils {
    private val permissions = arrayListOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.READ_PHONE_STATE,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            add(android.Manifest.permission.READ_PHONE_NUMBERS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }.toList()

    @SuppressLint("InlinedApi")
    fun getMissingPermissions(context: Context, excludeBackgroundLocation: Boolean = false): Array<String> {
        return permissions.filter { permission ->
            context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED &&
                    (!excludeBackgroundLocation || permission == android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }.toTypedArray()
    }

    fun checkShizukuPermission(callback: (result: Int) -> Unit) {
        if (Shizuku.isPreV11()) {
            callback(PackageManager.PERMISSION_DENIED)
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            callback(PackageManager.PERMISSION_GRANTED)
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            callback(PackageManager.PERMISSION_DENIED)
        } else {
            Shizuku.addRequestPermissionResultListener(object : Shizuku.OnRequestPermissionResultListener {
                override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                    callback(grantResult)
                    Shizuku.removeRequestPermissionResultListener(this)
                }
            })
            Shizuku.requestPermission(1001)
        }
    }
}