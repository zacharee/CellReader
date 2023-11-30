package dev.zwander.cellreader.data.util

import android.os.Build
import android.util.Log

fun Class<*>.printAllMethods() {
    Log.e("CellReader",
        "Methods for ${this::class.java.canonicalName}\n" +
                this::class.java.methods.joinToString("\n") {
                    "${it.name}(${
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            it.parameters.joinToString(
                                ", "
                            ) { param -> param.type.canonicalName ?: param.type.name }
                        } else {
                            "UNKNOWN PARAMETERS"
                        }
                    }): ${it.returnType.canonicalName}"
                }
    )
}
