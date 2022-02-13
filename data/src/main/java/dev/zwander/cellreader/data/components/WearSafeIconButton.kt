package dev.zwander.cellreader.data.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ContentAlpha
import dev.zwander.cellreader.data.util.rememberIsWear

@Composable
fun WearSafeIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isWear = context.rememberIsWear()

    if (isWear) {
        WearIconButton(
            onClick, modifier, enabled, interactionSource, content
        )
    } else {
        IconButton(
            onClick, modifier, enabled, interactionSource, content
        )
    }
}

@Composable
fun WearIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false, radius = 24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha = if (enabled) androidx.wear.compose.material.LocalContentAlpha.current else ContentAlpha.disabled
        CompositionLocalProvider(androidx.wear.compose.material.LocalContentAlpha provides contentAlpha, content = content)
    }
}