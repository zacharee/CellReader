package dev.zwander.cellreader.data

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.Shapes
import androidx.wear.compose.material.Typography
import dev.zwander.cellreader.data.util.isWear

@Composable
fun CellReaderTheme(content: @Composable() () -> Unit) {
    val context = LocalContext.current
    val isWear = remember {
        context.isWear
    }

    val typography = Typography(
        body1 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = if (isWear) 14.sp else 16.sp
        )
    )

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
            body1 = typography.body1
        ),
        shapes = androidx.compose.material.Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(12.dp)
        )
    ) {
        androidx.wear.compose.material.MaterialTheme(
            colors = colors,
            typography = typography,
            shapes = Shapes(
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(12.dp),
                large = RoundedCornerShape(12.dp)
            ),
            content = content
        )
    }
}