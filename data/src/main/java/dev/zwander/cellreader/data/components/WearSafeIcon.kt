package dev.zwander.cellreader.data.components

import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import dev.zwander.cellreader.data.util.rememberIsWear

@Composable
fun WearSafeIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color? = null
) {
    val context = LocalContext.current
    val isWear = context.rememberIsWear()

    val actualTint = tint ?: if (isWear) {
            androidx.wear.compose.material.LocalContentColor.current.copy(alpha = androidx.wear.compose.material.LocalContentAlpha.current)
        } else {
            LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
        }

    if (isWear) {
        androidx.wear.compose.material.Icon(
            painter, contentDescription, modifier,
            actualTint
        )
    } else {
        androidx.compose.material.Icon(
            painter, contentDescription, modifier,
            actualTint
        )
    }
}