package dev.zwander.cellreader.data.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.rememberIsWear

@Composable
fun IndenticatorLine(
    size: IntSize,
    isFinal: Boolean,
) {
    val density = LocalDensity.current
    val context = LocalContext.current

    val isWear = context.rememberIsWear()

    val lineWidth = if (isWear) 2 else 3
    val finalFactor = if (isWear) 5.5f else 7.5f

    val color = MaterialTheme.colorScheme.onBackground
    val offset by animateFloatAsState(targetValue = if (isFinal) (size.height / 2f - (finalFactor * density.density)) else size.height.toFloat())

    Canvas(
        modifier = Modifier
            .padding(start = (lineWidth / 2f).dp)
            .width(lineWidth.dp)
            .height((size.height / density.density).dp),
    ) {
        drawLine(
            brush = SolidColor(color),
            cap = StrokeCap.Round,
            start = Offset.Zero,
            end = Offset(
                0f,
                offset
            ),
            strokeWidth = drawContext.size.width
        )
    }
}

@Composable
fun IndenticatorArrow() {
    val context = LocalContext.current
    val isWear = context.rememberIsWear()

    val color = MaterialTheme.colorScheme.onBackground

    Image(
        painter = painterResource(id = R.drawable.indent_arrow),
        colorFilter = ColorFilter.tint(color),
        contentDescription = null,
        modifier = Modifier
            .padding(start = 0.dp, end = 4.dp)
            .size(if (isWear) 16.dp else 24.dp),
        contentScale = ContentScale.Fit,
        alignment = Alignment.CenterStart
    )
}