package dev.zwander.cellreader.data

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.Shapes
import androidx.wear.compose.material.Typography

val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

@Composable
fun CellReaderTheme(content: @Composable() () -> Unit) {
    val colors = Colors(
        primary = colorResource(id = R.color.purple_500),
        primaryVariant = colorResource(id = R.color.purple_200),
        secondary = colorResource(id = R.color.teal_200),
        surface = colorResource(id = R.color.surface),
        background = colorResource(id = R.color.background)
    )

    androidx.compose.material.MaterialTheme(
        colors = darkColors(
            primary = colors.primary,
            primaryVariant = colors.primaryVariant,
            secondary = colors.secondary,
            surface = colors.surface,
            background = colors.background
        ),
        typography = androidx.compose.material.Typography(
            body1 = Typography.body1
        ),
        shapes = androidx.compose.material.Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(12.dp)
        )
    ) {
        androidx.wear.compose.material.MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes(
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(12.dp),
                large = RoundedCornerShape(12.dp)
            ),
            content = content
        )
    }
}