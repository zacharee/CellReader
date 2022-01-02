package dev.zwander.cellreader.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.R

@Composable
fun CellReaderTheme(content: @Composable () -> Unit) {
    val colors = darkColors(
        primary = colorResource(id = R.color.purple_500),
        primaryVariant = colorResource(id = R.color.purple_200),
        secondary = colorResource(id = R.color.teal_200),
        surface = colorResource(id = R.color.surface),
        background = colorResource(id = R.color.background)
    )

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes.copy(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(12.dp)
        ),
        content = content
    )
}