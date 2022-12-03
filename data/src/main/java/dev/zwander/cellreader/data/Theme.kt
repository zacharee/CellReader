package dev.zwander.cellreader.data

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.Shapes
import androidx.wear.compose.material.Typography
import dev.zwander.cellreader.data.util.rememberIsWear

val LocalAnimationDuration = staticCompositionLocalOf { 0 }

@Composable
fun CellReaderTheme(content: @Composable() () -> Unit) {
    val context = LocalContext.current
    val isWear = context.rememberIsWear()

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

    val dark = isSystemInDarkTheme()

    val md3Colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (dark) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        dark -> {
            darkColorScheme(
                primary = colors.primary,
                secondary = colors.secondary,
                tertiary = colors.primaryVariant,
            )
        }
        else -> {
            lightColorScheme(
                primary = colors.primary,
                secondary = colors.secondary,
                tertiary = colors.primaryVariant
            )
        }
    }

    MaterialTheme(
        colorScheme = md3Colors,
        typography = androidx.compose.material3.Typography(
            bodySmall = typography.body1
        ),
        shapes = androidx.compose.material3.Shapes(
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
            content = {
                CompositionLocalProvider(
                    LocalAnimationDuration provides integerResource(id = android.R.integer.config_longAnimTime)
                ) {
                    content()
                }
            }
        )
    }
}