package dev.zwander.cellreader.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.reflect.javaType

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

fun ByteArray.asBitmap(): Bitmap? {
    return BitmapFactory.decodeByteArray(this, 0, size)
}

fun String.toColor(): Color {
    return Color(toColorString().toColorInt())
}

fun String.toColorString(): String {
    return String.format("#%06X", (hashCode() * length * length % hashCode() + hashCode() % length + Random(hashCode()).run { nextBits(nextBits(nextBits(500))) }) and 0xFFFFFF)
}

fun Color.toColorInt(): Int {
    return android.graphics.Color.argb(alpha, red, green, blue)
}

inline fun <reified T> MutableLiveData<T>.update(block: (T?) -> T) {
    this.value = block(value)
}