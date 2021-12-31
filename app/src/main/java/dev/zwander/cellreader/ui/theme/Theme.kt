package dev.zwander.cellreader.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200,
    surface = Color(0xff343434)
)

@Composable
fun CellReaderTheme(content: @Composable () -> Unit) {
    val colors = DarkColorPalette

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