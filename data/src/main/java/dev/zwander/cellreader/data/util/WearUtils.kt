package dev.zwander.cellreader.data.util

import android.content.Context
import android.content.pm.PackageManager

val Context.isWear: Boolean
    get() = packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)