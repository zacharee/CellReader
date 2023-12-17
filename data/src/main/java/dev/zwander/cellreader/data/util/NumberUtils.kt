package dev.zwander.cellreader.data.util

import kotlin.math.pow
import kotlin.math.roundToInt

fun Number.roundTo(numFractionDigits: Int): Number {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this.toDouble() * factor).roundToInt() / factor
}
