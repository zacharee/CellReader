package dev.zwander.cellreader.data.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

inline fun <E> withTryCatch(block: () -> E): E? {
    return try {
        block()
    } catch (_: Throwable) {
        null
    }
}

inline fun <E> withTryCatch(default: E, block: () -> E): E {
    return try {
        block()
    } catch (_: Throwable) {
        default
    }
}

@ChecksSdkIntAtLeast(parameter = 1, lambda = 3)
inline fun <E> withMinApi(api: Int, block: () -> E): E? {
    return if (Build.VERSION.SDK_INT >= api) {
        block()
    } else {
        null
    }
}

@ChecksSdkIntAtLeast(parameter = 1, lambda = 3)
inline fun <E> withMinApi(api: Int, default: E, block: () -> E): E {
    return if (Build.VERSION.SDK_INT >= api) {
        block()
    } else {
        default
    }
}
