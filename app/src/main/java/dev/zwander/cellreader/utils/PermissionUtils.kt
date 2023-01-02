package dev.zwander.cellreader.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import rikka.shizuku.Shizuku

object PermissionUtils {
    private val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.READ_PHONE_NUMBERS,
    ) + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)
    } else {
        arrayOf()
    } + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        arrayOf()
    }

    fun getMissingPermissions(context: Context): Array<String> {
        return permissions.filter { permission ->
            context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
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