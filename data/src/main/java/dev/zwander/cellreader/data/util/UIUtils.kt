package dev.zwander.cellreader.data.util

import android.content.Context
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

fun Modifier.angledGradient(colors: List<Pair<Float, Color>>, degrees: Float) = this.then(
    Modifier.drawBehind {
        val rad = (degrees * PI / 180).toFloat()
        val diagonal = sqrt(size.width * size.width + size.height * size.height)
        val centerOffsetX = cos(rad) * diagonal / 2
        val centerOffsetY = sin(rad) * diagonal / 2

        // negative so that 0 degrees is left -> right and 90 degrees is top -> bottom
        val startOffset = Offset(
            x = (center.x - centerOffsetX).coerceIn(0f, size.width),
            y = (center.y - centerOffsetY).coerceIn(0f, size.height)
        )
        val endOffset = Offset(
            x = (center.x + centerOffsetX).coerceIn(0f, size.width),
            y = (center.y + centerOffsetY).coerceIn(0f, size.height)
        )

        drawRect(
            brush = Brush.linearGradient(
                colorStops = colors.toTypedArray(),
                start = startOffset,
                end = endOffset
            ),
            size = size
        )
    }
)

@Composable
fun Number.asDp() =
    (dpAsPx()).dp

@Composable
fun Number.dpAsPx() = toFloat() / LocalDensity.current.density

fun Context.dpAsPx(number: Number) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    number.toFloat(),
    resources.displayMetrics
)

fun String.toColorString(): String {
    return String.format("#%06X", (hashCode() * length * length % hashCode() + hashCode() % length + Random(hashCode()).run { nextBits(nextBits(nextBits(500))) }) and 0xFFFFFF)
}

// Ported from https://github.com/RolandR/ColorHash
fun String.toColorString1(): String {
    var sum = 0

    for(i in this){
        sum += i.code
    }

    val r = (("0." + sin(sum + 1.0).toString().substring(6)).toDouble() * 256).toInt().inv()
    val g = (("0." + sin(sum + 2.0).toString().substring(6)).toDouble() * 256).toInt().inv()
    val b = (("0." + sin(sum + 3.0).toString().substring(6)).toDouble() * 256).toInt().inv()

    var hex = "#"

    hex += ("00" + r.toString(16)).run { substring(lastIndex - 1).uppercase() }
    hex += ("00" + g.toString(16)).run { substring(lastIndex - 1).uppercase() }
    hex += ("00" + b.toString(16)).run { substring(lastIndex - 1).uppercase() }

    return hex
}

fun String.toLightEnoughColorInt(): Int {
    var color = Color(toColorInt())

    if (color.luminance() < 0.2) {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color.toColorInt(), hsl)

        color = Color.hsl(hsl[0], hsl[1], hsl[2] + 0.2f)
    }

    return color.toColorInt()
}

fun Color.toColorInt(): Int {
    return android.graphics.Color.argb(alpha, red, green, blue)
}

inline fun <reified T> MutableLiveData<T>.update(noinline clone: ((T?) -> T)? = null, noinline block: ((T?) -> Unit)? = null) {
    val c = if (clone != null) {
        clone(value)
    } else {
        T::class.java.constructors.find {
            it.parameters.size == 1 &&
                    (it.parameters[0].type.isAssignableFrom(T::class.java) ||
                            T::class.java.isAssignableFrom(it.parameters[0].type) ||
                            it.parameters[0].type == T::class.java)
        }?.newInstance(value) as T
    }
    block?.invoke(c)

    this.value = c
}