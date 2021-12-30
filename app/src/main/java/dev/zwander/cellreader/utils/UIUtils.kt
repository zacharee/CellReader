package dev.zwander.cellreader.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.*

fun Modifier.angledGradient(colors: List<Color>, degrees: Float) = this.then(
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
                colors = colors,
                start = startOffset,
                end = endOffset
            ),
            size = size
        )
    }
)