package dev.zwander.cellreader.data.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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

    Canvas(
        modifier = Modifier
            .padding(start = (lineWidth / 2f).dp)
            .width(lineWidth.dp)
            .height((size.height / density.density).dp),
    ) {
        drawLine(
            brush = SolidColor(Color.White),
            cap = StrokeCap.Round,
            start = Offset.Zero,
            end = Offset(
                0f,
                if (isFinal) (size.height / 2f - (finalFactor * density.density)) else size.height.toFloat()
            ),
            strokeWidth = drawContext.size.width
        )
    }
}

@Composable
fun IndenticatorArrow() {
    val context = LocalContext.current
    val isWear = context.rememberIsWear()

    Image(
        painter = painterResource(id = R.drawable.indent_arrow),
        contentDescription = null,
        modifier = Modifier
            .padding(start = 0.dp, end = 4.dp)
            .size(if (isWear) 16.dp else 24.dp),
        contentScale = ContentScale.Fit,
        alignment = Alignment.CenterStart
    )
}