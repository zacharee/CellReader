package dev.zwander.cellreader.data.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

inline fun <E : Any?> withTryCatch(default: E? = null, block: () -> E): E? {
    return try {
        block()
    } catch (_: Throwable) {
        default
    }
}

inline fun <E : Any> withTryCatch(default: E, block: () -> E): E {
    return try {
        block()
    } catch (_: Throwable) {
        default
    }
}

@ChecksSdkIntAtLeast(parameter = 1, lambda = 3)
inline fun <E: Any?> withMinApi(api: Int, default: E? = null, block: () -> E): E? {
    return if (Build.VERSION.SDK_INT >= api) {
        block()
    } else {
        default
    }
}

@ChecksSdkIntAtLeast(parameter = 1, lambda = 3)
inline fun <E : Any> withMinApi(api: Int, default: E, block: () -> E): E {
    return if (Build.VERSION.SDK_INT >= api) {
        block()
    } else {
        default
    }
}
