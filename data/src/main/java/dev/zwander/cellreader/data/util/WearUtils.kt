package dev.zwander.cellreader.data.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable

val Context.isWear: Boolean
    get() = packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)

@Composable
fun Context.rememberIsWear(): Boolean {
    return rememberSaveable(inputs = arrayOf(isWear)) {
        isWear
    }
}