package dev.zwander.cellreader.ui.components

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.R

@Composable
fun IndenticatorLine(
    size: IntSize,
    isFinal: Boolean,
) {
    val density = LocalDensity.current

    Canvas(
        modifier = Modifier
            .padding(start = 9.5.dp)
            .width(3.dp)
            .height((size.height / density.density).dp),
    ) {
        drawLine(
            brush = SolidColor(Color.White),
            cap = StrokeCap.Round,
            start = Offset.Zero,
            end = Offset(
                0f,
                if (isFinal) (size.height / 2f - (7.5f * density.density)) else size.height.toFloat()
            ),
            strokeWidth = drawContext.size.width
        )
    }
}

@Composable
fun IndenticatorArrow() {
    Image(
        painter = painterResource(id = R.drawable.indent_arrow),
        contentDescription = null,
        modifier = Modifier
            .padding(start = 8.dp, end = 4.dp)
            .width(24.dp)
            .height(24.dp),
        contentScale = ContentScale.Fit,
        alignment = Alignment.CenterStart
    )
}